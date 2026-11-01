package com.monospace.battery.widgets.components

import android.os.BatteryManager
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.size
import androidx.glance.layout.width
import com.monospace.battery.core.constants.WidgetConstants


fun healthToDots(health: Int): Int = when (health) {
    BatteryManager.BATTERY_HEALTH_GOOD -> 3
    BatteryManager.BATTERY_HEALTH_OVERHEAT,
    BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE,
    BatteryManager.BATTERY_HEALTH_COLD -> 2

    BatteryManager.BATTERY_HEALTH_DEAD,
    BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> 1

    else -> 0
}

@Composable
fun WidgetStatusIndicator(
    activeDots: Int,
    modifier: GlanceModifier = GlanceModifier
) {
    val activeColor = widgetAccentColor()
    val inactiveColor = widgetColor(
        GlanceTheme.colors.onSurface,
        { it.onSurface.copy(alpha = WidgetConstants.HEALTH_DOTS_INACTIVE_ALPHA) },
        { it.onSurface.copy(alpha = WidgetConstants.HEALTH_DOTS_INACTIVE_ALPHA) }
    )

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.End
    ) {
        repeat(WidgetConstants.HEALTH_DOTS_COUNT) { index ->
            Box(
                modifier = GlanceModifier
                    .size(6.dp)
                    .cornerRadius(3.dp)
                    .background(if (index < activeDots) activeColor else inactiveColor)
            ) {}

            if (index < WidgetConstants.HEALTH_DOTS_COUNT - 1) {
                Spacer(modifier = GlanceModifier.width(2.dp))
            }
        }
    }
}
