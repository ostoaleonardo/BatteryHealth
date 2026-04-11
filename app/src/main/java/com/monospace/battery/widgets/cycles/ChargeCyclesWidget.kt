package com.monospace.battery.widgets.cycles

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.monospace.battery.helpers.BatteryMockUtils
import com.monospace.battery.helpers.WidgetsUtils
import com.monospace.battery.widgets.BatteryWidgetReceiver
import com.monospace.battery.widgets.components.LockedWidgetContent
import com.monospace.battery.widgets.components.WidgetValueLabel

class ChargeCyclesWidget : GlanceAppWidget() {

    companion object {
        private const val TAG = "ChargeCyclesWidget"
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val context = LocalContext.current
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
    private fun WidgetContent(batteryStatus: Intent?) {
        val context = LocalContext.current
        Log.i(TAG, "Updating ChargeCyclesWidget")

        val cycles = BatteryInfo(batteryStatus).chargeCycles

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
                value = "$cycles",
                label = context.getString(R.string.battery_charging_cycles),
                valueFontSize = 56.sp
            )
        }
    }
}

class ChargeCyclesWidgetReceiver : BatteryWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ChargeCyclesWidget()

    override val updateActions: List<String> = listOf(
        Intent.ACTION_POWER_CONNECTED,
        Intent.ACTION_POWER_DISCONNECTED,
        Intent.ACTION_SCREEN_ON
    )
}
