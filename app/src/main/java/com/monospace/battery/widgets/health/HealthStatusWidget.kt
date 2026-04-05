package com.monospace.battery.widgets.health

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import com.monospace.battery.R
import com.monospace.battery.helpers.BatteryInfo
import com.monospace.battery.helpers.BatteryStrings
import com.monospace.battery.helpers.WidgetsUtils
import com.monospace.battery.widgets.components.LockedWidgetContent
import com.monospace.battery.widgets.components.WidgetValueLabel

class HealthStatusWidget : GlanceAppWidget() {

    companion object {
        private const val TAG = "HealthStatusWidget"
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                val isPurchased = WidgetsUtils.isWidgetsPurchased(context)
                if (isPurchased) {
                    WidgetContent()
                } else {
                    LockedWidgetContent()
                }
            }
        }
    }

    @Composable
    fun WidgetContent() {
        val context = LocalContext.current
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = context.registerReceiver(null, intentFilter)
        
        Log.i(TAG, "Updating HealthStatusWidget")

        val health = BatteryInfo(batteryStatus).health
        
        val healthString = BatteryStrings().getHealthStatus(health)

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .appWidgetBackground()
                .cornerRadius(28.dp)
                .padding(16.dp)
                .background(GlanceTheme.colors.surface),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            WidgetValueLabel(
                value = context.getString(healthString),
                label = context.getString(R.string.battery_health)
            )
        }
    }
}
