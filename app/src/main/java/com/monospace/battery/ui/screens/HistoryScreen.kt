package com.monospace.battery.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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

@Composable
fun HistoryScreen(viewModel: HistoryViewModel = viewModel()) {
    val history by viewModel.history.collectAsState()
    val sessions by viewModel.sessions.collectAsState()
    val sot by viewModel.sot.collectAsState()
    val batteryUsed by viewModel.batteryUsed.collectAsState()
    val drainRate by viewModel.activeDrainRate.collectAsState()
    val estimatedSot by viewModel.estimatedFullSot.collectAsState()

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
                SectionTitle(stringResource(R.string.history_consumption_title))
                BatteryChart(history)
            }

            item {
                SectionTitle(stringResource(R.string.history_sessions_title))
            }

            if (sessions.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.history_no_sessions),
                        modifier = Modifier.padding(24.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
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
                            sessions.forEachIndexed { index, session ->
                                ChargeSessionItem(
                                    session = session,
                                    showDivider = index < sessions.size - 1
                                )
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
