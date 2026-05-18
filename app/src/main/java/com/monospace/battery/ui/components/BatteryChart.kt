package com.monospace.battery.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monospace.battery.data.models.BatteryHistoryEntry
import com.monospace.battery.ui.theme.Font
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BatteryChart(entries: List<BatteryHistoryEntry>) {
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(
        fontFamily = Font.AzeretMonoLight,
        fontSize = 8.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    )

    val dateFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    
    // Animation state
    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(entries) {
        if (entries.isNotEmpty()) {
            animationProgress.snapTo(0f)
            animationProgress.animateTo(1f, tween(1200))
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .padding(horizontal = 16.dp),
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
                .padding(top = 24.dp, bottom = 32.dp, start = 54.dp, end = 24.dp)
        ) {
            val width = size.width
            val height = size.height

            // 1. Draw horizontal guidelines
            val guideLineAlpha = 0.1f
            val guideLineColor = color.copy(alpha = guideLineAlpha)

            val yLevels = listOf(0f to "100%", height / 2 to "50%", height to "0%")
            yLevels.forEach { (y, label) ->
                drawLine(guideLineColor, start = Offset(0f, y), end = Offset(width, y))
                drawText(
                    textMeasurer = textMeasurer,
                    text = label,
                    style = labelStyle,
                    topLeft = Offset(-44.dp.toPx(), y - 7.dp.toPx())
                )
            }

            if (entries.isNotEmpty()) {
                val minTime = entries.first().timestamp
                val maxTime = entries.last().timestamp
                val timeRange = (maxTime - minTime).coerceAtLeast(1L)

                // Draw X-axis Time Labels
                val xLabels = if (entries.size >= 2) {
                    listOf(
                        0f to dateFormat.format(Date(minTime)),
                        width / 2 to dateFormat.format(Date(minTime + timeRange / 2)),
                        width to dateFormat.format(Date(maxTime))
                    )
                } else {
                    listOf(width / 2 to dateFormat.format(Date(maxTime)))
                }

                xLabels.forEach { (x, label) ->
                    val textLayoutResult = textMeasurer.measure(label, labelStyle)
                    drawText(
                        textLayoutResult = textLayoutResult,
                        topLeft = Offset(x - textLayoutResult.size.width / 2, height + 8.dp.toPx())
                    )
                }

                if (entries.size >= 2) {
                    val path = Path()
                    val fillPath = Path()
                    
                    // Create path based on animation progress
                    val pointsToDraw = (entries.size * animationProgress.value).toInt().coerceAtLeast(1)
                    
                    entries.take(pointsToDraw).forEachIndexed { index, entry ->
                        val x = ((entry.timestamp - minTime).toFloat() / timeRange.toFloat()) * width
                        val y = height - (entry.level.toFloat() / 100f * height)

                        if (index == 0) {
                            path.moveTo(x, y)
                            fillPath.moveTo(x, height)
                            fillPath.lineTo(x, y)
                        } else {
                            path.lineTo(x, y)
                            fillPath.lineTo(x, y)
                        }

                        if (index == pointsToDraw - 1) {
                            fillPath.lineTo(x, height)
                            fillPath.close()
                        }
                    }

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(color.copy(alpha = 0.3f), Color.Transparent)
                        )
                    )

                    drawPath(
                        path = path,
                        color = color,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Draw current point (last entry)
                val lastEntry = entries.last()
                val currentY = height - (lastEntry.level.toFloat() / 100f * height)
                val currentX = if (entries.size >= 2) width else width / 2f
                
                // Scale point pulse based on progress
                if (animationProgress.value > 0.95f) {
                    drawCircle(color, radius = 6.dp.toPx(), center = Offset(currentX, currentY))
                    drawCircle(surfaceColor, radius = 3.dp.toPx(), center = Offset(currentX, currentY))
                }
            }
        }
    }
}
