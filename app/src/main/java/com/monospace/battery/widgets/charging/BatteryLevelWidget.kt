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
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.monospace.battery.R
import com.monospace.battery.helpers.BatteryInfo
import com.monospace.battery.helpers.WidgetsUtils
import com.monospace.battery.widgets.components.DotsProgressBar
import com.monospace.battery.widgets.components.LockedWidgetContent
import com.monospace.battery.widgets.components.WidgetInfoRow
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class BatteryLevelWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            currentState<Preferences>()

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

    @Composable
    private fun WidgetContent(batteryStatus: Intent?) {
        val context = LocalContext.current
        val batteryInfo = BatteryInfo(batteryStatus)
        val batteryLevel = batteryInfo.level

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .appWidgetBackground()
                .cornerRadius(32.dp)
                .padding(16.dp)
                .background(GlanceTheme.colors.surface)
                .clickable(actionRunCallback<UpdateLevelAction>())
        ) {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WidgetInfoRow(
                        label = context.getString(R.string.battery_level),
                        value = context.getString(R.string.battery_percentage, batteryLevel)
                    )
                }

                Spacer(GlanceModifier.height(8.dp))
                DotsProgressBar(level = batteryLevel)
            }
        }
    }
}

class UpdateLevelAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        updateAppWidgetState(context, glanceId) { }
        BatteryLevelWidget().update(context, glanceId)
    }
}

class BatteryLevelWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = BatteryLevelWidget()

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == Intent.ACTION_POWER_CONNECTED ||
            intent.action == Intent.ACTION_POWER_DISCONNECTED ||
            intent.action == Intent.ACTION_BATTERY_CHANGED
        ) {
            MainScope().launch {
                GlanceAppWidgetManager(context).getGlanceIds(BatteryLevelWidget::class.java).forEach {
                    glanceAppWidget.update(context, it)
                }
            }
        }
    }
}
