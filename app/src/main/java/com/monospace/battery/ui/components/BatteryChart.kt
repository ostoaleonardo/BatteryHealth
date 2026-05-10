package com.monospace.battery.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.monospace.battery.data.models.BatteryHistoryEntry

@Composable
fun BatteryChart(entries: List<BatteryHistoryEntry>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(200.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        val color = MaterialTheme.colorScheme.primary
        val surfaceColor = MaterialTheme.colorScheme.surface

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 32.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
        ) {
            val width = size.width
            val height = size.height

            // 1. Draw horizontal guidelines (0%, 50%, 100%) - ALWAYS visible
            val guideLineAlpha = 0.1f
            val guideLineColor = color.copy(alpha = guideLineAlpha)

            // 100%
            drawLine(guideLineColor, start = Offset(0f, 0f), end = Offset(width, 0f))
            // 50%
            drawLine(
                guideLineColor,
                start = Offset(0f, height / 2),
                end = Offset(width, height / 2)
            )
            // 0%
            drawLine(guideLineColor, start = Offset(0f, height), end = Offset(width, height))

            if (entries.isNotEmpty()) {
                val lastEntry = entries.last()
                val minTime = entries.first().timestamp
                val maxTime = lastEntry.timestamp
                val timeRange = (maxTime - minTime).coerceAtLeast(1L)

                if (entries.size >= 2) {
                    // 2. Build paths
                    val path = Path()
                    val fillPath = Path()

                    entries.forEachIndexed { index, entry ->
                        val x =
                            ((entry.timestamp - minTime).toFloat() / timeRange.toFloat()) * width
                        val y = height - (entry.level.toFloat() / 100f * height)

                        if (index == 0) {
                            path.moveTo(x, y)
                            fillPath.moveTo(x, height)
                            fillPath.lineTo(x, y)
                        } else {
                            path.lineTo(x, y)
                            fillPath.lineTo(x, y)
                        }

                        if (index == entries.size - 1) {
                            fillPath.lineTo(x, height)
                            fillPath.close()
                        }
                    }

                    // 3. Draw Fill (Gradient)
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(color.copy(alpha = 0.3f), Color.Transparent)
                        )
                    )

                    // 4. Draw Line
                    drawPath(
                        path = path,
                        color = color,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // 5. Draw current point
                val currentY = height - (lastEntry.level.toFloat() / 100f * height)
                val currentX = if (entries.size >= 2) width else width / 2f
                drawCircle(color, radius = 6.dp.toPx(), center = Offset(currentX, currentY))
                drawCircle(surfaceColor, radius = 3.dp.toPx(), center = Offset(currentX, currentY))
            }
        }
    }
}
