package com.monospace.battery.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import com.monospace.battery.R
import com.monospace.battery.helpers.SharedPreferences
import com.monospace.battery.helpers.WidgetsUtils
import com.monospace.battery.ui.components.SettingsItem
import com.monospace.battery.ui.components.SettingsSectionTitle
import com.monospace.battery.ui.components.SettingsSliderItem
import com.monospace.battery.ui.components.SettingsSwitchItem
import com.monospace.battery.ui.theme.BatteryTheme

@Composable
fun SettingsScreen(
    onUnlockClick: () -> Unit
) {
    val context = LocalContext.current
    val isWidgetsPurchased by remember { mutableStateOf(WidgetsUtils.isWidgetsPurchased(context)) }
    val prefs = remember { SharedPreferences(context) }

    var healthyChargeEnabled by remember {
        mutableStateOf(prefs.getBoolean(SharedPreferences.ALERTS_PREFS, SharedPreferences.KEY_HEALTHY_CHARGE))
    }
    var fullChargeEnabled by remember {
        mutableStateOf(prefs.getBoolean(SharedPreferences.ALERTS_PREFS, SharedPreferences.KEY_FULL_CHARGE))
    }
    var tempAlertEnabled by remember {
        mutableStateOf(prefs.getBoolean(SharedPreferences.ALERTS_PREFS, SharedPreferences.KEY_TEMP_ALERT))
    }
    var healthyChargeLevel by remember {
        mutableStateOf(prefs.getInt(SharedPreferences.ALERTS_PREFS, SharedPreferences.KEY_HEALTHY_CHARGE_LEVEL, 80))
    }

    val versionName = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.0"
        } catch (_: Exception) {
            "1.0.0"
        }
    }

    val openGooglePlay = {
        val appPackageName = context.packageName
        val marketUri = "market://details?id=$appPackageName".toUri()
        val googlePlayUri = "https://play.google.com/store/apps/details?id=$appPackageName".toUri()

        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, marketUri))
        } catch (_: Exception) {
            context.startActivity(Intent(Intent.ACTION_VIEW, googlePlayUri))
        }
    }

    SettingsContent(
        isWidgetsPurchased = isWidgetsPurchased,
        versionName = versionName,
        healthyChargeEnabled = healthyChargeEnabled,
        fullChargeEnabled = fullChargeEnabled,
        tempAlertEnabled = tempAlertEnabled,
        healthyChargeLevel = healthyChargeLevel,
        onHealthyChargeChange = {
            healthyChargeEnabled = it
            prefs.setBoolean(SharedPreferences.ALERTS_PREFS, SharedPreferences.KEY_HEALTHY_CHARGE, it)
        },
        onFullChargeChange = {
            fullChargeEnabled = it
            prefs.setBoolean(SharedPreferences.ALERTS_PREFS, SharedPreferences.KEY_FULL_CHARGE, it)
        },
        onTempAlertChange = {
            tempAlertEnabled = it
            prefs.setBoolean(SharedPreferences.ALERTS_PREFS, SharedPreferences.KEY_TEMP_ALERT, it)
        },
        onHealthyChargeLevelChange = {
            healthyChargeLevel = it
            prefs.setInt(SharedPreferences.ALERTS_PREFS, SharedPreferences.KEY_HEALTHY_CHARGE_LEVEL, it)
        },
        onUnlockClick = onUnlockClick,
        onUpdateClick = openGooglePlay,
        onRateClick = openGooglePlay
    )
}

@Composable
fun SettingsContent(
    isWidgetsPurchased: Boolean,
    versionName: String,
    healthyChargeEnabled: Boolean,
    fullChargeEnabled: Boolean,
    tempAlertEnabled: Boolean,
    healthyChargeLevel: Int,
    onHealthyChargeChange: (Boolean) -> Unit,
    onFullChargeChange: (Boolean) -> Unit,
    onTempAlertChange: (Boolean) -> Unit,
    onHealthyChargeLevelChange: (Int) -> Unit,
    onUnlockClick: () -> Unit,
    onUpdateClick: () -> Unit,
    onRateClick: () -> Unit
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
                checked = healthyChargeEnabled,
                onCheckedChange = onHealthyChargeChange
            )

            if (healthyChargeEnabled) {
                SettingsSliderItem(
                    title = stringResource(R.string.settings_healthy_charge),
                    value = healthyChargeLevel,
                    onValueChange = onHealthyChargeLevelChange
                )
            }

            SettingsSwitchItem(
                title = stringResource(R.string.settings_full_charge),
                description = stringResource(R.string.settings_full_charge_desc),
                checked = fullChargeEnabled,
                onCheckedChange = onFullChargeChange
            )

            SettingsSwitchItem(
                title = stringResource(R.string.settings_temp_alert),
                description = stringResource(R.string.settings_temp_alert_desc),
                checked = tempAlertEnabled,
                onCheckedChange = onTempAlertChange
            )

            SettingsSectionTitle(title = stringResource(R.string.action_settings))

            if (!isWidgetsPurchased) {
                SettingsItem(
                    title = stringResource(R.string.settings_unlock_full),
                    description = stringResource(R.string.widget_purchase_description),
                    onClick = onUnlockClick
                )
            }

            SettingsItem(
                title = stringResource(R.string.settings_software_update),
                description = stringResource(R.string.settings_version, versionName),
                onClick = onUpdateClick
            )

            SettingsItem(
                title = stringResource(R.string.settings_rate_us),
                onClick = onRateClick
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    BatteryTheme {
        SettingsContent(
            isWidgetsPurchased = false,
            versionName = "1.0.0",
            healthyChargeEnabled = true,
            fullChargeEnabled = false,
            tempAlertEnabled = true,
            healthyChargeLevel = 80,
            onHealthyChargeChange = {},
            onFullChargeChange = {},
            onTempAlertChange = {},
            onHealthyChargeLevelChange = {},
            onUnlockClick = {},
            onUpdateClick = {},
            onRateClick = {}
        )
    }
}
