package com.monospace.battery.service.alerts

import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import com.monospace.battery.core.constants.Constants
import com.monospace.battery.data.local.PreferenceManager
import com.monospace.battery.data.local.WidgetsUtils
import com.monospace.battery.ui.screens.AlwaysOnDisplayActivity

object AodLauncher {
    private const val TAG = "AodLauncher"

    fun launchIfNeeded(context: Context, trigger: String) {
        val aodEnabled = PreferenceManager(context).getAlert(Constants.KEY_AOD_ENABLED, false)
        if (!aodEnabled || !WidgetsUtils.isWidgetsPurchased(context)) return

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        if (powerManager.isInteractive || !Settings.canDrawOverlays(context)) return

        Log.d(TAG, "Launching AOD ($trigger)")
        val intent = Intent(context, AlwaysOnDisplayActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        runCatching { context.startActivity(intent) }
            .onFailure { Log.w(TAG, "AOD launch blocked", it) }
    }
}
