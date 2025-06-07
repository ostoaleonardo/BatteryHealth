package com.monospace.battery.helpers

import android.content.Context

object WidgetsUtils {
    fun isWidgetsPurchased(context: Context): Boolean {
        return SharedPreferences(context).getItem(
            SharedPreferences.WIDGETS_FILE,
            SharedPreferences.WIDGETS_PURCHASED
        ) == "true"
    }
}
