package com.monospace.battery.ui.components.history

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.monospace.battery.R
import com.monospace.battery.data.models.LocalBatteryState
import com.monospace.battery.data.models.LocalSettingsActions
import com.monospace.battery.data.models.LocalSettingsState
import com.monospace.battery.ui.components.common.section.Section

@Composable
fun ActiveMonitoringSection() {
    val state = LocalSettingsState.current
    val actions = LocalSettingsActions.current
    val battery = LocalBatteryState.current

    val isDisablingLocked = battery.isCharging && state.activeMonitoringEnabled

    Section {
        switchItem(
            title = stringResource(R.string.settings_active_monitoring),
            description = stringResource(R.string.settings_active_monitoring_desc),
            checked = state.activeMonitoringEnabled,
            enabled = !isDisablingLocked,
            onCheckedChange = { isEnabled ->
                if (!isDisablingLocked) {
                    actions.onActiveMonitoringChange(isEnabled)
                }
            }
        )
    }
}
