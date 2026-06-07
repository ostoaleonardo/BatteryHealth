package com.monospace.battery.ui.components.aod

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.monospace.battery.R
import com.monospace.battery.data.models.LocalSettingsActions
import com.monospace.battery.data.models.LocalSettingsState
import com.monospace.battery.ui.components.AodStyleSelectors
import com.monospace.battery.ui.components.SettingsSection

@Composable
fun AodSettingsSection() {
    val state = LocalSettingsState.current
    val actions = LocalSettingsActions.current

    // 1. Style Selectors (Clock, Speedometer, Color)
    AodStyleSelectors(
        clockStyle = state.aodClockStyle,
        meterStyle = state.aodMeterStyle,
        selectedColor = if (state.aodColor == 0L) MaterialTheme.colorScheme.primary else Color(state.aodColor),
        is24h = state.aod24hFormat,
        onClockStyleChange = actions.onAodClockStyleChange,
        onMeterStyleChange = actions.onAodMeterStyleChange,
        onColorChange = actions.onAodColorChange
    )

    // 2. Additional Look & Feel Settings
    SettingsSection(stringResource(R.string.aod_look_feel)) {
        // Clock Settings
        switchItem(
            title = stringResource(R.string.aod_show_clock),
            checked = state.aodShowClock,
            onCheckedChange = actions.onAodShowClockChange
        )

        if (state.aodShowClock) {
            sliderItem(
                title = stringResource(R.string.aod_font_size_clock),
                description = "${((state.aodFontSizeClock - 40) * 100 / 80)}%",
                value = state.aodFontSizeClock,
                onValueChange = actions.onAodFontSizeClockChange,
                range = 40f..120f
            )

            switchItem(
                title = stringResource(R.string.aod_24h_format),
                checked = state.aod24hFormat,
                onCheckedChange = actions.onAod24hFormatChange
            )
        }

        // Date Settings
        switchItem(
            title = stringResource(R.string.aod_show_date),
            checked = state.aodShowDate,
            onCheckedChange = actions.onAodShowDateChange
        )

        if (state.aodShowDate) {
            sliderItem(
                title = stringResource(R.string.aod_font_size_date),
                description = "${((state.aodFontSizeDate - 10) * 100 / 20)}%",
                value = state.aodFontSizeDate,
                onValueChange = actions.onAodFontSizeDateChange,
                range = 10f..30f
            )
        }

        // Advanced Options
        switchItem(
            title = stringResource(R.string.aod_show_shortcuts),
            description = stringResource(R.string.aod_show_shortcuts_desc),
            checked = state.aodShowShortcuts,
            onCheckedChange = actions.onAodShowShortcutsChange
        )

        sliderItem(
            title = stringResource(R.string.aod_dim_amount),
            description = "${state.aodDimAmount}%",
            value = state.aodDimAmount,
            onValueChange = actions.onAodDimAmountChange,
            range = 0f..80f
        )
    }
}
