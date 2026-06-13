package com.monospace.battery.ui.components.history

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.monospace.battery.R
import com.monospace.battery.data.models.ChargeSession
import com.monospace.battery.data.models.LocalBatteryState
import com.monospace.battery.data.models.LocalSettingsActions
import com.monospace.battery.data.models.LocalSettingsState
import com.monospace.battery.ui.components.BannerActionCard
import com.monospace.battery.ui.components.SettingsSection
import com.monospace.battery.ui.theme.Font

@Composable
fun ChargeSessionsSection(
    sessions: List<ChargeSession>
) {
    val isPremium = LocalSettingsState.current.isWidgetsPurchased

    var limit by remember { mutableIntStateOf(5) }
    val displayedSessions = if (isPremium) sessions.take(limit) else sessions.take(3)

    SettingsSection(stringResource(R.string.history_sessions_title)) {
        customItem {
            if (sessions.isEmpty()) {
                EmptySessionsView()
            } else {
                SessionsListCard(
                    sessions = displayedSessions,
                    totalCount = sessions.size,
                    limit = limit,
                    onShowMoreClick = { limit += 5 }
                )
            }
        }
    }
}

@Composable
private fun EmptySessionsView() {
    Text(
        text = stringResource(R.string.history_no_sessions),
        modifier = Modifier.padding(vertical = 16.dp, horizontal = 24.dp),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodyMedium,
        fontFamily = Font.AzeretMonoLight
    )
}

@Composable
private fun SessionsListCard(
    sessions: List<ChargeSession>,
    totalCount: Int,
    limit: Int,
    onShowMoreClick: () -> Unit
) {
    val isPremium = LocalSettingsState.current.isWidgetsPurchased
    val currentLevel = LocalBatteryState.current.level

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column {
            sessions.forEachIndexed { index, session ->
                ChargeSessionItem(
                    session = session,
                    currentLevel = currentLevel,
                    showDivider = index < sessions.size - 1
                )
            }

            if (!isPremium && totalCount > 3) {
                PremiumUpgradeBanner()
            } else if (isPremium && totalCount > limit) {
                ShowMoreButton(
                    remainingCount = totalCount - limit,
                    onClick = onShowMoreClick
                )
            }
        }
    }
}

@Composable
private fun PremiumUpgradeBanner() {
    val onUpgradeClick = LocalSettingsActions.current.onUnlockClick

    BannerActionCard(
        title = stringResource(R.string.history_unlock_full),
        description = stringResource(R.string.history_unlock_full_desc),
        icon = Icons.Default.Lock,
        onClick = onUpgradeClick,
        shape = RoundedCornerShape(
            bottomStart = 28.dp,
            bottomEnd = 28.dp
        )
    )
}

@Composable
private fun ShowMoreButton(
    remainingCount: Int,
    onClick: () -> Unit
) {
    val nextBatch = minOf(5, remainingCount)

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 16.dp)
            .padding(horizontal = 16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            contentColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Text(
            text = stringResource(R.string.history_show_more, nextBatch).uppercase(),
            fontFamily = Font.AzeretMonoLight,
            style = MaterialTheme.typography.labelLarge
        )
    }
}
