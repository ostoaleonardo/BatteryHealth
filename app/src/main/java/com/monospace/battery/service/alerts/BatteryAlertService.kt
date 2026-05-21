package com.monospace.battery.service.alerts

import android.app.Notification
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
import android.util.Log
import androidx.core.app.NotificationCompat
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BatteryAlertService : Service() {

    private lateinit var notificationHelper: NotificationHelper
    private lateinit var prefs: PreferenceManager
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var db: BatteryDatabase
    private lateinit var batteryUtils: BatteryUtils

    private var lastLevel = -1
    private var lastTemp = -1
    private var lastStatus = -1

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            processBatteryIntent(intent)
        }
    }

    private fun processBatteryIntent(intent: Intent) {
        if (intent.action != Intent.ACTION_BATTERY_CHANGED) {
            when (intent.action) {
                Intent.ACTION_SCREEN_ON -> recordScreenEvent(true)
                Intent.ACTION_SCREEN_OFF -> recordScreenEvent(false)
            }
            return
        }

        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
        val batteryPct = if (scale > 0) (level * 100 / scale) else -1

        val temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val source = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
        val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)

        // A session is valid ONLY if it reports charging AND has a physical source
        // This avoids creating ghost sessions with source "None" when disconnecting
        val isCharging = (status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL) && source != 0

        val wasCharging = lastStatus == BatteryManager.BATTERY_STATUS_CHARGING ||
                lastStatus == BatteryManager.BATTERY_STATUS_FULL

        checkAlerts(batteryPct, temperature, isCharging, voltage)

        // Capture previous state before updating to avoid race conditions in the coroutine
        val prevLevel = lastLevel
        recordBatteryData(batteryPct, prevLevel, temperature, isCharging, wasCharging, source)

        // Update states immediately on main thread
        lastLevel = batteryPct
        lastStatus = status
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")

        runCatching {
            notificationHelper = NotificationHelper(this)
            prefs = PreferenceManager(this)
            db = BatteryDatabase.getDatabase(this)
            batteryUtils = BatteryUtils(this)

            startForegroundService()
            cleanUpActiveSessions()

            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_BATTERY_CHANGED)
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_SCREEN_OFF)
            }

            val initialIntent = registerReceiver(batteryReceiver, filter)
            initialIntent?.let { processBatteryIntent(it) }
        }.onFailure { e ->
            Log.e(TAG, "Failed to initialize service", e)
            stopSelf()
        }
    }

    private fun cleanUpActiveSessions() {
        serviceScope.launch {
            runCatching {
                val activeSession = db.batteryDao().getActiveSession()

                if (activeSession != null) {
                    val intent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                    val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
                    val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                            status == BatteryManager.BATTERY_STATUS_FULL

                    if (!isCharging) {
                        val currentLevel = intent?.getIntExtra(
                            BatteryManager.EXTRA_LEVEL,
                            activeSession.startLevel
                        ) ?: activeSession.startLevel

                        db.batteryDao().updateChargeSession(
                            activeSession.copy(
                                endTime = System.currentTimeMillis(),
                                endLevel = currentLevel
                            )
                        )
                    }
                }
            }.onFailure { e ->
                Log.e(TAG, "Error cleaning up sessions", e)
            }
        }
    }

    private fun startForegroundService() {
        val channelId = Constants.NOTIFICATION_CHANNEL_ID

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.notification_service_running))
            .setSmallIcon(R.drawable.bolt)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
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
        super.onDestroy()
        unregisterReceiver(batteryReceiver)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onBind(intent: Intent?): IBinder? = null

    private fun recordBatteryData(
        level: Int,
        prevLevel: Int,
        temperature: Int,
        isCharging: Boolean,
        wasCharging: Boolean,
        source: Int
    ) {
        if (level == -1) return

        serviceScope.launch {
            runCatching {
                // 1. History Entry
                if (level != prevLevel || prevLevel == -1) {
                    db.batteryDao().insertBatteryEntry(
                        BatteryHistoryEntry(
                            timestamp = System.currentTimeMillis(),
                            level = level,
                            temperature = temperature
                        )
                    )
                }

                // 2. Charge Session Logic
                if (isCharging && !wasCharging) {
                    Log.d(TAG, "DB: Session Started (Source: $source)")

                    db.batteryDao().insertChargeSession(
                        ChargeSession(
                            startTime = System.currentTimeMillis(),
                            startLevel = level,
                            chargeSource = source
                        )
                    )
                } else if (!isCharging && wasCharging) {
                    Log.d(TAG, "DB: Session Ended")
                    val activeSession = db.batteryDao().getActiveSession()

                    activeSession?.let {
                        db.batteryDao().updateChargeSession(
                            it.copy(
                                endTime = System.currentTimeMillis(),
                                endLevel = level
                            )
                        )
                    }
                }
            }.onFailure { e ->
                Log.e(TAG, "Database error", e)
            }
        }
    }

    private fun recordScreenEvent(isOn: Boolean) {
        serviceScope.launch {
            db.batteryDao().insertScreenEvent(
                ScreenEvent(
                    timestamp = System.currentTimeMillis(),
                    isScreenOn = isOn,
                    batteryLevel = lastLevel
                )
            )
        }
    }

    private fun checkAlerts(level: Int, temperature: Int, isCharging: Boolean, voltage: Int) {
        if (level == -1 || temperature == -1) return
        if (!WidgetsUtils.isWidgetsPurchased(this)) return

        checkHealthyChargeAlert(level, isCharging)
        checkLowBatteryAlert(level, isCharging)
        checkHighTemperatureAlert(temperature)
        checkSlowChargeAlert(isCharging, voltage)
        checkFastDischargeAlert(level, isCharging)
    }

    private fun checkHealthyChargeAlert(level: Int, isCharging: Boolean) {
        val enabled = prefs.getBoolean(Constants.PREFS_ALERTS, Constants.KEY_HEALTHY_CHARGE_ENABLED)
        val threshold = prefs.getInt(Constants.PREFS_ALERTS, Constants.KEY_HEALTHY_CHARGE_LEVEL, 80)

        if (enabled && isCharging && level >= threshold && lastLevel != -1 && lastLevel < threshold) {
            notificationHelper.showHealthyChargeNotification()
        }
    }

    private fun checkLowBatteryAlert(level: Int, isCharging: Boolean) {
        val enabled = prefs.getBoolean(Constants.PREFS_ALERTS, Constants.KEY_LOW_BATTERY_ENABLED)
        val threshold = prefs.getInt(Constants.PREFS_ALERTS, Constants.KEY_LOW_BATTERY_LEVEL, 20)

        if (enabled && !isCharging && level <= threshold && lastLevel != -1 && lastLevel > threshold) {
            notificationHelper.showLowBatteryNotification(threshold)
        }
    }

    private fun checkHighTemperatureAlert(temperature: Int) {
        val enabled = prefs.getBoolean(Constants.PREFS_ALERTS, Constants.KEY_TEMP_ALERT_ENABLED)
        val tempCelsius = temperature / 10

        if (enabled && tempCelsius >= 40 && lastTemp != -1 && lastTemp < 40) {
            notificationHelper.showTempAlertNotification()
        }

        lastTemp = tempCelsius
    }

    private fun checkSlowChargeAlert(isCharging: Boolean, voltage: Int) {
        val enabled = prefs.getBoolean(Constants.PREFS_ALERTS, Constants.KEY_SLOW_CHARGE_ENABLED)

        if (enabled && isCharging && lastStatus != BatteryManager.BATTERY_STATUS_CHARGING) {
            serviceScope.launch {
                val speed = batteryUtils.getChargeSpeed(voltage, true)
                if (speed > 0 && speed < 2.0) {
                    notificationHelper.showSlowChargeNotification()
                }
            }
        }
    }

    private fun checkFastDischargeAlert(level: Int, isCharging: Boolean) {
        val enabled = prefs.getBoolean(Constants.PREFS_ALERTS, Constants.KEY_FAST_DISCHARGE_ENABLED)

        if (enabled && !isCharging && lastLevel != -1 && level < lastLevel) {
            serviceScope.launch {
                val oneHourAgo = System.currentTimeMillis() - 3600000
                val history = db.batteryDao().getHistorySinceSync(oneHourAgo)
                if (history.size >= 2) {
                    val drop = history.first().level - level
                    if (drop >= 15) {
                        notificationHelper.showFastDischargeNotification()
                    }
                }
            }
        }
    }

    companion object {
        const val TAG = "BatteryAlertService"
    }
}
