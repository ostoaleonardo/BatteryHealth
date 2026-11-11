package com.monospace.battery.widgets.pager

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.size
import com.monospace.battery.R
import com.monospace.battery.core.utils.BatteryStrings
import com.monospace.battery.core.utils.BatteryUtils
import com.monospace.battery.data.models.BatteryInfo
import com.monospace.battery.ui.utils.getHealthIcon
import com.monospace.battery.widgets.components.LevelBar
import com.monospace.battery.widgets.components.WidgetInfoRow
import com.monospace.battery.widgets.components.WidgetStatusIndicator
import com.monospace.battery.widgets.components.WidgetValueLabel
import com.monospace.battery.widgets.components.healthToDots
import com.monospace.battery.widgets.components.widgetTextColor

@Composable
fun HealthContent(batteryStatus: Intent?) {
    val context = LocalContext.current
    val health = BatteryInfo(batteryStatus).health
    val healthString = BatteryStrings().getHealthStatus(health)
    val healthIconRes = getHealthIcon(health)

    Column(
        modifier = GlanceModifier.fillMaxSize(),
        horizontalAlignment = Alignment.Start
    ) {
        Box(
            modifier = GlanceModifier.fillMaxWidth(),
            contentAlignment = Alignment.TopEnd
        ) {
            WidgetStatusIndicator(activeDots = healthToDots(health))
        }

        Spacer(modifier = GlanceModifier.defaultWeight())

        Image(
            provider = ImageProvider(healthIconRes),
            contentDescription = null,
            modifier = GlanceModifier.size(32.dp),
            colorFilter = ColorFilter.tint(widgetTextColor())
        )

        Spacer(modifier = GlanceModifier.height(8.dp))

        WidgetValueLabel(
            value = context.getString(healthString),
            label = context.getString(R.string.battery_health),
            horizontalAlignment = Alignment.Start
        )
    }
}

@Composable
fun ChargingInfoContent(batteryStatus: Intent?) {
    val context = LocalContext.current
    val batteryInfo = BatteryInfo(batteryStatus)
    val batteryStrings = BatteryStrings()
    val batteryUtils = BatteryUtils(context)

    val isCharging = batteryInfo.isCharging
    val batteryLevel = batteryInfo.level
    val statusStr = context.getString(batteryStrings.getChargingStatus(isCharging))

    Column(modifier = GlanceModifier.fillMaxSize()) {
        Column(
            modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(modifier = GlanceModifier.fillMaxWidth()) {
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
                    colorFilter = ColorFilter.tint(widgetTextColor())
                )
            }

            if (isCharging) {
                val sourceStr =
                    context.getString(batteryStrings.getChargingSource(batteryInfo.chargeSource))
                val speed = batteryUtils.getChargeSpeed(batteryInfo.voltage, true)
                val speedWatts = context.getString(R.string.charge_speed_watts, speed)
                val qualitySpeed = context.getString(batteryStrings.getChargerQuality(speed))

                SpacedInfoRow(context.getString(R.string.charging_source), sourceStr)
                SpacedInfoRow(
                    context.getString(R.string.battery_speed),
                    "$speedWatts ($qualitySpeed)"
                )
            }
        }

        Spacer(GlanceModifier.height(8.dp))
        LevelBar(level = batteryLevel)
    }
}

@Composable
private fun SpacedInfoRow(label: String, value: String) {
    Spacer(GlanceModifier.height(8.dp))
    WidgetInfoRow(label = label, value = value)
}
