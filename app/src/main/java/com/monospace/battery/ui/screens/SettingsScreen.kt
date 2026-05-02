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
import com.monospace.battery.core.constants.AppConstants
import com.monospace.battery.core.utils.AppUtils
import com.monospace.battery.data.local.PreferenceManager
import com.monospace.battery.data.local.WidgetsUtils
import com.monospace.battery.data.models.SettingsUiActions
import com.monospace.battery.data.models.SettingsUiState
import com.monospace.battery.ui.components.SettingsSection
import com.monospace.battery.ui.theme.BatteryTheme

@Composable
fun SettingsScreen(
    onUnlockClick: () -> Unit
) {
    val context = LocalContext.current
    val isWidgetsPurchased by remember { mutableStateOf(WidgetsUtils.isWidgetsPurchased(context)) }
    val prefs = remember { PreferenceManager(context) }

    val healthyChargeEnabledState = remember {
        mutableStateOf(
            prefs.getBoolean(
                AppConstants.PREFS_ALERTS,
                AppConstants.KEY_HEALTHY_CHARGE_ENABLED
            )
        )
    }
    val tempAlertEnabledState = remember {
        mutableStateOf(
            prefs.getBoolean(
                AppConstants.PREFS_ALERTS,
                AppConstants.KEY_TEMP_ALERT_ENABLED
            )
        )
    }
    val healthyChargeLevelState = remember {
        mutableIntStateOf(
            prefs.getInt(
                AppConstants.PREFS_ALERTS,
                AppConstants.KEY_HEALTHY_CHARGE_LEVEL,
                80
            )
        )
    }
    val lowBatteryEnabledState = remember {
        mutableStateOf(
            prefs.getBoolean(
                AppConstants.PREFS_ALERTS,
                AppConstants.KEY_LOW_BATTERY_ENABLED
            )
        )
    }
    val lowBatteryLevelState = remember {
        mutableIntStateOf(
            prefs.getInt(
                AppConstants.PREFS_ALERTS,
                AppConstants.KEY_LOW_BATTERY_LEVEL,
                20
            )
        )
    }

    val versionName = remember { AppUtils.getVersionName(context) }

    val state = SettingsUiState(
        isWidgetsPurchased = isWidgetsPurchased,
        versionName = versionName,
        healthyChargeEnabled = healthyChargeEnabledState.value,
        tempAlertEnabled = tempAlertEnabledState.value,
        lowBatteryEnabled = lowBatteryEnabledState.value,
        healthyChargeLevel = healthyChargeLevelState.intValue,
        lowBatteryLevel = lowBatteryLevelState.intValue
    )

    val actions = SettingsUiActions(
        onHealthyChargeChange = {
            healthyChargeEnabledState.value = it
            prefs.setBoolean(
                AppConstants.PREFS_ALERTS,
                AppConstants.KEY_HEALTHY_CHARGE_ENABLED,
                it
            )
        },
        onTempAlertChange = {
            tempAlertEnabledState.value = it
            prefs.setBoolean(
                AppConstants.PREFS_ALERTS,
                AppConstants.KEY_TEMP_ALERT_ENABLED,
                it
            )
        },
        onLowBatteryChange = {
            lowBatteryEnabledState.value = it
            prefs.setBoolean(
                AppConstants.PREFS_ALERTS,
                AppConstants.KEY_LOW_BATTERY_ENABLED,
                it
            )
        },
        onHealthyChargeLevelChange = {
            healthyChargeLevelState.intValue = it
            prefs.setInt(
                AppConstants.PREFS_ALERTS,
                AppConstants.KEY_HEALTHY_CHARGE_LEVEL,
                it
            )
        },
        onLowBatteryLevelChange = {
            lowBatteryLevelState.intValue = it
            prefs.setInt(
                AppConstants.PREFS_ALERTS,
                AppConstants.KEY_LOW_BATTERY_LEVEL,
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
            SettingsSection(title = stringResource(R.string.action_settings)) {
                val unlockTitle = stringResource(R.string.settings_unlock_full)
                val unlockDesc = stringResource(R.string.widget_purchase_description)
                val updateTitle = stringResource(R.string.settings_software_update)
                val updateDesc = stringResource(R.string.settings_version, state.versionName)
                val rateTitle = stringResource(R.string.settings_rate_us)

                if (!state.isWidgetsPurchased) {
                    item(
                        title = unlockTitle,
                        description = unlockDesc,
                        onClick = actions.onUnlockClick
                    )
                }

                item(
                    title = updateTitle,
                    description = updateDesc,
                    onClick = actions.onUpdateClick
                )

                item(
                    title = rateTitle,
                    onClick = actions.onRateClick
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
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
                lowBatteryEnabled = true,
                healthyChargeLevel = 80,
                lowBatteryLevel = 20
            ),
            actions = SettingsUiActions(
                onHealthyChargeChange = {},
                onTempAlertChange = {},
                onLowBatteryChange = {},
                onHealthyChargeLevelChange = {},
                onLowBatteryLevelChange = {},
                onUnlockClick = {},
                onUpdateClick = {},
                onRateClick = {}
            )
        )
    }
}
