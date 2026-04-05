package com.monospace.battery.widgets.charging

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
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
import androidx.glance.layout.size
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.monospace.battery.R
import com.monospace.battery.helpers.BatteryInfo
import com.monospace.battery.helpers.BatteryStrings
import com.monospace.battery.helpers.BatteryUtils
import com.monospace.battery.helpers.WidgetsUtils
import com.monospace.battery.widgets.components.DotsProgressBar
import com.monospace.battery.widgets.components.LockedWidgetContent
import com.monospace.battery.widgets.components.WidgetInfoRow

class ChargingInfoWidget : GlanceAppWidget() {

    companion object {
        val UPDATE_COUNT_KEY = intPreferencesKey("update_count")
    }

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
    fun WidgetContent(batteryStatus: Intent?) {
        val context = LocalContext.current
        val batteryInfo = BatteryInfo(batteryStatus)
        val batteryStrings = BatteryStrings()
        val batteryUtils = BatteryUtils(context)

        val isCharging = batteryInfo.isCharging
        val batteryLevel = batteryInfo.level
        val statusStr = context.getString(batteryStrings.getChargingStatus(isCharging))

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .cornerRadius(28.dp)
                .appWidgetBackground()
                .padding(16.dp)
                .background(GlanceTheme.colors.surface)
                .clickable(actionRunCallback<UpdateAction>())
        ) {
            Column(modifier = GlanceModifier.fillMaxSize()) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = GlanceModifier.defaultWeight(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalAlignment = Alignment.Start
                    ) {
                        Row(
                            modifier = GlanceModifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            WidgetInfoRow(
                                label = context.getString(R.string.battery_status),
                                value = statusStr
                            )

                            Spacer(modifier = GlanceModifier.defaultWeight())

                            Image(
                                provider = ImageProvider(
                                    if (isCharging) R.drawable.bolt else R.drawable.cable
                                ),
                                contentDescription = null,
                                modifier = GlanceModifier.size(24.dp),
                                colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurface)
                            )
                        }

                        if (isCharging) {
                            val sourceStr = context.getString(batteryStrings.getChargingSource(batteryInfo.chargeSource))
                            val speed = batteryUtils.getChargeSpeed(batteryStatus ?: Intent(), true)

                            Spacer(GlanceModifier.height(8.dp))
                            WidgetInfoRow(
                                label = context.getString(R.string.charging_source),
                                value = sourceStr
                            )

                            Spacer(GlanceModifier.height(8.dp))
                            WidgetInfoRow(
                                label = context.getString(R.string.battery_speed),
                                value = context.getString(R.string.charge_speed_watts, speed)
                            )
                        } else {
                            Spacer(GlanceModifier.height(8.dp))
                            WidgetInfoRow(
                                label = context.getString(R.string.battery_level),
                                value = context.getString(R.string.battery_percentage, batteryLevel)
                            )
                        }
                    }
                }

                Spacer(GlanceModifier.height(12.dp))
                DotsProgressBar(level = batteryLevel)
            }
        }
    }
}

class UpdateAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        updateAppWidgetState(context, glanceId) { prefs ->
            val currentCount = prefs[ChargingInfoWidget.UPDATE_COUNT_KEY] ?: 0
            prefs[ChargingInfoWidget.UPDATE_COUNT_KEY] = currentCount + 1
        }

        ChargingInfoWidget().update(context, glanceId)
    }
}
