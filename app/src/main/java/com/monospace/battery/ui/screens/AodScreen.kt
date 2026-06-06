package com.monospace.battery.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.monospace.battery.data.models.BatteryState
import com.monospace.battery.data.models.LocalBatteryState
import com.monospace.battery.data.models.LocalSettingsActions
import com.monospace.battery.data.models.LocalSettingsState
import com.monospace.battery.data.models.SettingsUiActions
import com.monospace.battery.data.models.SettingsUiState
import com.monospace.battery.ui.components.aod.AodBannerSection
import com.monospace.battery.ui.components.aod.AodPreviewHeader
import com.monospace.battery.ui.components.aod.AodSettingsSection
import com.monospace.battery.ui.theme.BatteryTheme

@Composable
fun AodScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Fixed Header & Banners
            AodPreviewHeader()
            AodBannerSection()

            // 2. Scrollable Customization
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Customization Content
                item {
                    AodSettingsSection()
                }
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AodScreenPreview() {
    BatteryTheme {
        CompositionLocalProvider(
            LocalSettingsState provides SettingsUiState(isWidgetsPurchased = false),
            LocalSettingsActions provides SettingsUiActions(),
            LocalBatteryState provides BatteryState(level = 75)
        ) {
            AodScreen()
        }
    }
}
