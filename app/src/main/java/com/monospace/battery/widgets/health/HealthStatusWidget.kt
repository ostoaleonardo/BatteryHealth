package com.monospace.battery.widgets.health

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.appwidget.GlanceAppWidget
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
import com.monospace.battery.R
import com.monospace.battery.helpers.BatteryInfo
import com.monospace.battery.helpers.BatteryMockUtils
import com.monospace.battery.helpers.BatteryStrings
import com.monospace.battery.helpers.WidgetsUtils
import com.monospace.battery.ui.components.getHealthColor
import com.monospace.battery.ui.components.getHealthIcon
import com.monospace.battery.widgets.BatteryWidgetReceiver
import com.monospace.battery.widgets.components.LockedWidgetContent
import com.monospace.battery.widgets.components.WidgetStatusIndicator
import com.monospace.battery.widgets.components.WidgetValueLabel

class HealthStatusWidget : GlanceAppWidget() {

    companion object {
        private const val TAG = "HealthStatusWidget"
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val batteryStatus = context.registerReceiver(
                null, IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            )

            GlanceTheme {
                val isPurchased = WidgetsUtils.isWidgetsPurchased(context)

                if (isPurchased) {
                    WidgetContent(batteryStatus)
                } else {
                    LockedWidgetContent()
                }
            }
        }
    }

    override suspend fun providePreview(context: Context, widgetCategory: Int) {
        provideContent {
            val context = LocalContext.current
            val batteryStatus = context.registerReceiver(
                null, IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            )

            GlanceTheme {
                WidgetContent(batteryStatus ?: BatteryMockUtils.createMockBatteryIntent())
            }
        }
    }

    @Composable
    fun WidgetContent(batteryStatus: Intent?) {
        val context = LocalContext.current
        Log.i(TAG, "Updating HealthStatusWidget")

        val health = BatteryInfo(batteryStatus).health
        val healthString = BatteryStrings().getHealthStatus(health)
        val healthIconRes = getHealthIcon(health)
        val healthColor = getHealthColor(health)

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .appWidgetBackground()
                .cornerRadius(28.dp)
                .padding(16.dp)
                .background(GlanceTheme.colors.surface)
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

class HealthStatusWidgetReceiver : BatteryWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = HealthStatusWidget()

    override val updateActions: List<String> = listOf(
        Intent.ACTION_POWER_CONNECTED,
        Intent.ACTION_POWER_DISCONNECTED,
        Intent.ACTION_SCREEN_ON
    )
}
