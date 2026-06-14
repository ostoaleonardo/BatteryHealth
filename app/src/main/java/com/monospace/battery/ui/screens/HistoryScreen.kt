package com.monospace.battery.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.monospace.battery.data.mocks.MockDataProvider
import com.monospace.battery.data.models.BatteryHistoryEntry
import com.monospace.battery.data.models.BatteryState
import com.monospace.battery.data.models.BatteryTip
import com.monospace.battery.data.models.ChargeSession
import com.monospace.battery.data.models.ChargerStats
import com.monospace.battery.data.models.LocalBatteryState
import com.monospace.battery.data.models.LocalSettingsActions
import com.monospace.battery.data.models.LocalSettingsState
import com.monospace.battery.data.models.SettingsUiActions
import com.monospace.battery.data.models.SettingsUiState
import com.monospace.battery.ui.components.history.ActiveMonitoringSection
import com.monospace.battery.ui.components.history.BatteryTipsSection
import com.monospace.battery.ui.components.history.ChargeSessionsSection
import com.monospace.battery.ui.components.history.ChargerAnalysisSection
import com.monospace.battery.ui.components.history.ConsumptionChartSection
import com.monospace.battery.ui.theme.BatteryTheme
import com.monospace.battery.ui.viewmodels.HistoryViewModel

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = viewModel()
) {
    val history by viewModel.history.collectAsState()
    val sessions by viewModel.sessions.collectAsState()
    val chargerStats by viewModel.chargerStats.collectAsState()
    val currentTip by viewModel.currentTip.collectAsState()

    HistoryScreen(
        history = history,
        sessions = sessions,
        chargerStats = chargerStats,
        currentTip = currentTip.second,
        onNextTip = viewModel::nextTip
    )
}

@Composable
private fun HistoryScreen(
    history: List<BatteryHistoryEntry>,
    sessions: List<ChargeSession>,
    chargerStats: List<ChargerStats>,
    currentTip: BatteryTip,
    onNextTip: () -> Unit
) {
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
            item {
                ActiveMonitoringSection()
            }

            // 2. Consumption Chart
            item {
                ConsumptionChartSection(history)
            }

            // 3. Charger Analysis
            item {
                ChargerAnalysisSection(chargerStats)
            }

            // 4. Charging Sessions
            item {
                ChargeSessionsSection(sessions)
            }

            // 5. Battery Tips
            item {
                BatteryTipsSection(
                    tip = currentTip,
                    onNextTip = onNextTip
                )
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun HistoryScreenPreview() {
    BatteryTheme {
        CompositionLocalProvider(
            LocalSettingsState provides SettingsUiState(isWidgetsPurchased = true),
            LocalSettingsActions provides SettingsUiActions(),
            LocalBatteryState provides BatteryState(level = 45)
        ) {
            HistoryScreen(
                history = MockDataProvider.dummyHistory,
                sessions = MockDataProvider.dummySessions,
                chargerStats = MockDataProvider.dummyChargerStats,
                currentTip = MockDataProvider.dummyTip,
                onNextTip = {}
            )
        }
    }
}
