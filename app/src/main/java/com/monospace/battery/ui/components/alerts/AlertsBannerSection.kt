package com.monospace.battery.ui.components.alerts

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
fun AlertsBannerSection() {
    val state = LocalSettingsState.current
    val actions = LocalSettingsActions.current
    val isPremium = state.isWidgetsPurchased
    val hasNotification = state.hasNotificationPermission

    if (!isPremium) {
        BannerActionCard(
            title = stringResource(R.string.settings_unlock_full),
            description = stringResource(R.string.widget_purchase_description),
            icon = Icons.Default.Lock,
            onClick = actions.onUnlockClick,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    } else if (!hasNotification) {
        BannerActionCard(
            title = stringResource(R.string.permission_notifications_title),
            description = stringResource(R.string.permission_notifications_description),
            icon = Icons.Default.NotificationsActive,
            onClick = actions.onNotificationPermissionRequest,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}
