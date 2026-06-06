package com.monospace.battery.ui.components.alerts

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.monospace.battery.R
import com.monospace.battery.data.models.LocalSettingsActions
import com.monospace.battery.data.models.LocalSettingsState
import com.monospace.battery.ui.components.SettingsSection

@Composable
fun SafetyAlertsSection() {
    val state = LocalSettingsState.current
    val actions = LocalSettingsActions.current
    val isPremium = state.isWidgetsPurchased

    SettingsSection(stringResource(R.string.alerts_category_safety)) {
        switchItem(
            title = stringResource(R.string.settings_temp_alert),
            description = stringResource(R.string.settings_temp_alert_desc),
            checked = state.tempAlertEnabled,
            enabled = isPremium,
            onCheckedChange = actions.onTempAlertChange
        )
    }
}
