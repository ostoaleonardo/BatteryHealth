package com.monospace.battery.service.alerts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.monospace.battery.core.constants.Constants
import com.monospace.battery.data.local.PreferenceManager
import com.monospace.battery.data.local.WidgetsUtils
import com.monospace.battery.ui.screens.AlwaysOnDisplayActivity

class BatteryAlertReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED, QUICKBOOT_POWERON -> {
                startAlertService(context)
            }

            Intent.ACTION_POWER_CONNECTED -> {
                checkAndLaunchAod(context)
            }
        }
    }

    private fun startAlertService(context: Context) {
        val monitoringKeys = listOf(
            Constants.KEY_AOD_ENABLED,
            Constants.KEY_HEALTHY_CHARGE_ENABLED,
            Constants.KEY_LOW_BATTERY_ENABLED,
            Constants.KEY_TEMP_ALERT_ENABLED,
            Constants.KEY_FAST_DISCHARGE_ENABLED,
            Constants.KEY_SLOW_CHARGE_ENABLED,
            Constants.KEY_ACTIVE_MONITORING
        )

        val prefs = PreferenceManager(context)

        if (monitoringKeys.any { prefs.get(Constants.PREFS_ALERTS, it, false) }) {
            val serviceIntent = Intent(context, BatteryAlertService::class.java)

            runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            }.onFailure {
                Log.w(TAG, "Could not start alert service", it)
                runCatching { FirebaseCrashlytics.getInstance().recordException(it) }
            }
        }
    }

    private fun checkAndLaunchAod(context: Context) {
        val prefs = PreferenceManager(context)
        val aodEnabled = prefs.get(Constants.PREFS_ALERTS, Constants.KEY_AOD_ENABLED, false)
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

                    runCatching { context.startActivity(aodIntent) }
                        .onFailure { Log.w(TAG, "AOD launch blocked", it) }
                }
            }
        }
    }

    companion object {
        private const val TAG = "BatteryReceiver"
        private const val QUICKBOOT_POWERON = "android.intent.action.QUICKBOOT_POWERON"
    }
}
