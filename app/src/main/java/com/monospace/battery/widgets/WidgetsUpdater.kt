package com.monospace.battery.widgets

import android.content.Context
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import com.monospace.battery.widgets.charging.BatteryLevelWidget
import com.monospace.battery.widgets.cycles.ChargeCyclesWidget
import com.monospace.battery.widgets.pager.BatteryPagerWidget

object WidgetsUpdater {

    val REFRESH_KEY = longPreferencesKey("refresh")

    suspend fun updateLive(context: Context) {
        refresh(context, BatteryLevelWidget())
        refresh(context, BatteryPagerWidget())
    }

    suspend fun updateAll(context: Context) {
        updateLive(context)
        refresh(context, ChargeCyclesWidget())
    }

    suspend fun refresh(context: Context, widget: GlanceAppWidget) {
        runCatching {
            GlanceAppWidgetManager(context).getGlanceIds(widget::class.java).forEach { id ->
                updateAppWidgetState(context, id) { it[REFRESH_KEY] = System.nanoTime() }
                widget.update(context, id)
            }
        }
    }
}
