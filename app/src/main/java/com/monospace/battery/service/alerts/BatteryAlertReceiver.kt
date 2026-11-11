package com.monospace.battery.service.alerts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.monospace.battery.core.constants.Constants
import com.monospace.battery.data.local.PreferenceManager
import com.monospace.battery.data.local.WidgetsUtils

class BatteryAlertReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            QUICKBOOT_POWERON -> startAlertService(context)

            Intent.ACTION_POWER_CONNECTED -> AodLauncher.launchIfNeeded(context, "POWER_CONNECTED")
        }
    }

    private fun startAlertService(context: Context) {
        val alertKeys = listOf(
            Constants.KEY_AOD_ENABLED,
            Constants.KEY_HEALTHY_CHARGE_ENABLED,
            Constants.KEY_LOW_BATTERY_ENABLED,
            Constants.KEY_TEMP_ALERT_ENABLED,
            Constants.KEY_FAST_DISCHARGE_ENABLED,
            Constants.KEY_SLOW_CHARGE_ENABLED
        )

        val prefs = PreferenceManager(context)
        val alertsActive = WidgetsUtils.isWidgetsPurchased(context) &&
                alertKeys.any { prefs.getAlert(it, false) }

        if (alertsActive || prefs.getAlert(Constants.KEY_ACTIVE_MONITORING, false)) {
            runCatching { BatteryAlertService.start(context) }.onFailure {
                Log.w(TAG, "Could not start alert service", it)
                runCatching { FirebaseCrashlytics.getInstance().recordException(it) }
            }
        }
    }

    companion object {
        private const val TAG = "BatteryReceiver"
        private const val QUICKBOOT_POWERON = "android.intent.action.QUICKBOOT_POWERON"
    }
}
