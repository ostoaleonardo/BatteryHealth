package com.monospace.battery.ui.components.history

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.monospace.battery.R
import com.monospace.battery.data.models.BatteryHistoryEntry
import com.monospace.battery.ui.components.SettingsSection

@Composable
fun ConsumptionChartSection(
    history: List<BatteryHistoryEntry>
) {
    SettingsSection(stringResource(R.string.history_consumption_title)) {
        customItem {
            BatteryChart(history)
        }
    }
}
