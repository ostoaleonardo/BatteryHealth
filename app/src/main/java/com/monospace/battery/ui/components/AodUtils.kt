package com.monospace.battery.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.cos
import kotlin.math.sin

fun DrawScope.drawAodMeter(
    style: Int,
    level: Int,
    color: Color,
    timeMillis: Long = System.currentTimeMillis()
) {
    val strokeWidth = size.width * 0.05f

    when (style) {
        0 -> { /* None - Percentage only */ }
        1 -> drawSolidArc(level, color, strokeWidth, rounded = true)
        2 -> drawSolidArc(level, color, strokeWidth, rounded = false)
        3 -> drawDottedArc(level, color, strokeWidth, rounded = false)
        4 -> drawDottedArc(level, color, strokeWidth, rounded = true)
        5 -> drawWaterGlass(level, color, timeMillis)
    }
}

private fun DrawScope.drawSolidArc(level: Int, color: Color, strokeWidth: Float, rounded: Boolean) {
    val start = 140f
    val sweep = 260f
    val cap = if (rounded) StrokeCap.Round else StrokeCap.Butt

    // Background track
    drawArc(
        color = Color.Gray.copy(alpha = 0.2f),
        startAngle = start,
        sweepAngle = sweep,
        useCenter = false,
        style = Stroke(width = strokeWidth, cap = cap)
    )

    // Dynamic progress
    drawArc(
        color = color,
        startAngle = start,
        sweepAngle = (level / 100f) * sweep,
        useCenter = false,
        style = Stroke(width = strokeWidth, cap = cap)
    )
}

private fun DrawScope.drawDottedArc(
    level: Int,
    color: Color,
    strokeWidth: Float,
    rounded: Boolean
) {
    val startAngle = 140f
    val sweepAngle = 260f
    val dotsCount = 20
    val cap = if (rounded) StrokeCap.Round else StrokeCap.Butt

    val baseRadius = size.width / 2f - strokeWidth
    val innerRadius = baseRadius - strokeWidth * 1.2f
    val outerRadius = baseRadius + strokeWidth * 1.2f
    val tickThickness = strokeWidth * 0.35f

    for (i in 0 until dotsCount) {
        val angleDeg = startAngle + i * (sweepAngle / (dotsCount - 1))
        val angleRad = Math.toRadians(angleDeg.toDouble())

        val isFilled = (i).toFloat() / (dotsCount - 1) <= (level / 100f)

        val startOffset = Offset(
            x = center.x + (innerRadius * cos(angleRad)).toFloat(),
            y = center.y + (innerRadius * sin(angleRad)).toFloat()
        )
        val endOffset = Offset(
            x = center.x + (outerRadius * cos(angleRad)).toFloat(),
            y = center.y + (outerRadius * sin(angleRad)).toFloat()
        )

        drawLine(
            color = if (isFilled) color else Color.Gray.copy(alpha = 0.2f),
            start = startOffset,
            end = endOffset,
            strokeWidth = tickThickness,
            cap = cap
        )
    }
}

private fun DrawScope.drawWaterGlass(level: Int, color: Color, timeMillis: Long) {
    val width = size.width
    val height = size.height
    val fillHeight = (level / 100f) * height

    // Wave parameters - Optimized for screen width
    val waveHeight = height * 0.02f
    // Time-based phase for continuous movement
    val phase = (timeMillis % 3000) / 3000f * 2 * Math.PI.toFloat()

    val path = Path()
    path.moveTo(0f, height)
    path.lineTo(0f, height - fillHeight)

    for (x in 0..width.toInt() step 10) {
        val relativeX = x.toFloat() / width
        val y = height - fillHeight + waveHeight * sin(2 * Math.PI.toFloat() * relativeX + phase)
        path.lineTo(x.toFloat(), y)
    }

    path.lineTo(width, height - fillHeight)
    path.lineTo(width, height)
    path.close()

    // Layer 1: Base Liquid
    drawPath(path, color.copy(alpha = 0.5f))

    // Layer 2: Secondary wave for depth
    val phase2 = (timeMillis % 5000) / 5000f * 2 * Math.PI.toFloat()
    val path2 = Path()
    path2.moveTo(0f, height)
    path2.lineTo(0f, height - fillHeight + 5f)

    for (x in 0..width.toInt() step 10) {
        val relativeX = x.toFloat() / width
        val y = height - fillHeight + 5f + (waveHeight * 0.8f) *
                sin(2 * Math.PI.toFloat() * relativeX - phase2)
        path2.lineTo(x.toFloat(), y)
    }

    path2.lineTo(width, height)
    path2.close()
    drawPath(path2, color.copy(alpha = 0.3f))
}
