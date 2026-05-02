package com.monospace.battery.data.local

import android.content.Context
import android.util.Base64

import com.monospace.battery.core.constants.AppConstants

object WidgetsUtils {

    fun isWidgetsPurchased(context: Context): Boolean {
        val prefs = PreferenceManager(context)
        val token = prefs.getString(AppConstants.PREFS_WIDGETS, AppConstants.KEY_WIDGETS_PURCHASE_TOKEN) ?: return false

        return runCatching {
            val expectedToken = Base64.encodeToString(
                context.packageName.toByteArray(),
                Base64.NO_WRAP
            )

            token == expectedToken
        }.getOrDefault(false)
    }

    fun setWidgetsPurchased(context: Context, purchased: Boolean) {
        val prefs = PreferenceManager(context)

        if (purchased) {
            val token = Base64.encodeToString(
                context.packageName.toByteArray(),
                Base64.NO_WRAP
            )

            prefs.setString(AppConstants.PREFS_WIDGETS, AppConstants.KEY_WIDGETS_PURCHASE_TOKEN, token)
            prefs.setLong(AppConstants.PREFS_WIDGETS, AppConstants.KEY_WIDGETS_LAST_CHECK, System.currentTimeMillis())
        } else {
            prefs.remove(AppConstants.PREFS_WIDGETS, AppConstants.KEY_WIDGETS_PURCHASE_TOKEN)
            prefs.remove(AppConstants.PREFS_WIDGETS, AppConstants.KEY_WIDGETS_LAST_CHECK)
        }
    }
}
