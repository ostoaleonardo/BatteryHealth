package com.monospace.battery.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.monospace.battery.R
import com.monospace.battery.ui.components.BatteryChart
import com.monospace.battery.ui.components.ChargeSessionItem
import com.monospace.battery.ui.components.SectionTitle
import com.monospace.battery.ui.components.SotCard
import com.monospace.battery.ui.theme.Font

@Composable
fun HistoryScreen(
    isPremium: Boolean = false,
    onUpgradeClick: () -> Unit = {},
    viewModel: HistoryViewModel = viewModel()
) {
    val history by viewModel.history.collectAsState()
    val sessions by viewModel.sessions.collectAsState()
    val sot by viewModel.sot.collectAsState()
    val batteryUsed by viewModel.batteryUsed.collectAsState()
    val drainRate by viewModel.activeDrainRate.collectAsState()
    val estimatedSot by viewModel.estimatedFullSot.collectAsState()

    val displayedSessions = if (isPremium) sessions else sessions.take(3)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SotCard(
                    sot = sot,
                    batteryUsed = batteryUsed,
                    drainRate = drainRate,
                    estimatedFullSot = estimatedSot
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                SectionTitle(stringResource(R.string.history_consumption_title))
                BatteryChart(history)
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                SectionTitle(stringResource(R.string.history_sessions_title))
            }

            if (sessions.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.history_no_sessions),
                        modifier = Modifier.padding(24.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = Font.AzeretMonoLight
                    )
                }
            } else {
                item {
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
                                    showDivider = index < displayedSessions.size - 1 || (!isPremium && sessions.size > 3)
                                )
                            }

                            if (!isPremium && sessions.size > 3) {
                                HistoryUpsell(onUpgradeClick)
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun HistoryUpsell(onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.history_unlock_full),
            style = MaterialTheme.typography.titleSmall,
            fontFamily = Font.AzeretMonoLight,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = stringResource(R.string.history_unlock_full_desc),
            style = MaterialTheme.typography.bodySmall,
            fontFamily = Font.AzeretMonoLight,
            modifier = Modifier.padding(vertical = 8.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = stringResource(R.string.premium_button).uppercase(),
                fontFamily = Font.AzeretMonoLight,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
