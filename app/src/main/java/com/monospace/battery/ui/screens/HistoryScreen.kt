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
import com.monospace.battery.data.models.SettingsUiActions
import com.monospace.battery.data.models.SettingsUiState
import com.monospace.battery.ui.components.BatteryChart
import com.monospace.battery.ui.components.BatteryTipsSection
import com.monospace.battery.ui.components.ChargeSessionsSection
import com.monospace.battery.ui.components.ChargerAnalysisSection
import com.monospace.battery.ui.components.SectionTitle
import com.monospace.battery.ui.components.SettingsSection
import com.monospace.battery.ui.viewmodels.HistoryViewModel

@Composable
fun HistoryScreen(
    isPremium: Boolean = false,
    currentLevel: Int = 0,
    onUpgradeClick: () -> Unit = {},
    state: SettingsUiState,
    actions: SettingsUiActions,
    viewModel: HistoryViewModel = viewModel()
) {
    val history by viewModel.history.collectAsState()
    val sessions by viewModel.sessions.collectAsState()
    val chargerStats by viewModel.chargerStats.collectAsState()
    val currentTip by viewModel.currentTip.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            // 1. Active Monitoring Switch
            if (!state.anyAlertEnabled) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    SettingsSection(title = stringResource(R.string.alerts_category_monitoring)) {
                        switchItem(
                            title = stringResource(R.string.settings_active_monitoring),
                            description = stringResource(R.string.settings_active_monitoring_desc),
                            checked = state.activeMonitoringEnabled,
                            onCheckedChange = actions.onActiveMonitoringChange
                        )
                    }
                }
            }

            // 2. Consumption Chart
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionTitle(stringResource(R.string.history_consumption_title))
                BatteryChart(history)
            }

            // 3. Charger Analysis (Pro feature)
            item {
                Spacer(modifier = Modifier.height(24.dp))
                ChargerAnalysisSection(chargerStats, isPremium, onUpgradeClick)
            }

            // 4. Charging Sessions
            item {
                Spacer(modifier = Modifier.height(24.dp))
                ChargeSessionsSection(sessions, isPremium, currentLevel, onUpgradeClick)
            }

            // 5. Battery Tips
            item {
                Spacer(modifier = Modifier.height(24.dp))
                BatteryTipsSection(currentTip.second, viewModel::nextTip)
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
