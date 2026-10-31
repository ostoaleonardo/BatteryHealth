package com.monospace.battery.widgets

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.monospace.battery.widgets.charging.BatteryLevelWidget
import com.monospace.battery.widgets.charging.ChargingInfoWidget
import com.monospace.battery.widgets.cycles.ChargeCyclesWidget
import com.monospace.battery.widgets.health.HealthStatusWidget

object WidgetsUpdater {

    suspend fun updateLive(context: Context) {
        runCatching {
            ChargingInfoWidget().updateAll(context)
            BatteryLevelWidget().updateAll(context)
        }
    }

    suspend fun updateAll(context: Context) {
        updateLive(context)
        runCatching {
            ChargeCyclesWidget().updateAll(context)
            HealthStatusWidget().updateAll(context)
        }
    }
}
