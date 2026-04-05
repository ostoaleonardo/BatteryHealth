package com.monospace.battery.widgets.health

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import com.monospace.battery.MainActivity
import com.monospace.battery.R
import com.monospace.battery.helpers.BatteryInfo
import com.monospace.battery.helpers.BatteryStrings
import com.monospace.battery.helpers.WidgetsUtils
import com.monospace.battery.widgets.components.LockedWidgetContent
import com.monospace.battery.widgets.components.WidgetStatusIndicator
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

        val healthIconRes = when (health) {
            BatteryManager.BATTERY_HEALTH_GOOD -> R.drawable.battery_status_good
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> R.drawable.thermometer
            BatteryManager.BATTERY_HEALTH_DEAD -> R.drawable.battery_alert
            else -> R.drawable.battery_unknown
        }

        val healthColorRes = when (health) {
            BatteryManager.BATTERY_HEALTH_GOOD -> R.color.battery_health_good
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> R.color.battery_health_overheat
            BatteryManager.BATTERY_HEALTH_DEAD -> R.color.battery_health_dead
            else -> R.color.battery_health_unknown
        }

        val healthColor = Color(context.getColor(healthColorRes))

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .appWidgetBackground()
                .cornerRadius(28.dp)
                .padding(16.dp)
                .background(GlanceTheme.colors.surface)
                .clickable(actionStartActivity(Intent(context, MainActivity::class.java)))
        ) {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                horizontalAlignment = Alignment.Start
            ) {
                Box(
                    modifier = GlanceModifier.fillMaxWidth(),
                    contentAlignment = Alignment.TopEnd
                ) {
                    WidgetStatusIndicator(color = healthColor)
                }

                Spacer(modifier = GlanceModifier.defaultWeight())

                Image(
                    provider = ImageProvider(healthIconRes),
                    contentDescription = null,
                    modifier = GlanceModifier.size(32.dp),
                    colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurface)
                )

                Spacer(modifier = GlanceModifier.height(8.dp))

                WidgetValueLabel(
                    value = context.getString(healthString),
                    label = context.getString(R.string.battery_health),
                    horizontalAlignment = Alignment.Start
                )
            }
        }
    }
}
