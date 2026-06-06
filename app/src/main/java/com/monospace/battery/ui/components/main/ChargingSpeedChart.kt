package com.monospace.battery.ui.components.main

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.random.Random

@Composable
fun ChargingSpeedChart(
    speed: Double,
    tint: Color,
    modifier: Modifier = Modifier
) {
    // 1. Generate points that simulate an increasing curve then stabilization
    val points = remember(speed) {
        val random = Random(speed.toLong())

        List(10) { index ->
            // Curve factor: starts at 0.1 and reaches 1.0 around index 4
            val baseFactor = when (index) {
                0 -> 0.05f
                1 -> 0.25f
                2 -> 0.55f
                3 -> 0.85f
                else -> 1.0f
            }

            // Add slight random fluctuation once "stabilized"
            val fluctuation = if (index > 4) (0.95 + random.nextDouble() * 0.1).toFloat() else 1.0f
            (speed * baseFactor * fluctuation).toFloat()
        }
    }

    // 2. Animation progress (0.0 to 1.0)
    val animationProgress = remember { Animatable(0f) }

    // We trigger the animation whenever speed goes from 0 to > 0
    LaunchedEffect(speed > 0) {
        if (speed > 0) {
            animationProgress.snapTo(0f)
            // Slower animation to clearly see the "ramp up"
            animationProgress.animateTo(1f, tween(1500))
        } else {
            animationProgress.snapTo(0f)
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val maxVal = (points.maxOrNull() ?: 1f).coerceAtLeast(1f)

        val path = Path()
        val fillPath = Path()

        val stepX = width / (points.size - 1)

        // Only draw if we have some progress
        if (animationProgress.value > 0.01f) {
            points.forEachIndexed { i, value ->
                val x = i * stepX

                // Calculate target Y position (final state)
                val targetY = height - (value / maxVal * (height * 0.70f)) - (height * 0.10f)

                // Organic growth: points on the left reach target height faster
                val pointDelay = (i.toFloat() / points.size.toFloat()) * 0.4f
                val pointProgress = ((animationProgress.value - pointDelay) / (1f - pointDelay))
                    .coerceIn(0f, 1f)

                val y = height - ((height - targetY) * pointProgress)

                if (i == 0) {
                    path.moveTo(x, y)
                    fillPath.moveTo(x, height)
                    fillPath.lineTo(x, y)
                } else {
                    path.lineTo(x, y)
                    fillPath.lineTo(x, y)
                }

                if (i == points.size - 1) {
                    fillPath.lineTo(x, height)
                    fillPath.close()
                }
            }

            // Draw Gradient Fill
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(tint.copy(alpha = 0.4f), Color.Transparent),
                    startY = height * 0.2f,
                    endY = height
                )
            )

            // Draw Main Line
            drawPath(
                path = path,
                color = tint,
                style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}
