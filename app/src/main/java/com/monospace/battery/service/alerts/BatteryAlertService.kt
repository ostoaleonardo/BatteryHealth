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
import com.monospace.battery.core.constants.AppConstants
import com.monospace.battery.data.local.PreferenceManager
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
    
    private var lastLevel = -1
    private var lastTemp = -1
    private var lastStatus = -1

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            processBatteryIntent(intent)
        }
    }

    private fun processBatteryIntent(intent: Intent) {
        Log.d("BatteryAlertService", "Intent received: ${intent.action}")
        when (intent.action) {
            Intent.ACTION_BATTERY_CHANGED -> {
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
                val batteryPct = if (scale > 0) (level * 100 / scale) else -1
                
                val temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)
                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                val source = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
                val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || 
                                 status == BatteryManager.BATTERY_STATUS_FULL

                checkAlerts(batteryPct, temperature, isCharging)
                recordBatteryData(batteryPct, status, source)
            }
            Intent.ACTION_SCREEN_ON -> recordScreenEvent(true)
            Intent.ACTION_SCREEN_OFF -> recordScreenEvent(false)
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("BatteryAlertService", "Service created")
        notificationHelper = NotificationHelper(this)
        prefs = PreferenceManager(this)
        db = BatteryDatabase.getDatabase(this)
        
        // Start as foreground service to ensure it keeps running
        startForegroundService()
        
        cleanUpActiveSessions()
        
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        val initialIntent = registerReceiver(batteryReceiver, filter)
        initialIntent?.let { processBatteryIntent(it) }
    }

    private fun cleanUpActiveSessions() {
        serviceScope.launch {
            val activeSession = db.batteryDao().getActiveSession()
            if (activeSession != null) {
                // Check if device is currently charging
                val intent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
                val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || 
                                 status == BatteryManager.BATTERY_STATUS_FULL
                
                if (!isCharging) {
                    // Not charging, but we have an active session. Close it.
                    val currentLevel = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, activeSession.startLevel) ?: activeSession.startLevel
                    db.batteryDao().updateChargeSession(
                        activeSession.copy(
                            endTime = System.currentTimeMillis(),
                            endLevel = currentLevel
                        )
                    )
                }
            }
        }
    }

    private fun startForegroundService() {
        val channelId = AppConstants.NOTIFICATION_CHANNEL_ID

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
            startForeground(AppConstants.NOTIFICATION_SERVICE_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(AppConstants.NOTIFICATION_SERVICE_ID, notification)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(batteryReceiver)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun recordBatteryData(level: Int, status: Int, source: Int) {
        Log.d("BatteryAlertService", "recordBatteryData check: level=$level, lastLevel=$lastLevel")
        if (level == -1) return
        
        serviceScope.launch {
            try {
                // Record History Entry if level changed or if it's the very first time
                if (level != lastLevel || lastLevel == -1) {
                    Log.d("BatteryAlertService", "DB INSERT: level=$level")
                    db.batteryDao().insertBatteryEntry(
                        BatteryHistoryEntry(timestamp = System.currentTimeMillis(), level = level)
                    )
                    lastLevel = level
                }

                // Handle Charge Session
                val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || 
                                 status == BatteryManager.BATTERY_STATUS_FULL
                val wasCharging = lastStatus == BatteryManager.BATTERY_STATUS_CHARGING || 
                                  lastStatus == BatteryManager.BATTERY_STATUS_FULL

                if (isCharging && !wasCharging) {
                    Log.d("BatteryAlertService", "SESSION START: level=$level")
                    db.batteryDao().insertChargeSession(
                        ChargeSession(
                            startTime = System.currentTimeMillis(),
                            startLevel = level,
                            chargeSource = source
                        )
                    )
                } else if (!isCharging && wasCharging) {
                    Log.d("BatteryAlertService", "SESSION END: level=$level")
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
                lastStatus = status
            } catch (e: Exception) {
                Log.e("BatteryAlertService", "Database error", e)
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

    private fun checkAlerts(level: Int, temperature: Int, isCharging: Boolean) {
        if (level == -1 || temperature == -1) return

        // 1. Healthy Charge (n%) - Only when charging and crossing the threshold
        val healthyEnabled = prefs.getBoolean(AppConstants.PREFS_ALERTS, AppConstants.KEY_HEALTHY_CHARGE_ENABLED)
        val healthyLevel = prefs.getInt(AppConstants.PREFS_ALERTS, AppConstants.KEY_HEALTHY_CHARGE_LEVEL, 80)

        if (healthyEnabled && isCharging && level >= healthyLevel && lastLevel != -1 && lastLevel < healthyLevel) {
            notificationHelper.showHealthyChargeNotification()
        }

        // 2. Low Battery (n%) - Only when NOT charging and dropping below the threshold
        val lowEnabled = prefs.getBoolean(AppConstants.PREFS_ALERTS, AppConstants.KEY_LOW_BATTERY_ENABLED)
        val lowLevel = prefs.getInt(AppConstants.PREFS_ALERTS, AppConstants.KEY_LOW_BATTERY_LEVEL, 20)

        if (lowEnabled && !isCharging && level <= lowLevel && lastLevel != -1 && lastLevel > lowLevel) {
            notificationHelper.showLowBatteryNotification(lowLevel)
        }

        // 3. High Temperature (> 40°C) - Threshold crossing (temp is in tenths of degree)
        val tempEnabled = prefs.getBoolean(AppConstants.PREFS_ALERTS, AppConstants.KEY_TEMP_ALERT_ENABLED)
        val tempCelsius = temperature / 10
        if (tempEnabled && tempCelsius >= 40 && lastTemp != -1 && lastTemp < 40) {
            notificationHelper.showTempAlertNotification()
        }

        lastTemp = tempCelsius
    }
}
