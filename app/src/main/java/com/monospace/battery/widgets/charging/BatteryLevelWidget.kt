package com.monospace.battery.widgets.charging

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.monospace.battery.core.constants.WidgetConstants
import com.monospace.battery.core.utils.BatteryMockUtils
import com.monospace.battery.data.local.WidgetsUtils
import com.monospace.battery.data.models.BatteryInfo
import com.monospace.battery.widgets.BatteryWidgetReceiver
import com.monospace.battery.widgets.components.LevelBar
import com.monospace.battery.widgets.components.LockedWidgetContent
import com.monospace.battery.widgets.components.WidgetPreviewSize
import com.monospace.battery.widgets.components.widgetSurfaceColor

class BatteryLevelWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Exact

    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            currentState<Preferences>()

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
                WidgetPreviewSize(WidgetConstants.PREVIEW_SIZE_DP, WidgetConstants.PREVIEW_ROW_HEIGHT_DP) {
                    WidgetContent(batteryStatus ?: BatteryMockUtils.createMockBatteryIntent())
                }
            }
        }
    }

    @Composable
    fun WidgetContent(batteryStatus: Intent?) {
        val batteryLevel = BatteryInfo(batteryStatus).level

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .appWidgetBackground()
                .cornerRadius(32.dp)
                .padding(12.dp)
                .background(widgetSurfaceColor())
        ) {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LevelBar(level = batteryLevel)
            }
        }
    }
}

class BatteryLevelWidgetReceiver : BatteryWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = BatteryLevelWidget()
}
