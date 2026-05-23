package com.monospace.battery.ui.components

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
import com.monospace.battery.ui.theme.Font

@Composable
fun ChargeSessionsSection(
    sessions: List<ChargeSession>,
    isPremium: Boolean,
    currentLevel: Int,
    onUpgradeClick: () -> Unit
) {
    var limit by remember { mutableIntStateOf(5) }

    val displayedSessions = when {
        !isPremium -> sessions.take(3)
        else -> sessions.take(limit)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        SectionTitle(stringResource(R.string.history_sessions_title))

        if (sessions.isEmpty()) {
            Text(
                text = stringResource(R.string.history_no_sessions),
                modifier = Modifier.padding(24.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = Font.AzeretMonoLight
            )
        } else {
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
                    displayedSessions.forEachIndexed { index, session ->
                        ChargeSessionItem(
                            session = session,
                            currentLevel = currentLevel,
                            showDivider = index < displayedSessions.size - 1
                        )
                    }

                    if (!isPremium && sessions.size > 3) {
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
                    } else if (isPremium && sessions.size > limit) {
                        val nextBatch = minOf(5, sessions.size - limit)

                        Button(
                            onClick = { limit += 5 },
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
                }
            }
        }
    }
}
