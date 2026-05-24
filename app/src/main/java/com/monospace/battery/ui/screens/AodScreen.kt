package com.monospace.battery.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.monospace.battery.R
import com.monospace.battery.core.constants.Constants
import com.monospace.battery.data.models.SettingsUiActions
import com.monospace.battery.data.models.SettingsUiState
import com.monospace.battery.ui.components.AodPreviewCard
import com.monospace.battery.ui.components.AodStyleSelectors
import com.monospace.battery.ui.components.BannerActionCard
import com.monospace.battery.ui.components.SegmentOption
import com.monospace.battery.ui.components.SegmentedControl
import com.monospace.battery.ui.components.SettingsSection

@Composable
fun AodScreen(
    state: SettingsUiState,
    actions: SettingsUiActions,
    currentBatteryLevel: Int
) {
    val isPremium = state.isWidgetsPurchased
    val context = LocalContext.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Sticky Preview & Activation Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(bottom = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val themePrimary = MaterialTheme.colorScheme.primary
                val previewColor = if (state.aodColor == 0L) themePrimary else Color(state.aodColor)

                // Smartphone Preview (Always visible)
                AodPreviewCard(
                    level = currentBatteryLevel,
                    color = previewColor,
                    clockStyle = state.aodClockStyle,
                    meterStyle = state.aodMeterStyle,
                    showDate = state.aodShowDate,
                    showClock = state.aodShowClock,
                    showShortcuts = state.aodShowShortcuts,
                    is24h = state.aod24hFormat,
                    fontSizeClock = (state.aodFontSizeClock * 0.3f).toInt(),
                    fontSizeDate = (state.aodFontSizeDate * 0.4f).toInt()
                )

                // Activation Controls Row (Only for Premium)
                if (isPremium) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                            .padding(horizontal = 24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        val isAodEnabled = state.alwaysOnDisplayEnabled

                        // Reusable Segmented Control for Activation
                        SegmentedControl(
                            options = listOf(
                                SegmentOption(Constants.OFF, R.string.aod_disabled),
                                SegmentOption(Constants.ON, R.string.aod_enabled)
                            ),
                            selected = if (isAodEnabled) Constants.ON else Constants.OFF,
                            onSelected = { id ->
                                actions.onAlwaysOnDisplayChange(id == Constants.ON)
                            }
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        // Manual Try Button
                        Button(
                            onClick = {
                                val intent = Intent(context, AlwaysOnDisplayActivity::class.java)
                                context.startActivity(intent)
                            },
                            modifier = Modifier.size(40.dp),
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = stringResource(R.string.aod_try_now),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // 2. Scrollable Style Adjustments
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                if (!isPremium) {
                    item {
                        BannerActionCard(
                            title = stringResource(R.string.premium_title),
                            description = stringResource(R.string.premium_description),
                            icon = Icons.Default.Lock,
                            onClick = actions.onUnlockClick,
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 16.dp)
                        )
                    }
                } else if (state.alwaysOnDisplayEnabled && !state.hasOverlayPermission) {
                    item {
                        BannerActionCard(
                            title = stringResource(R.string.permission_overlay_title),
                            description = stringResource(R.string.permission_overlay_desc),
                            icon = Icons.Default.NotificationsActive,
                            onClick = actions.onOverlayPermissionRequest,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 16.dp)
                        )
                    }
                }

                // Style Selectors (Clock, Speedometer, Color)
                item {
                    AodStyleSelectors(
                        level = currentBatteryLevel,
                        clockStyle = state.aodClockStyle,
                        meterStyle = state.aodMeterStyle,
                        selectedColor = if (state.aodColor == 0L) MaterialTheme.colorScheme.primary else Color(
                            state.aodColor
                        ),
                        is24h = state.aod24hFormat,
                        onClockStyleChange = actions.onAodClockStyleChange,
                        onMeterStyleChange = actions.onAodMeterStyleChange,
                        onColorChange = actions.onAodColorChange
                    )
                }

                // Additional Preferences
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    SettingsSection(stringResource(R.string.aod_look_feel)) {
                        // Clock Settings
                        switchItem(
                            title = stringResource(R.string.aod_show_clock),
                            checked = state.aodShowClock,
                            onCheckedChange = actions.onAodShowClockChange
                        )

                        if (state.aodShowClock) {
                            sliderItem(
                                title = stringResource(R.string.aod_font_size_clock),
                                description = "${((state.aodFontSizeClock - 40) * 100 / 80)}%",
                                value = state.aodFontSizeClock,
                                onValueChange = actions.onAodFontSizeClockChange,
                                range = 40f..120f
                            )

                            switchItem(
                                title = stringResource(R.string.aod_24h_format),
                                checked = state.aod24hFormat,
                                enabled = true,
                                onCheckedChange = actions.onAod24hFormatChange
                            )
                        }

                        // Date Settings
                        switchItem(
                            title = stringResource(R.string.aod_show_date),
                            checked = state.aodShowDate,
                            enabled = true,
                            onCheckedChange = actions.onAodShowDateChange
                        )

                        if (state.aodShowDate) {
                            sliderItem(
                                title = stringResource(R.string.aod_font_size_date),
                                description = "${((state.aodFontSizeDate - 10) * 100 / 20)}%",
                                value = state.aodFontSizeDate,
                                onValueChange = actions.onAodFontSizeDateChange,
                                range = 10f..30f
                            )
                        }

                        // Advanced Options
                        switchItem(
                            title = stringResource(R.string.aod_show_shortcuts),
                            description = stringResource(R.string.aod_show_shortcuts_desc),
                            checked = state.aodShowShortcuts,
                            onCheckedChange = actions.onAodShowShortcutsChange
                        )

                        sliderItem(
                            title = stringResource(R.string.aod_dim_amount),
                            description = "${state.aodDimAmount}%",
                            value = state.aodDimAmount,
                            onValueChange = actions.onAodDimAmountChange,
                            range = 0f..80f
                        )
                    }
                }
            }
        }
    }
}
