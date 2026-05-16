package com.monospace.battery.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.monospace.battery.R
import com.monospace.battery.data.models.SettingsUiActions
import com.monospace.battery.data.models.SettingsUiState
import com.monospace.battery.ui.components.PremiumCard
import com.monospace.battery.ui.components.SettingsSection

@Composable
fun AlertsScreen(
    state: SettingsUiState,
    actions: SettingsUiActions
) {
    val isPremium = state.isWidgetsPurchased

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            if (!isPremium) {
                PremiumCard(onUpgradeClick = actions.onUnlockClick)
            }

            // 1. Charge Alarms
            SettingsSection(stringResource(R.string.alerts_category_charge)) {
                switchItem(
                    title = stringResource(R.string.settings_healthy_charge),
                    description = stringResource(R.string.settings_healthy_charge_desc),
                    checked = state.healthyChargeEnabled,
                    enabled = isPremium,
                    onCheckedChange = actions.onHealthyChargeChange
                )

                if (state.healthyChargeEnabled) {
                    sliderItem(
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

            // 2. Discharge Alarms
            Spacer(modifier = Modifier.height(24.dp))
            SettingsSection(stringResource(R.string.alerts_category_discharge)) {
                switchItem(
                    title = stringResource(R.string.settings_low_battery),
                    description = stringResource(R.string.settings_low_battery_desc),
                    checked = state.lowBatteryEnabled,
                    enabled = isPremium,
                    onCheckedChange = actions.onLowBatteryChange
                )

                if (state.lowBatteryEnabled) {
                    sliderItem(
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

            // 3. Safety Alarms
            Spacer(modifier = Modifier.height(24.dp))
            SettingsSection(stringResource(R.string.alerts_category_safety)) {
                switchItem(
                    title = stringResource(R.string.settings_temp_alert),
                    description = stringResource(R.string.settings_temp_alert_desc),
                    checked = state.tempAlertEnabled,
                    enabled = isPremium,
                    onCheckedChange = actions.onTempAlertChange
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
