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

@Composable
fun DotsProgressBar(level: Int, modifier: GlanceModifier = GlanceModifier.fillMaxWidth()) {
    val dotsCount = 10
    val activeDots = (level / 10).coerceIn(0, 10)

    // Level-based color logic
    val activeColor = when {
        level >= 80 -> Color(0xFF00A25B) // Green (Good)
        level >= 40 -> Color(0xFFFF9800) // Orange (Warning)
        else -> Color(0xFFF44336)        // Red (Critical)
    }

    val inactiveColor = Color(0x40E0E0E0)

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
                    .background(if (i <= activeDots) activeColor else inactiveColor)
            ) {}
        }
    }
}
