package com.monospace.battery.data.models

import androidx.compose.runtime.staticCompositionLocalOf

data class SettingsUiState(
    val isWidgetsPurchased: Boolean = false,
    val hasNotificationPermission: Boolean = false,
    val versionName: String = "",
    val healthyChargeEnabled: Boolean = false,
    val tempAlertEnabled: Boolean = false,
    val lowBatteryEnabled: Boolean = false,
    val fastDischargeEnabled: Boolean = false,
    val slowChargeEnabled: Boolean = false,
    val healthyChargeLevel: Int = 80,
    val lowBatteryLevel: Int = 20,
    val alwaysOnDisplayEnabled: Boolean = false,
    val activeMonitoringEnabled: Boolean = false,
    val hasOverlayPermission: Boolean = false,
    val aodClockStyle: Int = 0,
    val aodMeterStyle: Int = 0,
    val aodColor: Long = 0xFF00A25B,
    val aodShowDate: Boolean = true,
    val aodShowClock: Boolean = true,
    val aod24hFormat: Boolean = true,
    val aodFontSizeClock: Int = 80,
    val aodFontSizeDate: Int = 14,
    val aodDimAmount: Int = 0,
    val aodShowShortcuts: Boolean = false
) {
    val anyAlertEnabled: Boolean
        get() = healthyChargeEnabled || tempAlertEnabled || lowBatteryEnabled ||
                fastDischargeEnabled || slowChargeEnabled || alwaysOnDisplayEnabled
}

data class SettingsUiActions(
    val onHealthyChargeChange: (Boolean) -> Unit = {},
    val onTempAlertChange: (Boolean) -> Unit = {},
    val onLowBatteryChange: (Boolean) -> Unit = {},
    val onFastDischargeChange: (Boolean) -> Unit = {},
    val onSlowChargeChange: (Boolean) -> Unit = {},
    val onHealthyChargeLevelChange: (Int) -> Unit = {},
    val onLowBatteryLevelChange: (Int) -> Unit = {},
    val onAlwaysOnDisplayChange: (Boolean) -> Unit = {},
    val onActiveMonitoringChange: (Boolean) -> Unit = {},
    val onAodClockStyleChange: (Int) -> Unit = {},
    val onAodMeterStyleChange: (Int) -> Unit = {},
    val onAodColorChange: (Long) -> Unit = {},
    val onAodShowDateChange: (Boolean) -> Unit = {},
    val onAodShowClockChange: (Boolean) -> Unit = {},
    val onAod24hFormatChange: (Boolean) -> Unit = {},
    val onAodFontSizeClockChange: (Int) -> Unit = {},
    val onAodFontSizeDateChange: (Int) -> Unit = {},
    val onAodDimAmountChange: (Int) -> Unit = {},
    val onAodShowShortcutsChange: (Boolean) -> Unit = {},
    val onOverlayPermissionRequest: () -> Unit = {},
    val onUnlockClick: () -> Unit = {},
    val onNotificationPermissionRequest: () -> Unit = {},
    val onUpdateClick: () -> Unit = {},
    val onRateClick: () -> Unit = {}
)

val LocalSettingsState = staticCompositionLocalOf { SettingsUiState() }
val LocalSettingsActions = staticCompositionLocalOf { SettingsUiActions() }
