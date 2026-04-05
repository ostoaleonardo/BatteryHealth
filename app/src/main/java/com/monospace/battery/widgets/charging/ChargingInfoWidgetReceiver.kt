package com.monospace.battery.widgets.charging

import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class ChargingInfoWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ChargingInfoWidget()

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == Intent.ACTION_POWER_CONNECTED ||
            intent.action == Intent.ACTION_POWER_DISCONNECTED ||
            intent.action == Intent.ACTION_BATTERY_CHANGED
        ) {
            MainScope().launch {
                GlanceAppWidgetManager(context).getGlanceIds(ChargingInfoWidget::class.java).forEach {
                    glanceAppWidget.update(context, it)
                }
            }
        }
    }
}
