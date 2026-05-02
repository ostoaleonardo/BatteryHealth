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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.monospace.battery.R
import com.monospace.battery.helpers.AppUtils
import com.monospace.battery.helpers.SharedPreferences
import com.monospace.battery.helpers.WidgetsUtils
import com.monospace.battery.ui.components.SettingsItem
import com.monospace.battery.ui.components.SettingsItemPosition
import com.monospace.battery.ui.components.SettingsSectionTitle
import com.monospace.battery.ui.components.SettingsSliderItem
import com.monospace.battery.ui.components.SettingsSwitchItem
import com.monospace.battery.ui.theme.BatteryTheme

data class SettingsUiState(
    val isWidgetsPurchased: Boolean,
    val versionName: String,
    val healthyChargeEnabled: Boolean,
    val tempAlertEnabled: Boolean,
    val healthyChargeLevel: Int
)

data class SettingsUiActions(
    val onHealthyChargeChange: (Boolean) -> Unit,
    val onTempAlertChange: (Boolean) -> Unit,
    val onHealthyChargeLevelChange: (Int) -> Unit,
    val onUnlockClick: () -> Unit,
    val onUpdateClick: () -> Unit,
    val onRateClick: () -> Unit
)

@Composable
fun SettingsScreen(
    onUnlockClick: () -> Unit
) {
    val context = LocalContext.current
    val isWidgetsPurchased by remember { mutableStateOf(WidgetsUtils.isWidgetsPurchased(context)) }
    val prefs = remember { SharedPreferences(context) }

    val healthyChargeEnabledState = remember {
        mutableStateOf(
            prefs.getBoolean(
                SharedPreferences.ALERTS_PREFS,
                SharedPreferences.KEY_HEALTHY_CHARGE
            )
        )
    }
    val tempAlertEnabledState = remember {
        mutableStateOf(
            prefs.getBoolean(
                SharedPreferences.ALERTS_PREFS,
                SharedPreferences.KEY_TEMP_ALERT
            )
        )
    }
    val healthyChargeLevelState = remember {
        mutableIntStateOf(
            prefs.getInt(
                SharedPreferences.ALERTS_PREFS,
                SharedPreferences.KEY_HEALTHY_CHARGE_LEVEL,
                80
            )
        )
    }

    val versionName = remember { AppUtils.getVersionName(context) }

    val state = SettingsUiState(
        isWidgetsPurchased = isWidgetsPurchased,
        versionName = versionName,
        healthyChargeEnabled = healthyChargeEnabledState.value,
        tempAlertEnabled = tempAlertEnabledState.value,
        healthyChargeLevel = healthyChargeLevelState.intValue
    )

    val actions = SettingsUiActions(
        onHealthyChargeChange = {
            healthyChargeEnabledState.value = it
            prefs.setBoolean(
                SharedPreferences.ALERTS_PREFS,
                SharedPreferences.KEY_HEALTHY_CHARGE,
                it
            )
        },
        onTempAlertChange = {
            tempAlertEnabledState.value = it
            prefs.setBoolean(SharedPreferences.ALERTS_PREFS, SharedPreferences.KEY_TEMP_ALERT, it)
        },
        onHealthyChargeLevelChange = {
            healthyChargeLevelState.intValue = it
            prefs.setInt(
                SharedPreferences.ALERTS_PREFS,
                SharedPreferences.KEY_HEALTHY_CHARGE_LEVEL,
                it
            )
        },
        onUnlockClick = onUnlockClick,
        onUpdateClick = { AppUtils.openPlayStore(context) },
        onRateClick = { AppUtils.openPlayStore(context) }
    )

    SettingsContent(state, actions)
}

@Composable
fun SettingsContent(
    state: SettingsUiState,
    actions: SettingsUiActions
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            SettingsSectionTitle(title = stringResource(R.string.settings_alerts_title))

            SettingsSwitchItem(
                title = stringResource(R.string.settings_healthy_charge),
                description = stringResource(R.string.settings_healthy_charge_desc),
                checked = state.healthyChargeEnabled,
                position = SettingsItemPosition.TOP,
                onCheckedChange = actions.onHealthyChargeChange
            )

            if (state.healthyChargeEnabled) {
                Spacer(modifier = Modifier.height(2.dp))
                SettingsSliderItem(
                    title = stringResource(R.string.settings_healthy_charge),
                    value = state.healthyChargeLevel,
                    position = SettingsItemPosition.MIDDLE,
                    onValueChange = actions.onHealthyChargeLevelChange
                )
            }

            Spacer(modifier = Modifier.height(2.dp))
            SettingsSwitchItem(
                title = stringResource(R.string.settings_temp_alert),
                description = stringResource(R.string.settings_temp_alert_desc),
                checked = state.tempAlertEnabled,
                position = SettingsItemPosition.BOTTOM,
                onCheckedChange = actions.onTempAlertChange
            )

            SettingsSectionTitle(title = stringResource(R.string.action_settings))

            if (!state.isWidgetsPurchased) {
                SettingsItem(
                    title = stringResource(R.string.settings_unlock_full),
                    description = stringResource(R.string.widget_purchase_description),
                    position = SettingsItemPosition.TOP,
                    onClick = actions.onUnlockClick
                )
                Spacer(modifier = Modifier.height(2.dp))
            }

            SettingsItem(
                title = stringResource(R.string.settings_software_update),
                description = stringResource(R.string.settings_version, state.versionName),
                position = if (state.isWidgetsPurchased) SettingsItemPosition.TOP else SettingsItemPosition.MIDDLE,
                onClick = actions.onUpdateClick
            )

            Spacer(modifier = Modifier.height(2.dp))
            SettingsItem(
                title = stringResource(R.string.settings_rate_us),
                position = SettingsItemPosition.BOTTOM,
                onClick = actions.onRateClick
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    BatteryTheme {
        SettingsContent(
            state = SettingsUiState(
                isWidgetsPurchased = false,
                versionName = "1.0.0",
                healthyChargeEnabled = true,
                tempAlertEnabled = true,
                healthyChargeLevel = 80
            ),
            actions = SettingsUiActions(
                onHealthyChargeChange = {},
                onTempAlertChange = {},
                onHealthyChargeLevelChange = {},
                onUnlockClick = {},
                onUpdateClick = {},
                onRateClick = {}
            )
        )
    }
}
