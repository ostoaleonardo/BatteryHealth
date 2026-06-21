package com.monospace.battery.ui.components.alerts

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.monospace.battery.R
import com.monospace.battery.data.models.LocalSettingsActions
import com.monospace.battery.data.models.LocalSettingsState
import com.monospace.battery.ui.components.common.section.Section

@Composable
fun ChargeAlertsSection() {
    val state = LocalSettingsState.current
    val actions = LocalSettingsActions.current
    val isPremium = state.isWidgetsPurchased

    Section(stringResource(R.string.alerts_category_charge)) {
        switchItem(
            title = stringResource(R.string.settings_healthy_charge),
            description = stringResource(R.string.settings_healthy_charge_desc),
            checked = state.healthyChargeEnabled,
            enabled = isPremium,
            onCheckedChange = actions.onHealthyChargeChange
        )

        if (state.healthyChargeEnabled) {
            sliderItem(
                description = "${state.healthyChargeLevel}%",
                value = state.healthyChargeLevel,
                enabled = isPremium,
                onValueChange = actions.onHealthyChargeLevelChange
            )
        }

        switchItem(
            title = stringResource(R.string.settings_slow_charge),
            description = stringResource(R.string.settings_slow_charge_desc),
            checked = state.slowChargeEnabled,
            enabled = isPremium,
            onCheckedChange = actions.onSlowChargeChange
        )
    }
}
