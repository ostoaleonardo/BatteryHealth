package com.monospace.battery.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.widget.RemoteViews
import com.monospace.battery.R
import com.monospace.battery.helpers.Actions.actions
import com.monospace.battery.helpers.BatteryInfo
import com.monospace.battery.helpers.BatteryStrings
import com.monospace.battery.helpers.WidgetsUtils

class HealthStatusWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (actions.contains(intent?.action)) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisAppWidget = ComponentName(context!!, HealthStatusWidget::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget)
            onUpdate(context, appWidgetManager, appWidgetIds)
        } else {
            super.onReceive(context, intent)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val isPurchased = WidgetsUtils.isWidgetsPurchased(context)

        val views = if (isPurchased) {
            // Get the battery info
            val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val intent = context.registerReceiver(null, intentFilter)
            val health = BatteryInfo(intent).health
            val healthString = BatteryStrings().getHealthStatus(health)

            // Construct the RemoteViews object
            RemoteViews(context.packageName, R.layout.health_status_widget).apply {
                setTextViewText(R.id.health, context.getString(healthString))
            }
        } else {
            // Construct the RemoteViews object for locked widget
            RemoteViews(context.packageName, R.layout.locked_widget)
        }

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
