package com.monospace.battery.helpers

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.monospace.battery.MainActivity
import com.monospace.battery.R

class BatteryAlertService : Service() {

    private lateinit var notificationHelper: NotificationHelper
    private lateinit var prefs: SharedPreferences
    
    private var lastLevel = -1
    private var lastTemp = -1

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
            val batteryPct = if (scale > 0) (level * 100 / scale) else -1
            
            val temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)
            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || 
                             status == BatteryManager.BATTERY_STATUS_FULL

            checkAlerts(batteryPct, temperature, isCharging)
        }
    }

    override fun onCreate() {
        super.onCreate()
        notificationHelper = NotificationHelper(this)
        prefs = SharedPreferences(this)
        
        // Start as foreground service to ensure it keeps running
        startForegroundService()
        
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(batteryReceiver, filter)
    }

    private fun startForegroundService() {
        val channelId = NotificationHelper.CHANNEL_ID
        
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
            startForeground(1, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(1, notification)
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

    private fun checkAlerts(level: Int, temperature: Int, isCharging: Boolean) {
        if (level == -1 || temperature == -1) return

        // 1. Healthy Charge (n%) - Only when charging and crossing the threshold
        val healthyEnabled = prefs.getBoolean(SharedPreferences.ALERTS_PREFS, SharedPreferences.KEY_HEALTHY_CHARGE)
        val healthyLevel = prefs.getInt(SharedPreferences.ALERTS_PREFS, SharedPreferences.KEY_HEALTHY_CHARGE_LEVEL, 80)
        
        if (healthyEnabled && isCharging && level >= healthyLevel && lastLevel != -1 && lastLevel < healthyLevel) {
            notificationHelper.showHealthyChargeNotification()
        }

        // 2. High Temperature (> 40°C) - Threshold crossing (temp is in tenths of degree)
        val tempEnabled = prefs.getBoolean(SharedPreferences.ALERTS_PREFS, SharedPreferences.KEY_TEMP_ALERT)
        val tempCelsius = temperature / 10
        if (tempEnabled && tempCelsius >= 40 && lastTemp != -1 && lastTemp < 40) {
            notificationHelper.showTempAlertNotification()
        }

        lastLevel = level
        lastTemp = tempCelsius
    }
}
