package com.monospace.battery.ui.components.aod

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.monospace.battery.R
import com.monospace.battery.data.models.LocalSettingsActions
import com.monospace.battery.data.models.LocalSettingsState
import com.monospace.battery.ui.components.BannerActionCard

@Composable
fun AodBannerSection() {
    val state = LocalSettingsState.current
    val actions = LocalSettingsActions.current
    val isPremium = state.isWidgetsPurchased
    val showOverlayBanner = state.alwaysOnDisplayEnabled && !state.hasOverlayPermission

    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        if (!isPremium) {
            BannerActionCard(
                title = stringResource(R.string.premium_title),
                description = stringResource(R.string.premium_description),
                icon = Icons.Default.Lock,
                onClick = actions.onUnlockClick,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        } else if (showOverlayBanner) {
            BannerActionCard(
                title = stringResource(R.string.permission_overlay_title),
                description = stringResource(R.string.permission_overlay_desc),
                icon = Icons.Default.NotificationsActive,
                onClick = actions.onOverlayPermissionRequest,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}
