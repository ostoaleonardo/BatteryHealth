package com.monospace.battery.core.constants

import androidx.compose.ui.graphics.Color
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.milliseconds

object WidgetConstants {

    // Glass background
    const val GLASS_ALPHA_DEFAULT = 60
    const val GLASS_ALPHA_MIN = 20f
    const val GLASS_ALPHA_MAX = 80f
    val GLASS_REFRESH_DEBOUNCE = 200.milliseconds

    // Layout
    const val TEXT_HORIZONTAL_MARGIN_DP = 32f
    const val BAR_HORIZONTAL_MARGIN_DP = 24f
    const val ICON_RESERVED_WIDTH_DP = 64f

    // Level bar
    const val BAR_DEFAULT_HEIGHT_DP = 31
    const val BAR_OUTER_CORNER_RATIO = 7f / 31f
    const val BAR_INNER_CORNER_RATIO = 2f / 31f
    const val BAR_TRACK_DARK_ALPHA = 0.29f
    const val BAR_TRACK_LIGHT_ALPHA = 0.35f

    // Health indicator
    const val HEALTH_DOTS_COUNT = 3
    const val HEALTH_DOTS_INACTIVE_ALPHA = 0.25f

    // Light mode text
    val TEXT_PRIMARY_LIGHT = Color(0xFF1C1C1C)
    val TEXT_SECONDARY_LIGHT = Color(0xFF768288)

    // Preview
    const val PREVIEW_SIZE_DP = 168
    const val PREVIEW_ROW_HEIGHT_DP = 84
    val PREVIEW_REFRESH_INTERVAL = 1.days

    // Pager
    const val PAGER_PAGE_COUNT = 2
}
