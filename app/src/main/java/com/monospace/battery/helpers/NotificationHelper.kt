package com.monospace.battery.helpers

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.monospace.battery.R

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "battery_alerts_channel"
        const val HEALTHY_CHARGE_ID = 1001
        const val LOW_BATTERY_ID = 1002
        const val TEMP_ALERT_ID = 1003
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.notification_channel_name)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showHealthyChargeNotification() {
        showNotification(
            HEALTHY_CHARGE_ID,
            context.getString(R.string.notification_healthy_title),
            context.getString(R.string.notification_healthy_message)
        )
    }

    fun showLowBatteryNotification(level: Int) {
        showNotification(
            LOW_BATTERY_ID,
            context.getString(R.string.notification_low_battery_title),
            context.getString(R.string.notification_low_battery_message, level)
        )
    }

    fun showTempAlertNotification() {
        showNotification(
            TEMP_ALERT_ID,
            context.getString(R.string.notification_temp_title),
            context.getString(R.string.notification_temp_message)
        )
    }

    private fun showNotification(id: Int, title: String, message: String) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.bolt) // Use bolt as a generic battery icon
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val notificationManager = NotificationManagerCompat.from(context)
        var hasPermission = true

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasPermission = ActivityCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        }

        if (hasPermission) {
            notificationManager.notify(id, builder.build())
        }
    }
}
