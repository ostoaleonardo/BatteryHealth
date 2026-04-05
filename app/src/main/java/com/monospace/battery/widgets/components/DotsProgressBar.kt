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
import androidx.glance.layout.height

private val ColorGreen = Color(0xFF00A25B)
private val ColorOrange = Color(0xFFFF9800)
private val ColorRed = Color(0xFFF44336)
private val ColorInactive = Color(0x40E0E0E0)

@Composable
fun DotsProgressBar(level: Int, modifier: GlanceModifier = GlanceModifier.fillMaxWidth()) {
    val dotsCount = 10
    val activeDots = (level / 10).coerceIn(0, 10)

    // Level-based color logic
    val activeColor = when {
        level >= 80 -> ColorGreen
        level >= 40 -> ColorOrange
        else -> ColorRed
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..dotsCount) {
            Box(
                modifier = GlanceModifier
                    .defaultWeight()
                    .height(8.dp)
                    .cornerRadius(4.dp)
                    .background(if (i <= activeDots) activeColor else ColorInactive)
            ) {}
        }
    }
}
