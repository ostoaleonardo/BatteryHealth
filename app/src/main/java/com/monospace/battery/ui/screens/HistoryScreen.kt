package com.monospace.battery.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
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
import com.monospace.battery.data.models.LocalBatteryState
import com.monospace.battery.data.models.LocalSettingsActions
import com.monospace.battery.data.models.LocalSettingsState
import com.monospace.battery.ui.components.SettingsSection
import com.monospace.battery.ui.components.history.BatteryTipsSection
import com.monospace.battery.ui.components.history.ChargeSessionsSection
import com.monospace.battery.ui.components.history.ChargerAnalysisSection
import com.monospace.battery.ui.components.history.ConsumptionChartSection
import com.monospace.battery.ui.viewmodels.HistoryViewModel

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = viewModel()
) {
    val state = LocalSettingsState.current
    val actions = LocalSettingsActions.current
    val batteryState = LocalBatteryState.current
    val isPremium = state.isWidgetsPurchased

    val history by viewModel.history.collectAsState()
    val sessions by viewModel.sessions.collectAsState()
    val chargerStats by viewModel.chargerStats.collectAsState()
    val currentTip by viewModel.currentTip.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. Active Monitoring Switch
            if (!state.anyAlertEnabled) {
                item {
                    SettingsSection(stringResource(R.string.alerts_category_monitoring)) {
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
                ConsumptionChartSection(history)
            }

            // 3. Charger Analysis (Pro feature)
            item {
                ChargerAnalysisSection(chargerStats, isPremium, actions.onUnlockClick)
            }

            // 4. Charging Sessions
            item {
                ChargeSessionsSection(sessions, isPremium, batteryState.level, actions.onUnlockClick)
            }

            // 5. Battery Tips
            item {
                BatteryTipsSection(currentTip.second, viewModel::nextTip)
            }
        }
    }
}
