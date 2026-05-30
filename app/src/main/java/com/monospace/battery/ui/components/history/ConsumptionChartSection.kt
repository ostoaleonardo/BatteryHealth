package com.monospace.battery.ui.components.history

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.monospace.battery.R
import com.monospace.battery.data.models.BatteryHistoryEntry
import com.monospace.battery.ui.components.SectionTitle

@Composable
fun ConsumptionChartSection(
    history: List<BatteryHistoryEntry>
) {
    Column {
        SectionTitle(stringResource(R.string.history_consumption_title))
        BatteryChart(history)
    }
}
