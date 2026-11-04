package com.monospace.battery.widgets

import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class BatteryWidgetReceiver : GlanceAppWidgetReceiver() {
    abstract override val glanceAppWidget: GlanceAppWidget

    open val updateActions: List<String> = listOf(
        Intent.ACTION_POWER_CONNECTED,
        Intent.ACTION_POWER_DISCONNECTED,
        Intent.ACTION_BATTERY_CHANGED,
        Intent.ACTION_SCREEN_ON
    )

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (updateActions.contains(intent.action)) {
            val pending = goAsync()

            CoroutineScope(Dispatchers.Default).launch {
                try {
                    WidgetsUpdater.refresh(context, this@BatteryWidgetReceiver::class.java, glanceAppWidget)
                } finally {
                    pending.finish()
                }
            }
        }
    }
}
