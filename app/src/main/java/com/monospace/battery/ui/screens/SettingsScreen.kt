package com.monospace.battery.ui.screens

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
import com.monospace.battery.ui.components.settings.AboutSection
import com.monospace.battery.ui.components.settings.SettingsBannerSection
import com.monospace.battery.ui.theme.BatteryTheme

@Composable
fun SettingsScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            SettingsBannerSection()

            AboutSection()
        }
    }
}

@Preview(showBackground = true, locale = "es")
@Composable
fun SettingsScreenPreview() {
    BatteryTheme {
        CompositionLocalProvider(
            LocalSettingsState provides SettingsUiState(
                isWidgetsPurchased = false,
                versionName = "1.1.2"
            ),
            LocalSettingsActions provides SettingsUiActions()
        ) {
            SettingsScreen()
        }
    }
}
