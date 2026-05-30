package com.monospace.battery.data.local

import android.content.Context
import android.util.Base64

import com.monospace.battery.core.constants.Constants

object WidgetsUtils {

    fun isWidgetsPurchased(context: Context): Boolean {
        val prefs = PreferenceManager(context)
        val token = prefs.get(Constants.PREFS_WIDGETS, Constants.KEY_WIDGETS_PURCHASE_TOKEN, "")

        if (token.isEmpty()) return false

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

            prefs.set(Constants.PREFS_WIDGETS, Constants.KEY_WIDGETS_PURCHASE_TOKEN, token)
            prefs.set(Constants.PREFS_WIDGETS, Constants.KEY_WIDGETS_LAST_CHECK, System.currentTimeMillis())
        } else {
            prefs.remove(Constants.PREFS_WIDGETS, Constants.KEY_WIDGETS_PURCHASE_TOKEN)
            prefs.remove(Constants.PREFS_WIDGETS, Constants.KEY_WIDGETS_LAST_CHECK)
        }
    }
}
