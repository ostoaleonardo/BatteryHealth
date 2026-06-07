package com.monospace.battery.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.monospace.battery.data.models.LocalBatteryState
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

enum class MeterStyle(val index: Int) {
    NONE(0),
    SOLID_ROUND(1),
    DOTTED_ROUND(2),
    MATERIAL_CIRCULAR(3),
    WAVY(4),
    WATER_GLASS(5);

    companion object {
        fun fromIndex(index: Int): MeterStyle = entries.find { it.index == index } ?: NONE
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeterDisplay(
    styleIndex: Int,
    modifier: Modifier = Modifier,
    level: Int = LocalBatteryState.current.level,
    color: Color = MaterialTheme.colorScheme.primary,
    strokeWidth: Dp = 4.dp,
    size: Dp = 50.dp
) {
    val style = MeterStyle.fromIndex(styleIndex)

    // Internal animation state for styles that need it (Water Glass)
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    if (style == MeterStyle.WATER_GLASS) {
        LaunchedEffect(Unit) {
            while (true) {
                currentTime = System.currentTimeMillis()
                delay(50L)
            }
        }
    }

    when (style) {
        MeterStyle.WAVY -> {
            WavyMeter(level, color, modifier, strokeWidth, size)
        }

        MeterStyle.MATERIAL_CIRCULAR -> {
            MaterialMeter(level, color, modifier, strokeWidth)
        }

        else -> {
            Canvas(modifier = modifier) {
                drawAodMeter(style, level, color, currentTime)
            }
        }
    }
}

fun DrawScope.drawAodMeter(
    style: MeterStyle,
    level: Int,
    color: Color,
    timeMillis: Long = System.currentTimeMillis()
) {
    val strokeWidth = size.width * 0.05f

    when (style) {
        MeterStyle.NONE -> { /* Percentage only */ }
        MeterStyle.SOLID_ROUND -> drawSolidArc(level, color, strokeWidth, rounded = true)
        MeterStyle.DOTTED_ROUND -> drawDottedArc(level, color, strokeWidth, rounded = true)
        MeterStyle.MATERIAL_CIRCULAR -> { /* Composable */ }
        MeterStyle.WAVY -> { /* Composable */ }
        MeterStyle.WATER_GLASS -> drawWaterGlass(level, color, timeMillis)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WavyMeter(
    level: Int,
    color: Color,
    modifier: Modifier,
    strokeWidth: Dp,
    size: Dp
) {
    val density = LocalDensity.current
    val strokeWidthPx = with(density) { strokeWidth.toPx() }

    // Formula: Circumference / wavesCount = (diameter * PI) / wavesCount
    val wavesCount = 10
    val wavelength = (size.value * 3.14159f / wavesCount).dp

    CircularWavyProgressIndicator(
        progress = { level / 100f },
        modifier = modifier,
        color = color,
        stroke = Stroke(width = strokeWidthPx, cap = StrokeCap.Round),
        trackColor = color.copy(alpha = 0.2f),
        trackStroke = Stroke(width = strokeWidthPx, cap = StrokeCap.Round),
        wavelength = wavelength
    )
}

@Composable
private fun MaterialMeter(
    level: Int,
    color: Color,
    modifier: Modifier,
    strokeWidth: Dp
) {
    CircularProgressIndicator(
        progress = { level / 100f },
        modifier = modifier,
        color = color,
        strokeWidth = strokeWidth,
        trackColor = color.copy(alpha = 0.2f),
        strokeCap = StrokeCap.Round
    )
}
