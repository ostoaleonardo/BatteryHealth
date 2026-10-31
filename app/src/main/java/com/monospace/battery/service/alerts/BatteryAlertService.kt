package com.monospace.battery.service.alerts

import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.SystemClock
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.monospace.battery.MainActivity
import com.monospace.battery.R
import com.monospace.battery.core.constants.Constants
import com.monospace.battery.core.utils.BatteryUtils
import com.monospace.battery.data.local.PreferenceManager
import com.monospace.battery.data.local.WidgetsUtils
import com.monospace.battery.data.local.db.BatteryDatabase
import com.monospace.battery.data.models.BatteryHistoryEntry
import com.monospace.battery.data.models.ChargeSession
import com.monospace.battery.data.models.ScreenEvent
import com.monospace.battery.notifications.NotificationHelper
import com.monospace.battery.ui.screens.AlwaysOnDisplayActivity
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

    private var lastLevel = -1
    private var lastStatus = -1
    private var charging = false
    private var lastWidgetUpdate = 0L

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

        registerReceiver(batteryReceiver, filter)?.let { intent ->
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

        val recordHistory = prefs.getAlert(Constants.KEY_ACTIVE_MONITORING, false) ||
                prefs.getAlert(Constants.KEY_FAST_DISCHARGE_ENABLED, false)
        if (recordHistory && (batteryPct != lastLevel || isCharging != wasCharging)) {
            recordHistoryEntry(batteryPct, temperature)
        }

        refreshWidgets(batteryPct != lastLevel, isCharging != wasCharging)

        lastLevel = batteryPct
        lastStatus = status
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
        val aodEnabled = prefs.getAlert(Constants.KEY_AOD_ENABLED, false)
        val isPremium = WidgetsUtils.isWidgetsPurchased(this)

        if (!aodEnabled || !isPremium || !charging) return

        val powerManager = getSystemService(POWER_SERVICE) as PowerManager

        if (!powerManager.isInteractive && android.provider.Settings.canDrawOverlays(this)) {
            Log.d(TAG, "Launching AOD ($trigger)")
            val intent = Intent(this, AlwaysOnDisplayActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            runCatching { startActivity(intent) }
                .onFailure { reportError("AOD launch blocked", it) }
        }
    }

    private fun observeMonitoringToggle() {
        monitoringJob = scope.launch {
            prefs.observeAlert(Constants.KEY_ACTIVE_MONITORING, false)
                .collect { isEnabled -> runCatching { syncSession(isEnabled) } }
        }
    }

    private suspend fun syncSession(isEnabled: Boolean) {
        val intent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val source = intent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0
        val level = intent?.let { levelOf(it) } ?: -1

        if (isEnabled && isCharging(status, source)) {
            startNewSession(level.coerceAtLeast(0), source)
        } else {
            closeActiveSession(level)
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

    private suspend fun closeActiveSession(level: Int) {
        runCatching {
            sessionMutex.withLock {
                db.batteryDao().closeAllActiveSessions(System.currentTimeMillis(), level)
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

        if (initialized) {
            val level = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                ?.let { levelOf(it) } ?: lastLevel
            scope.launch { closeActiveSession(level) }.invokeOnCompletion { scope.coroutineContext.cancel() }
        } else {
            scope.cancel()
        }

        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_SERVICE) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }
        return if (initialized) START_STICKY else START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun recordScreenEvent(isOn: Boolean) {
        if (!prefs.getAlert(Constants.KEY_ACTIVE_MONITORING, false)) return

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
        const val ACTION_STOP_SERVICE = "STOP_BATTERY_MONITORING"
        private const val WIDGET_REFRESH_MS = 60_000L
    }
}
