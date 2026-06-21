package com.monospace.battery.ui.components.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.monospace.battery.R
import com.monospace.battery.data.models.LocalSettingsActions
import com.monospace.battery.data.models.LocalSettingsState
import com.monospace.battery.ui.components.common.BannerActionCard

@Composable
fun SettingsBannerSection() {
    val state = LocalSettingsState.current
    val actions = LocalSettingsActions.current

    if (!state.isWidgetsPurchased) {
        BannerActionCard(
            title = stringResource(R.string.settings_unlock_full),
            description = stringResource(R.string.widget_purchase_description),
            icon = Icons.Default.Lock,
            onClick = actions.onUnlockClick,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}
