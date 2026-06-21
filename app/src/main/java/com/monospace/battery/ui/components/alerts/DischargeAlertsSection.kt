package com.monospace.battery.ui.components.alerts

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.monospace.battery.R
import com.monospace.battery.data.models.LocalSettingsActions
import com.monospace.battery.data.models.LocalSettingsState
import com.monospace.battery.ui.components.common.section.Section

@Composable
fun DischargeAlertsSection() {
    val state = LocalSettingsState.current
    val actions = LocalSettingsActions.current
    val isPremium = state.isWidgetsPurchased

    Section(stringResource(R.string.alerts_category_discharge)) {
        switchItem(
            title = stringResource(R.string.settings_low_battery),
            description = stringResource(R.string.settings_low_battery_desc),
            checked = state.lowBatteryEnabled,
            enabled = isPremium,
            onCheckedChange = actions.onLowBatteryChange
        )

        if (state.lowBatteryEnabled) {
            sliderItem(
                description = "${state.lowBatteryLevel}%",
                value = state.lowBatteryLevel,
                enabled = isPremium,
                onValueChange = actions.onLowBatteryLevelChange,
                range = 0f..50f
            )
        }

        switchItem(
            title = stringResource(R.string.settings_fast_discharge),
            description = stringResource(R.string.settings_fast_discharge_desc),
            checked = state.fastDischargeEnabled,
            enabled = isPremium,
            onCheckedChange = actions.onFastDischargeChange
        )
    }
}
