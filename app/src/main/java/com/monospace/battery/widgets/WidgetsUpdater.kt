package com.monospace.battery.widgets

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import com.monospace.battery.core.utils.CrashReporter
import com.monospace.battery.widgets.charging.BatteryLevelWidget
import com.monospace.battery.widgets.charging.BatteryLevelWidgetReceiver
import com.monospace.battery.widgets.cycles.ChargeCyclesWidget
import com.monospace.battery.widgets.cycles.ChargeCyclesWidgetReceiver
import com.monospace.battery.widgets.pager.BatteryPagerWidget
import com.monospace.battery.widgets.pager.BatteryPagerWidgetReceiver

object WidgetsUpdater {

    private const val TAG = "WidgetsUpdater"

    val REFRESH_KEY = longPreferencesKey("refresh")

    suspend fun updateLive(context: Context) {
        refresh(context, BatteryLevelWidgetReceiver::class.java, BatteryLevelWidget())
        refresh(context, BatteryPagerWidgetReceiver::class.java, BatteryPagerWidget())
    }

    suspend fun updateAll(context: Context) {
        updateLive(context)
        refresh(context, ChargeCyclesWidgetReceiver::class.java, ChargeCyclesWidget())
    }

    suspend fun refresh(context: Context, receiver: Class<*>, widget: GlanceAppWidget) {
        val name = widget::class.java.simpleName

        runCatching {
            val manager = GlanceAppWidgetManager(context)
            val appWidgetIds = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(ComponentName(context, receiver))

            CrashReporter.key("widgets_${name}_count", appWidgetIds.size)

            appWidgetIds.forEach { appWidgetId ->
                runCatching {
                    val id = manager.getGlanceIdBy(appWidgetId)
                    updateAppWidgetState(context, id) { it[REFRESH_KEY] = System.nanoTime() }
                    widget.update(context, id)
                }.onFailure {
                    CrashReporter.record(
                        TAG,
                        "Widget refresh failed",
                        it,
                        "widget" to name,
                        "appWidgetId" to appWidgetId
                    )
                }
            }
        }.onFailure {
            CrashReporter.record(TAG, "Widget lookup failed: $name", it)
        }
    }
}
