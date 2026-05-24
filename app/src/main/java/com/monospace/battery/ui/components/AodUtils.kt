package com.monospace.battery.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.cos
import kotlin.math.sin

fun DrawScope.drawAodMeter(
    style: Int,
    level: Int,
    color: Color
) {
    val strokeWidth = size.width * 0.05f

    when (style) {
        0 -> drawSolidArc(level, color, strokeWidth, rounded = true)
        1 -> drawSolidArc(level, color, strokeWidth, rounded = false)
        2 -> drawDottedArc(level, color, strokeWidth, rounded = false)
        3 -> drawDottedArc(level, color, strokeWidth, rounded = true)
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
