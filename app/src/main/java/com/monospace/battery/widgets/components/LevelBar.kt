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
import androidx.glance.unit.ColorProvider
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
        .coerceAtLeast(WidgetConstants.BAR_MARKERS_TOTAL_DP + 1f)
    val complete = level >= 100
    val fillWidth = if (complete) {
        barWidth
    } else {
        (barWidth - WidgetConstants.BAR_MARKERS_TOTAL_DP) * level.coerceIn(0, 100) / 100f
    }
    val outerCorner = heightDp * WidgetConstants.BAR_OUTER_CORNER_RATIO
    val innerCorner = heightDp * WidgetConstants.BAR_INNER_CORNER_RATIO
    val capWidth = min(fillWidth / 2f, outerCorner * 2f)

    val accent = widgetAccentColor()
    val track = dayNightColorProvider(
        day = Color.White.copy(alpha = WidgetConstants.BAR_TRACK_LIGHT_ALPHA),
        night = Color.Black.copy(alpha = WidgetConstants.BAR_TRACK_DARK_ALPHA)
    )

    Box(
        modifier = modifier.width(barWidth.dp).height(heightDp.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .cornerRadius(outerCorner.dp)
                .background(track)
        ) {}

        if (fillWidth > 0f) {
            Box(
                modifier = GlanceModifier
                    .width(fillWidth.dp)
                    .fillMaxHeight()
                    .cornerRadius(outerCorner.dp)
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

        if (!complete) {
            Row(modifier = GlanceModifier.fillMaxHeight().padding(start = fillWidth.dp)) {
                Spacer(modifier = GlanceModifier.width(WidgetConstants.BAR_FIRST_GAP_DP.dp))
                Marker(accent, WidgetConstants.BAR_FIRST_MARKER_WIDTH_DP)
                Spacer(modifier = GlanceModifier.width(WidgetConstants.BAR_SECOND_GAP_DP.dp))
                Marker(accent, WidgetConstants.BAR_SECOND_MARKER_WIDTH_DP)
            }
        }
    }
}

@Composable
private fun PreviewLevelBar(modifier: GlanceModifier, heightDp: Int) {
    val outerCorner = heightDp * WidgetConstants.BAR_OUTER_CORNER_RATIO
    val innerCorner = heightDp * WidgetConstants.BAR_INNER_CORNER_RATIO
    val accent = widgetAccentColor()
    val track = dayNightColorProvider(
        day = Color.White.copy(alpha = WidgetConstants.BAR_TRACK_LIGHT_ALPHA),
        night = Color.Black.copy(alpha = WidgetConstants.BAR_TRACK_DARK_ALPHA)
    )

    Box(modifier = modifier.fillMaxWidth().height(heightDp.dp)) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .cornerRadius(outerCorner.dp)
                .background(track)
        ) {}

        Row(modifier = GlanceModifier.fillMaxSize()) {
            Box(
                modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .cornerRadius(outerCorner.dp)
                        .background(accent)
                ) {}
                Box(
                    modifier = GlanceModifier
                        .width((outerCorner * 2f).dp)
                        .fillMaxHeight()
                        .cornerRadius(innerCorner.dp)
                        .background(accent)
                ) {}
            }

            Spacer(modifier = GlanceModifier.width(WidgetConstants.BAR_FIRST_GAP_DP.dp))
            Marker(accent, WidgetConstants.BAR_FIRST_MARKER_WIDTH_DP)
            Spacer(modifier = GlanceModifier.width(WidgetConstants.BAR_SECOND_GAP_DP.dp))
            Marker(accent, WidgetConstants.BAR_SECOND_MARKER_WIDTH_DP)

            Spacer(modifier = GlanceModifier.defaultWeight())
        }
    }
}

@Composable
private fun Marker(color: ColorProvider, widthDp: Float) {
    Box(
        modifier = GlanceModifier
            .width(widthDp.dp)
            .fillMaxHeight()
            .cornerRadius((widthDp / 2f).dp)
            .background(color)
    ) {}
}
