package com.monospace.battery.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.monospace.battery.R
import com.monospace.battery.ui.components.BatteryChart
import com.monospace.battery.ui.components.BatteryTipsSection
import com.monospace.battery.ui.components.ChargeSessionsSection
import com.monospace.battery.ui.components.ChargerAnalysisSection
import com.monospace.battery.ui.components.SectionTitle
import com.monospace.battery.ui.components.SotCard

@Composable
fun HistoryScreen(
    isPremium: Boolean = false,
    currentLevel: Int = 0,
    onUpgradeClick: () -> Unit = {},
    viewModel: HistoryViewModel = viewModel()
) {
    val history by viewModel.history.collectAsState()
    val sessions by viewModel.sessions.collectAsState()
    val sot by viewModel.sot.collectAsState()
    val batteryUsed by viewModel.batteryUsed.collectAsState()
    val drainRate by viewModel.activeDrainRate.collectAsState()
    val estimatedSot by viewModel.estimatedFullSot.collectAsState()
    val chargerStats by viewModel.chargerStats.collectAsState()
    val currentTip by viewModel.currentTip.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. SOT Diagnostics
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SotCard(
                    sot = sot,
                    batteryUsed = batteryUsed,
                    drainRate = drainRate,
                    estimatedFullSot = estimatedSot
                )
            }

            // 2. Consumption Chart (Animated)
            item {
                Spacer(modifier = Modifier.height(24.dp))
                SectionTitle(stringResource(R.string.history_consumption_title))
                BatteryChart(history)
            }

            // 3. Charger Analysis (Pro feature)
            item {
                Spacer(modifier = Modifier.height(24.dp))
                ChargerAnalysisSection(
                    stats = chargerStats,
                    isPremium = isPremium,
                    onUpgradeClick = onUpgradeClick
                )
            }

            // 4. Charging Sessions
            item {
                Spacer(modifier = Modifier.height(24.dp))
                ChargeSessionsSection(
                    sessions = sessions,
                    isPremium = isPremium,
                    currentLevel = currentLevel,
                    onUpgradeClick = onUpgradeClick
                )
            }

            // 5. Battery Tips (Free Value-Add)
            item {
                Spacer(modifier = Modifier.height(24.dp))
                BatteryTipsSection(
                    tip = currentTip.second,
                    onNextTip = viewModel::nextTip
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
