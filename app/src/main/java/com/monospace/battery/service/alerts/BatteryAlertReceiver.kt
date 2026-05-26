package com.monospace.battery.service.alerts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import com.monospace.battery.core.constants.Constants
import com.monospace.battery.data.local.PreferenceManager
import com.monospace.battery.data.local.WidgetsUtils
import com.monospace.battery.ui.screens.AlwaysOnDisplayActivity

class BatteryAlertReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_LOCKED_BOOT_COMPLETED -> {
                startAlertService(context)
            }

            Intent.ACTION_POWER_CONNECTED -> {
                checkAndLaunchAod(context)
            }
        }
    }

    private fun startAlertService(context: Context) {
        val serviceIntent = Intent(context, BatteryAlertService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }

    private fun checkAndLaunchAod(context: Context) {
        val prefs = PreferenceManager(context)
        val aodEnabled = prefs.getBoolean(Constants.PREFS_ALERTS, Constants.KEY_AOD_ENABLED)
        val isPremium = WidgetsUtils.isWidgetsPurchased(context)

        if (aodEnabled && isPremium) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

            if (!powerManager.isInteractive) {
                if (Settings.canDrawOverlays(context)) {
                    Log.d(TAG, "Launching AOD (Power Connected)")

                    val aodIntent = Intent(context, AlwaysOnDisplayActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    }

                    context.startActivity(aodIntent)
                }
            }
        }
    }

    companion object {
        private const val TAG = "BatteryReceiver"
    }
}
