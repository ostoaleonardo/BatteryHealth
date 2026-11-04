package com.monospace.battery.service.alerts

import android.app.ActivityManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.BatteryManager
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.PowerManager
import android.os.SystemClock
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.monospace.battery.MainActivity
import com.monospace.battery.R
import com.monospace.battery.core.constants.Constants
import com.monospace.battery.core.utils.BatteryUtils
import com.monospace.battery.data.local.PreferenceManager
import com.monospace.battery.data.local.db.BatteryDatabase
import com.monospace.battery.data.models.BatteryHistoryEntry
import com.monospace.battery.data.models.ChargeSession
import com.monospace.battery.data.models.ScreenEvent
import com.monospace.battery.notifications.NotificationHelper
import com.monospace.battery.widgets.WidgetsUpdater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.TimeUnit

class BatteryAlertService : Service() {

    private lateinit var prefs: PreferenceManager
    private lateinit var db: BatteryDatabase
    private lateinit var alertHandler: BatteryAlertHandler

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val sessionMutex = Mutex()
    private var monitoringJob: Job? = null

    private var initialized = false
    private var receiverRegistered = false

    @Volatile private var lastLevel = -1
    private var lastStatus = -1
    @Volatile private var charging = false
    private var lastWidgetUpdate = 0L
    private var lastSeenSavedAt = 0L
    private var receiverThread: HandlerThread? = null

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            runCatching { processBatteryIntent(intent) }
                .onFailure { reportError("Battery broadcast failed", it) }
        }
    }

    override fun onCreate() {
        super.onCreate()

        if (!startAsForeground()) {
            stopSelf()
            return
        }

        runCatching {
            initDependencies()
            observeMonitoringToggle()
            setupBatteryReceiver()
            recordInitialScreenState()
            purgeOldData()
            initialized = true
        }.onFailure { e ->
            reportError("Failed to initialize service", e)
            stopSelf()
        }
    }

    private fun initDependencies() {
        prefs = PreferenceManager(this)
        db = BatteryDatabase.getDatabase(this)
        alertHandler = BatteryAlertHandler(
            this, scope, prefs, NotificationHelper(this), BatteryUtils(this), db
        )
    }

    private fun setupBatteryReceiver() {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }

        val thread = HandlerThread(RECEIVER_THREAD_NAME).also { it.start() }
        receiverThread = thread

        registerReceiver(batteryReceiver, filter, null, Handler(thread.looper))?.let { intent ->
            lastLevel = levelOf(intent)
            lastStatus = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            charging = isCharging(lastStatus, intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0))
        }
        receiverRegistered = true
    }

    private fun recordInitialScreenState() {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        recordScreenEvent(powerManager.isInteractive)
    }

    private fun purgeOldData() {
        scope.launch {
            runCatching {
                val cutoff = System.currentTimeMillis() -
                        TimeUnit.DAYS.toMillis(Constants.HISTORY_RETENTION_DAYS.toLong())
                db.batteryDao().deleteHistoryBefore(cutoff)
                db.batteryDao().deleteScreenEventsBefore(cutoff)
            }.onFailure { Log.w(TAG, "History purge failed", it) }
        }
    }

    private fun processBatteryIntent(intent: Intent) {
        when (intent.action) {
            Intent.ACTION_SCREEN_ON -> recordScreenEvent(true)
            Intent.ACTION_SCREEN_OFF -> {
                recordScreenEvent(false)
                checkAodStart("SCREEN_OFF")
            }

            Intent.ACTION_BATTERY_CHANGED -> handleBatteryChanged(intent)
        }
    }

    private fun handleBatteryChanged(intent: Intent) {
        val batteryPct = levelOf(intent)
        val temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val source = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)

        val isCharging = isCharging(status, source)
        val wasCharging = isCharging(lastStatus, 1)
        charging = isCharging

        alertHandler.checkAlerts(batteryPct, temperature, isCharging, lastStatus)

        manageChargeSession(isCharging, wasCharging, batteryPct, source)

        if (isCharging && !wasCharging) checkAodStart("CHARGE_STARTED")

        if (batteryPct != lastLevel || isCharging != wasCharging) {
            if (shouldRecordHistory()) recordHistoryEntry(batteryPct, temperature)
            saveLastSeen(batteryPct)
        }

        refreshWidgets(batteryPct != lastLevel, isCharging != wasCharging)

        lastLevel = batteryPct
        lastStatus = status
    }

    private fun shouldRecordHistory() =
        prefs.getAlert(Constants.KEY_ACTIVE_MONITORING, false) ||
                prefs.getAlert(Constants.KEY_FAST_DISCHARGE_ENABLED, false)

    private fun saveLastSeen(level: Int) {
        val now = System.currentTimeMillis()
        if (now - lastSeenSavedAt < Constants.LAST_SEEN_SAVE_INTERVAL_MS) return

        lastSeenSavedAt = now
        prefs.edit(Constants.PREFS_ALERTS) {
            putLong(Constants.KEY_LAST_SEEN_TIME, now)
            putInt(Constants.KEY_LAST_SEEN_LEVEL, level)
        }
    }

    private fun refreshWidgets(levelChanged: Boolean, plugChanged: Boolean) {
        val now = SystemClock.elapsedRealtime()
        if (!plugChanged && (!levelChanged || now - lastWidgetUpdate < WIDGET_REFRESH_MS)) return

        lastWidgetUpdate = now
        scope.launch { WidgetsUpdater.updateLive(applicationContext) }
    }

    private fun manageChargeSession(
        isCharging: Boolean,
        wasCharging: Boolean,
        level: Int,
        source: Int
    ) {
        if (!prefs.getAlert(Constants.KEY_ACTIVE_MONITORING, false)) return

        if (isCharging && !wasCharging) {
            scope.launch { startNewSession(level, source) }
        } else if (!isCharging && wasCharging) {
            scope.launch { closeActiveSession(level) }
        }
    }

    private fun checkAodStart(trigger: String) {
        if (charging) AodLauncher.launchIfNeeded(this, trigger)
    }

    private fun observeMonitoringToggle() {
        monitoringJob = scope.launch {
            var first = true
            prefs.observeAlert(Constants.KEY_ACTIVE_MONITORING, false)
                .collect { isEnabled ->
                    runCatching { syncSession(isEnabled, first) }
                    first = false
                }
        }
    }

    private suspend fun syncSession(isEnabled: Boolean, firstRun: Boolean) {
        val intent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val source = intent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0
        val level = intent?.let { levelOf(it) } ?: -1

        if (isEnabled && isCharging(status, source)) {
            startNewSession(level.coerceAtLeast(0), source)
            return
        }

        val lastSeen = prefs.get(Constants.PREFS_ALERTS, Constants.KEY_LAST_SEEN_TIME, 0L)
        val now = System.currentTimeMillis()

        if (firstRun && lastSeen in 1 until now) {
            val lastSeenLevel = prefs.get(Constants.PREFS_ALERTS, Constants.KEY_LAST_SEEN_LEVEL, level)
            closeActiveSession(lastSeenLevel, lastSeen)
        } else {
            closeActiveSession(level, now)
        }
    }

    private suspend fun startNewSession(level: Int, source: Int) {
        sessionMutex.withLock {
            if (db.batteryDao().getActiveSession() == null) {
                db.batteryDao().insertChargeSession(
                    ChargeSession(
                        startTime = System.currentTimeMillis(),
                        startLevel = level,
                        chargeSource = source
                    )
                )
            }
        }
    }

    private suspend fun closeActiveSession(level: Int, endTime: Long = System.currentTimeMillis()) {
        runCatching {
            sessionMutex.withLock {
                db.batteryDao().closeAllActiveSessions(endTime, level)
            }
        }
    }

    private fun recordHistoryEntry(level: Int, temperature: Int) {
        val timestamp = System.currentTimeMillis()
        scope.launch {
            runCatching {
                db.batteryDao().insertBatteryEntry(
                    BatteryHistoryEntry(timestamp = timestamp, level = level, temperature = temperature)
                )
            }.onFailure { Log.e(TAG, "Error recording battery data", it) }
        }
    }

    private fun startAsForeground(): Boolean = runCatching {
        NotificationHelper.createChannels(this)

        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, Constants.SERVICE_CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.notification_service_running))
            .setSmallIcon(R.drawable.bolt)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                Constants.NOTIFICATION_SERVICE_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(Constants.NOTIFICATION_SERVICE_ID, notification)
        }
    }.onFailure { reportError("startForeground failed", it) }.isSuccess

    override fun onDestroy() {
        if (receiverRegistered) {
            runCatching { unregisterReceiver(batteryReceiver) }
            receiverRegistered = false
        }
        monitoringJob?.cancel()
        receiverThread?.quitSafely()
        receiverThread = null

        if (initialized) {
            val level = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                ?.let { levelOf(it) } ?: lastLevel
            scope.launch { closeActiveSession(level) }.invokeOnCompletion { scope.cancel() }
        } else {
            scope.cancel()
        }

        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_SERVICE) {
            stopSelf(startId)
            return START_NOT_STICKY
        }

        startAsForeground()
        return if (initialized) START_STICKY else START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun recordScreenEvent(isOn: Boolean) {
        if (!shouldRecordHistory()) return

        val timestamp = System.currentTimeMillis()
        val level = lastLevel
        scope.launch {
            runCatching {
                db.batteryDao().insertScreenEvent(
                    ScreenEvent(timestamp = timestamp, isScreenOn = isOn, batteryLevel = level)
                )
            }
        }
    }

    private fun levelOf(intent: Intent): Int {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
        return if (scale > 0 && level >= 0) level * 100 / scale else -1
    }

    private fun isCharging(status: Int, source: Int) =
        (status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL) && source != 0

    private fun reportError(message: String, error: Throwable) {
        Log.e(TAG, message, error)
        runCatching { FirebaseCrashlytics.getInstance().recordException(error) }
    }

    companion object {
        const val TAG = "BatteryAlertService"
        private const val RECEIVER_THREAD_NAME = "battery-receiver"
        const val ACTION_STOP_SERVICE = "STOP_BATTERY_MONITORING"
        private const val WIDGET_REFRESH_MS = 60_000L

        fun start(context: Context) {
            ContextCompat.startForegroundService(context, Intent(context, BatteryAlertService::class.java))
        }

        @Suppress("DEPRECATION")
        fun stop(context: Context) {
            val manager = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
            val running = manager.getRunningServices(Int.MAX_VALUE)
                .any { it.service.className == BatteryAlertService::class.java.name }

            if (running) {
                context.startService(
                    Intent(context, BatteryAlertService::class.java).setAction(ACTION_STOP_SERVICE)
                )
            }
        }
    }
}
