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
import android.util.Log
import androidx.core.app.NotificationCompat
import com.monospace.battery.MainActivity
import com.monospace.battery.R
import com.monospace.battery.core.constants.Constants
import com.monospace.battery.core.utils.BatteryUtils
import com.monospace.battery.data.local.PreferenceManager
import com.monospace.battery.data.local.WidgetsUtils
import com.monospace.battery.data.local.db.BatteryDatabase
import com.monospace.battery.data.models.ScreenEvent
import com.monospace.battery.notifications.NotificationHelper
import com.monospace.battery.ui.screens.AlwaysOnDisplayActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BatteryAlertService : Service() {

    private lateinit var notification: NotificationHelper
    private lateinit var prefs: PreferenceManager
    private lateinit var db: BatteryDatabase
    private lateinit var batteryUtils: BatteryUtils
    private lateinit var alertHandler: BatteryAlertHandler

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var lastLevel = -1
    private var lastStatus = -1

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            processBatteryIntent(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()

        runCatching {
            initDependencies()
            startForegroundService()
            observeMonitoringToggle()
            setupBatteryReceiver()
            recordInitialScreenState()
        }.onFailure { e ->
            Log.e(TAG, "Failed to initialize service", e)
            stopSelf()
        }
    }

    private fun initDependencies() {
        prefs = PreferenceManager(this)
        batteryUtils = BatteryUtils(this)
        db = BatteryDatabase.getDatabase(this)
        notification = NotificationHelper(this)
        alertHandler = BatteryAlertHandler(this, scope, prefs, notification, batteryUtils, db)
    }

    private fun setupBatteryReceiver() {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }

        val initialIntent = registerReceiver(batteryReceiver, filter)

        // Initialize state from first sticky intent
        initialIntent?.let { intent ->
            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100)

            lastLevel = if (scale > 0) (level * 100 / scale) else -1
            lastStatus = status

            processBatteryIntent(intent)
        }
    }

    private fun recordInitialScreenState() {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        recordScreenEvent(powerManager.isInteractive)
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
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
        val batteryPct = if (scale > 0) (level * 100 / scale) else -1

        val temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val source = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
        val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)

        val isCharging = (status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL) && source != 0

        val wasCharging = lastStatus == BatteryManager.BATTERY_STATUS_CHARGING ||
                lastStatus == BatteryManager.BATTERY_STATUS_FULL

        // 1. Check Alarms
        alertHandler.checkAlerts(batteryPct, temperature, isCharging, voltage, lastStatus)

        // 2. Session Management
        manageChargeSession(isCharging, wasCharging, batteryPct, source)

        // 3. AOD Management
        if (isCharging && !wasCharging) {
            checkAodStart("CHARGE_STARTED")
        }

        // 4. Data Recording (History)
        val isMonitoring = prefs.getAlert(Constants.KEY_ACTIVE_MONITORING, false)
        if (isMonitoring && (batteryPct != lastLevel || isCharging != wasCharging)) {
            BatteryWorker.enqueue(this, batteryPct, temperature)
        }

        lastLevel = batteryPct
        lastStatus = status
    }

    private fun manageChargeSession(
        isCharging: Boolean,
        wasCharging: Boolean,
        level: Int,
        source: Int
    ) {
        val isMonitoring = prefs.getAlert(Constants.KEY_ACTIVE_MONITORING, false)
        if (!isMonitoring) return

        if (isCharging && !wasCharging) {
            startNewSession(level, source)
        } else if (!isCharging && wasCharging) {
            closeActiveSession()
        }
    }

    private fun checkAodStart(trigger: String) {
        val aodEnabled = prefs.getAlert(Constants.KEY_AOD_ENABLED, false)
        val isPremium = WidgetsUtils.isWidgetsPurchased(this)

        if (aodEnabled && isPremium && lastStatus == BatteryManager.BATTERY_STATUS_CHARGING) {
            val powerManager = getSystemService(POWER_SERVICE) as PowerManager

            if (!powerManager.isInteractive && android.provider.Settings.canDrawOverlays(this)) {
                Log.d(TAG, "Launching AOD ($trigger)")
                val intent = Intent(this, AlwaysOnDisplayActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                }
                startActivity(intent)
            }
        }
    }

    private fun observeMonitoringToggle() {
        scope.launch {
            prefs.observeAlert(Constants.KEY_ACTIVE_MONITORING, false)
                .collect { isEnabled -> handleSessionToggle(isEnabled) }
        }
    }

    private fun handleSessionToggle(isEnabled: Boolean) {
        scope.launch {
            runCatching {
                val activeSession = db.batteryDao().getActiveSession()
                val intent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
                val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL
                val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
                val source = intent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0

                if (isEnabled && isCharging && activeSession == null) {
                    startNewSession(if (level != -1) level else 0, source)
                } else if (!isEnabled && activeSession != null) {
                    closeActiveSession()
                }
            }
        }
    }

    private fun startNewSession(level: Int, source: Int) {
        scope.launch {
            db.batteryDao().insertChargeSession(
                com.monospace.battery.data.models.ChargeSession(
                    startTime = System.currentTimeMillis(),
                    startLevel = level,
                    chargeSource = source
                )
            )
        }
    }

    private fun closeActiveSession() {
        scope.launch {
            runCatching {
                val activeSession = db.batteryDao().getActiveSession()
                if (activeSession != null) {
                    val intent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                    val level =
                        intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, activeSession.startLevel)
                            ?: activeSession.startLevel

                    db.batteryDao().updateChargeSession(
                        activeSession.copy(
                            endTime = System.currentTimeMillis(),
                            endLevel = level
                        )
                    )
                }
            }
        }
    }

    private fun startForegroundService() {
        val channelId = Constants.NOTIFICATION_CHANNEL_ID
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent =
            PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        val stopIntent = Intent(this, BatteryAlertService::class.java).apply {
            action = ACTION_STOP_SERVICE
        }

        val stopPendingIntent =
            PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.notification_service_running))
            .setSmallIcon(R.drawable.bolt)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(
                R.drawable.bolt,
                getString(R.string.notification_action_stop),
                stopPendingIntent
            )
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
    }

    override fun onDestroy() {
        closeActiveSession()
        super.onDestroy()
        unregisterReceiver(batteryReceiver)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_SERVICE) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun recordScreenEvent(isOn: Boolean) {
        val isMonitoring = prefs.getAlert(Constants.KEY_ACTIVE_MONITORING, false)
        if (!isMonitoring) return

        scope.launch {
            db.batteryDao().insertScreenEvent(
                ScreenEvent(
                    timestamp = System.currentTimeMillis(),
                    isScreenOn = isOn,
                    batteryLevel = lastLevel
                )
            )
        }
    }

    companion object {
        const val TAG = "BatteryAlertService"
        const val ACTION_STOP_SERVICE = "STOP_BATTERY_MONITORING"
    }
}
