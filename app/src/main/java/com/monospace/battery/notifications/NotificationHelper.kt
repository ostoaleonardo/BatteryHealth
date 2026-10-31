package com.monospace.battery.notifications

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.PendingIntent
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.monospace.battery.MainActivity
import com.monospace.battery.R
import com.monospace.battery.core.constants.Constants

class NotificationHelper(private val context: Context) {

    private val permissionManager = PermissionManager(context)

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() = createChannels(context)

    fun showHealthyChargeNotification() {
        showNotification(
            Constants.NOTIFICATION_HEALTHY_CHARGE_ID,
            context.getString(R.string.notification_healthy_title),
            context.getString(R.string.notification_healthy_message)
        )
    }

    fun showLowBatteryNotification(level: Int) {
        showNotification(
            Constants.NOTIFICATION_LOW_BATTERY_ID,
            context.getString(R.string.notification_low_battery_title),
            context.getString(R.string.notification_low_battery_message, level)
        )
    }

    fun showTempAlertNotification() {
        showNotification(
            Constants.NOTIFICATION_TEMP_ALERT_ID,
            context.getString(R.string.notification_temp_title),
            context.getString(R.string.notification_temp_message)
        )
    }

    fun showFastDischargeNotification() {
        showNotification(
            Constants.NOTIFICATION_FAST_DISCHARGE_ID,
            context.getString(R.string.notification_fast_discharge_title),
            context.getString(R.string.notification_fast_discharge_message)
        )
    }

    fun showSlowChargeNotification() {
        showNotification(
            Constants.NOTIFICATION_SLOW_CHARGE_ID,
            context.getString(R.string.notification_slow_charge_title),
            context.getString(R.string.notification_slow_charge_message)
        )
    }

    @SuppressLint("MissingPermission")
    private fun showNotification(id: Int, title: String, message: String) {
        val builder = NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.bolt) // Use bolt as a generic battery icon
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setContentIntent(
                PendingIntent.getActivity(
                    context,
                    id,
                    Intent(context, MainActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
            .setAutoCancel(true)

        val notificationManager = NotificationManagerCompat.from(context)

        if (permissionManager.hasNotificationPermission()) {
            runCatching {
                notificationManager.notify(id, builder.build())
            }
        }
    }

    companion object {
        fun createChannels(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            manager.createNotificationChannel(
                NotificationChannel(
                    Constants.NOTIFICATION_CHANNEL_ID,
                    context.getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_HIGH
                )
            )
            manager.createNotificationChannel(
                NotificationChannel(
                    Constants.SERVICE_CHANNEL_ID,
                    context.getString(R.string.notification_service_channel_name),
                    NotificationManager.IMPORTANCE_LOW
                )
            )
        }
    }
}
