package com.monospace.battery.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.monospace.battery.data.models.BatteryState
import com.monospace.battery.data.models.LocalBatteryState
import com.monospace.battery.data.models.LocalSettingsActions
import com.monospace.battery.data.models.LocalSettingsState
import com.monospace.battery.data.models.SettingsUiActions
import com.monospace.battery.data.models.SettingsUiState
import com.monospace.battery.ui.components.aod.AodBannerSection
import com.monospace.battery.ui.components.aod.AodClockStyleSelector
import com.monospace.battery.ui.components.aod.AodColorSelector
import com.monospace.battery.ui.components.aod.AodMeterStyleSelector
import com.monospace.battery.ui.components.aod.AodPreviewHeader
import com.monospace.battery.ui.components.aod.AodSettingsSectionContent
import com.monospace.battery.ui.theme.BatteryTheme
import com.monospace.battery.ui.utils.getAodColor

@Composable
fun AodScreen() {
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        if (isLandscape) {
            LandscapeLayout()
        } else {
            PortraitLayout()
        }
    }
}

@Composable
private fun PortraitLayout() {
    Column(modifier = Modifier.fillMaxSize()) {
        // Fixed Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            AodPreviewHeader()
            AodBannerSection()
        }

        // Scrollable content
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            aodSettingsItems()
        }
    }
}

@Composable
private fun LandscapeLayout() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item { AodPreviewHeader() }
        item { AodBannerSection() }
        aodSettingsItems()
    }
}

private fun LazyListScope.aodSettingsItems() {
    item {
        val state = LocalSettingsState.current
        val actions = LocalSettingsActions.current

        AodClockStyleSelector(
            selectedIndex = state.aodClockStyle,
            is24h = state.aod24hFormat,
            onSelect = actions.onAodClockStyleChange
        )
    }

    item {
        val state = LocalSettingsState.current
        val actions = LocalSettingsActions.current

        AodMeterStyleSelector(
            selectedIndex = state.aodMeterStyle,
            selectedColor = getAodColor(state.aodColor),
            onSelect = actions.onAodMeterStyleChange
        )
    }

    item {
        val state = LocalSettingsState.current
        val actions = LocalSettingsActions.current

        AodColorSelector(
            selectedColor = getAodColor(state.aodColor),
            onColorSelect = actions.onAodColorChange
        )
    }

    item {
        AodSettingsSectionContent()
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
