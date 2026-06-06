package com.monospace.battery.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.monospace.battery.data.models.LocalSettingsActions
import com.monospace.battery.data.models.LocalSettingsState
import com.monospace.battery.data.models.SettingsUiActions
import com.monospace.battery.data.models.SettingsUiState
import com.monospace.battery.ui.components.alerts.AlertsBannerSection
import com.monospace.battery.ui.components.alerts.ChargeAlertsSection
import com.monospace.battery.ui.components.alerts.DischargeAlertsSection
import com.monospace.battery.ui.components.alerts.SafetyAlertsSection
import com.monospace.battery.ui.theme.BatteryTheme

@Composable
fun AlertsScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Upsell / Permission Banners
            AlertsBannerSection()

            // 1. Charge Alarms
            ChargeAlertsSection()

            // 2. Discharge Alarms
            DischargeAlertsSection()

            // 3. Safety Alarms
            SafetyAlertsSection()
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AlertsScreenPreview() {
    BatteryTheme {
        CompositionLocalProvider(
            LocalSettingsState provides SettingsUiState(isWidgetsPurchased = true),
            LocalSettingsActions provides SettingsUiActions()
        ) {
            AlertsScreen()
        }
    }
}
