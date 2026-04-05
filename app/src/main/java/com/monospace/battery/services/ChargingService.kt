package com.monospace.battery.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.monospace.battery.R
import com.monospace.battery.helpers.BatteryInfo
import com.monospace.battery.helpers.BatteryUtils

class ChargingService : Service() {

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_BATTERY_CHANGED) {
                updateNotification(intent)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(batteryReceiver, filter)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")
        val channelId = createNotificationChannel()

        // Initial notification to start as foreground
        val notification = createNotification(
            channelId,
            "Initializing...",
            "Fetching battery data...",
            "Fetching battery data..."
        )

        startForeground(NOTIFICATION_ID, notification)
        return START_STICKY
    }

    private fun updateNotification(intent: Intent) {
        val batteryInfo = BatteryInfo(intent)
        val batteryUtils = BatteryUtils(this)

        val speed = batteryUtils.getChargeSpeed(intent, true)
        val temp = batteryInfo.temperature / 10.0
        val remaining = batteryUtils.getChargeTimeRemaining(true)
        val level = batteryInfo.level

        val title = getString(R.string.notification_charging_title)

        // Small notification: 🔋 85%  •  ⚡ 15.4 W  •  🌡️ 31.0 °C  •  ⏳ 01:20
        val smallMain = getString(R.string.notification_charging_info_format, level, speed)
        val smallExtra = getString(R.string.notification_charging_extra_format, temp)
        val smallTime = if (remaining != "00:00") {
            "  •  " + getString(R.string.notification_charging_time_format, remaining)
        } else {
            ""
        }

        val collapsedText = "$smallMain  •  $smallExtra$smallTime"

        /*  Big notification:

            🔋 Battery: 85%
            ⚡ Speed: 15.4 W
            🌡️ Temperature: 31.0 °C
            ⏳ Time remaining: 01:20
        */
        val bigBattery = getString(R.string.notification_big_battery, level)
        val bigSpeed = getString(R.string.notification_big_speed, speed)
        val bigTemp = getString(R.string.notification_big_temp, temp)
        val bigTime = if (remaining != "00:00") {
            "\n" + getString(R.string.notification_big_time, remaining)
        } else {
            ""
        }

        val bigText = "$bigBattery\n$bigSpeed\n$bigTemp$bigTime"

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(
            NOTIFICATION_ID,
            createNotification(CHANNEL_ID, title, collapsedText, bigText)
        )

        Log.d(TAG, "Notification updated: $bigText")
    }

    private fun createNotification(
        channelId: String,
        title: String,
        content: String,
        bigText: String
    ): Notification {
        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_charging)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_LOW) // LOW to avoid sound on every update
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun createNotificationChannel(): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Charging Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows real-time battery charging information"
                setSound(null, null)
                enableVibration(false)
            }

            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
        return CHANNEL_ID
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
        unregisterReceiver(batteryReceiver)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val TAG = "ChargingService"
        private const val NOTIFICATION_ID = 2002
        private const val CHANNEL_ID = "charging_realtime_channel"
    }
}