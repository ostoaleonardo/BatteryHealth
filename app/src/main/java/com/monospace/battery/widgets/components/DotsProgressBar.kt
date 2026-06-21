package com.monospace.battery.widgets.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.size
import com.monospace.battery.ui.utils.getBatteryLevelColor

private val ColorInactive = Color(0x40E0E0E0)

@Composable
fun DotsProgressBar(level: Int, modifier: GlanceModifier = GlanceModifier.fillMaxWidth()) {
    val dotsCount = 10
    val activeDots = (level / 10).coerceIn(0, 10)
    val activeColor = getBatteryLevelColor(level)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..dotsCount) {
            Box(
                modifier = GlanceModifier.defaultWeight(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = GlanceModifier
                        .size(10.dp)
                        .cornerRadius(5.dp)
                        .background(if (i <= activeDots) activeColor else ColorInactive)
                ) {}
            }
        }
    }
}
