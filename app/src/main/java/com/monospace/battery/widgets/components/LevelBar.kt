package com.monospace.battery.widgets.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.LocalSize
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import com.monospace.battery.core.constants.WidgetConstants
import kotlin.math.min
import androidx.glance.color.ColorProvider as dayNightColorProvider

@Composable
fun LevelBar(
    level: Int,
    modifier: GlanceModifier = GlanceModifier,
    heightDp: Int = WidgetConstants.BAR_DEFAULT_HEIGHT_DP
) {
    if (LocalWidgetPreview.current) {
        PreviewLevelBar(modifier, heightDp)
        return
    }

    val barWidth = (LocalSize.current.width.value - WidgetConstants.BAR_HORIZONTAL_MARGIN_DP)
        .coerceAtLeast(1f)
    val fillWidth = barWidth * level.coerceIn(0, 100) / 100f
    val complete = level >= 100
    val corner = heightDp * WidgetConstants.BAR_OUTER_CORNER_RATIO
    val innerCorner = heightDp * WidgetConstants.BAR_INNER_CORNER_RATIO
    val capWidth = min(fillWidth / 2f, corner * 2f)
    val accent = widgetAccentColor()

    Box(
        modifier = modifier.width(barWidth.dp).height(heightDp.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .cornerRadius(corner.dp)
                .background(barTrackColor())
        ) {}

        if (fillWidth > 0f) {
            Box(
                modifier = GlanceModifier
                    .width(fillWidth.dp)
                    .fillMaxHeight()
                    .cornerRadius(corner.dp)
                    .background(accent)
            ) {}

            if (!complete) {
                Box(
                    modifier = GlanceModifier
                        .fillMaxHeight()
                        .padding(start = (fillWidth - capWidth).dp)
                ) {
                    Box(
                        modifier = GlanceModifier
                            .width(capWidth.dp)
                            .fillMaxHeight()
                            .cornerRadius(innerCorner.dp)
                            .background(accent)
                    ) {}
                }
            }
        }
    }
}

@Composable
private fun PreviewLevelBar(modifier: GlanceModifier, heightDp: Int) {
    val corner = heightDp * WidgetConstants.BAR_OUTER_CORNER_RATIO
    val innerCorner = heightDp * WidgetConstants.BAR_INNER_CORNER_RATIO
    val accent = widgetAccentColor()

    Box(modifier = modifier.fillMaxWidth().height(heightDp.dp)) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .cornerRadius(corner.dp)
                .background(barTrackColor())
        ) {}

        Row(modifier = GlanceModifier.fillMaxSize()) {
            Box(
                modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .cornerRadius(corner.dp)
                        .background(accent)
                ) {}
                Box(
                    modifier = GlanceModifier
                        .width((corner * 2f).dp)
                        .fillMaxHeight()
                        .cornerRadius(innerCorner.dp)
                        .background(accent)
                ) {}
            }

            Spacer(modifier = GlanceModifier.defaultWeight())
        }
    }
}

@Composable
private fun barTrackColor() = dayNightColorProvider(
    day = Color.White.copy(alpha = WidgetConstants.BAR_TRACK_LIGHT_ALPHA),
    night = Color.Black.copy(alpha = WidgetConstants.BAR_TRACK_DARK_ALPHA)
)
