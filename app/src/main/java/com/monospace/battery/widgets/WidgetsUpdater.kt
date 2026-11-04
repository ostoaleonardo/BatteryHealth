package com.monospace.battery.widgets

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import com.monospace.battery.widgets.charging.BatteryLevelWidget
import com.monospace.battery.widgets.charging.BatteryLevelWidgetReceiver
import com.monospace.battery.widgets.cycles.ChargeCyclesWidget
import com.monospace.battery.widgets.cycles.ChargeCyclesWidgetReceiver
import com.monospace.battery.widgets.pager.BatteryPagerWidget
import com.monospace.battery.widgets.pager.BatteryPagerWidgetReceiver

object WidgetsUpdater {

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
        runCatching {
            val manager = GlanceAppWidgetManager(context)
            val appWidgetIds = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(ComponentName(context, receiver))

            appWidgetIds.forEach { appWidgetId ->
                val id = manager.getGlanceIdBy(appWidgetId)
                updateAppWidgetState(context, id) { it[REFRESH_KEY] = System.nanoTime() }
                widget.update(context, id)
            }
        }
    }
}
