package com.monospace.battery.widgets.pager

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.monospace.battery.core.constants.WidgetConstants
import com.monospace.battery.core.utils.BatteryMockUtils
import com.monospace.battery.core.utils.CrashReporter
import com.monospace.battery.data.local.WidgetsUtils
import com.monospace.battery.widgets.BatteryWidgetReceiver
import com.monospace.battery.widgets.WidgetsUpdater
import com.monospace.battery.widgets.components.LockedWidgetContent
import com.monospace.battery.widgets.components.WidgetPreviewSize
import com.monospace.battery.widgets.components.widgetSurfaceColor

class BatteryPagerWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Exact

    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val page = (currentState<Preferences>()[PAGE_KEY] ?: 0).coerceIn(0, WidgetConstants.PAGER_PAGE_COUNT - 1)

            val batteryStatus = context.registerReceiver(
                null, IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            )

            GlanceTheme {
                if (WidgetsUtils.isWidgetsPurchased(context)) {
                    PagerContent(page, batteryStatus)
                } else {
                    LockedWidgetContent()
                }
            }
        }
    }

    override suspend fun providePreview(context: Context, widgetCategory: Int) {
        provideContent {
            val batteryStatus = LocalContext.current.registerReceiver(
                null, IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            )

            GlanceTheme {
                WidgetPreviewSize(WidgetConstants.PREVIEW_SIZE_DP, WidgetConstants.PREVIEW_SIZE_DP) {
                    PagerContent(0, batteryStatus ?: BatteryMockUtils.createMockBatteryIntent())
                }
            }
        }
    }

    @Composable
    private fun PagerContent(page: Int, batteryStatus: Intent?) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .appWidgetBackground()
                .cornerRadius(28.dp)
                .background(widgetSurfaceColor())
                .padding(12.dp)
                .clickable(actionRunCallback<NextPageAction>())
        ) {
            when (page) {
                0 -> HealthContent(batteryStatus)
                else -> ChargingInfoContent(batteryStatus)
            }
        }
    }

    companion object {
        val PAGE_KEY = intPreferencesKey("page")
    }
}

class NextPageAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        CrashReporter.log("Pager widget next page")

        updateAppWidgetState(context, glanceId) { prefs ->
            prefs[BatteryPagerWidget.PAGE_KEY] =
                ((prefs[BatteryPagerWidget.PAGE_KEY] ?: 0) + 1) % WidgetConstants.PAGER_PAGE_COUNT
            prefs[WidgetsUpdater.REFRESH_KEY] = System.nanoTime()
        }

        BatteryPagerWidget().update(context, glanceId)
    }
}

class BatteryPagerWidgetReceiver : BatteryWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = BatteryPagerWidget()
}
