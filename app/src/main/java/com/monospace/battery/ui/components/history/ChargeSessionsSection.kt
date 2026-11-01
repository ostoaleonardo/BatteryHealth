package com.monospace.battery.ui.components.history

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.monospace.battery.R
import com.monospace.battery.data.models.ChargeSession
import com.monospace.battery.data.models.LocalBatteryState
import com.monospace.battery.data.models.LocalSettingsActions
import com.monospace.battery.data.models.LocalSettingsState
import com.monospace.battery.ui.components.common.section.BaseItem
import com.monospace.battery.ui.components.common.section.Section

private const val FREE_SESSIONS_LIMIT = 3
private const val SESSIONS_PAGE_SIZE = 5

@Composable
fun ChargeSessionsSection(
    sessions: List<ChargeSession>
) {
    val isPremium = LocalSettingsState.current.isWidgetsPurchased
    val onUpgradeClick = LocalSettingsActions.current.onUnlockClick
    val currentLevel = LocalBatteryState.current.level

    var limit by remember { mutableIntStateOf(SESSIONS_PAGE_SIZE) }
    val displayedSessions = if (isPremium) sessions.take(limit) else sessions.take(FREE_SESSIONS_LIMIT)

    Section(stringResource(R.string.history_sessions_title)) {
        if (sessions.isEmpty()) {
            customItem { BaseItem(description = stringResource(R.string.history_no_sessions)) }
        } else {
            displayedSessions.forEach { session ->
                customItem { ChargeSessionItem(session = session, currentLevel = currentLevel) }
            }

            if (!isPremium && sessions.size > FREE_SESSIONS_LIMIT) {
                customItem {
                    BaseItem(
                        title = stringResource(R.string.history_unlock_full),
                        description = stringResource(R.string.history_unlock_full_desc),
                        onClick = onUpgradeClick,
                        trailingContent = { Icon(imageVector = Icons.Default.Lock, contentDescription = null) }
                    )
                }
            } else if (isPremium && sessions.size > limit) {
                val nextBatch = minOf(SESSIONS_PAGE_SIZE, sessions.size - limit)

                item(
                    title = stringResource(R.string.history_show_more, nextBatch),
                    iconRes = R.drawable.arrow_drop_down,
                    onClick = { limit += SESSIONS_PAGE_SIZE }
                )
            }
        }
    }
}
