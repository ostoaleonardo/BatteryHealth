package com.monospace.battery.helpers

import android.content.Context
import android.util.Base64

object WidgetsUtils {
    private const val WIDGETS_FILE = "widgets_purchase"
    private const val WIDGETS_PURCHASED = "purchase_token"
    private const val LAST_CHECK_KEY = "last_check_timestamp"

    fun isWidgetsPurchased(context: Context): Boolean {
        val prefs = SharedPreferences(context)
        val token = prefs.getString(WIDGETS_FILE, WIDGETS_PURCHASED) ?: return false

        return try {
            val expectedToken = Base64.encodeToString(
                context.packageName.toByteArray(),
                Base64.NO_WRAP
            )

            token == expectedToken
        } catch (_: Exception) {
            false
        }
    }

    fun setWidgetsPurchased(context: Context, purchased: Boolean) {
        val prefs = SharedPreferences(context)

        if (purchased) {
            val token = Base64.encodeToString(
                context.packageName.toByteArray(),
                Base64.NO_WRAP
            )

            prefs.setString(WIDGETS_FILE, WIDGETS_PURCHASED, token)
            prefs.setLong(WIDGETS_FILE, LAST_CHECK_KEY, System.currentTimeMillis())
        } else {
            prefs.remove(WIDGETS_FILE, WIDGETS_PURCHASED)
            prefs.remove(WIDGETS_FILE, LAST_CHECK_KEY)
        }
    }
}
