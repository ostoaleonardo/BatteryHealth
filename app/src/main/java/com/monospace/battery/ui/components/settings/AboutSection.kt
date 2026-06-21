package com.monospace.battery.ui.components.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.monospace.battery.R
import com.monospace.battery.data.models.LocalSettingsActions
import com.monospace.battery.data.models.LocalSettingsState
import com.monospace.battery.ui.components.common.section.Section

@Composable
fun AboutSection() {
    val state = LocalSettingsState.current
    val actions = LocalSettingsActions.current

    Section(stringResource(R.string.settings_about_app)) {
        val updateTitle = stringResource(R.string.settings_software_update)
        val updateDesc = stringResource(R.string.settings_version, state.versionName)
        val rateTitle = stringResource(R.string.settings_rate_us)

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
}
