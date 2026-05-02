package com.monospace.battery.data.models

data class SettingsUiState(
    val isWidgetsPurchased: Boolean,
    val versionName: String,
    val healthyChargeEnabled: Boolean,
    val tempAlertEnabled: Boolean,
    val lowBatteryEnabled: Boolean,
    val healthyChargeLevel: Int,
    val lowBatteryLevel: Int
)

data class SettingsUiActions(
    val onHealthyChargeChange: (Boolean) -> Unit,
    val onTempAlertChange: (Boolean) -> Unit,
    val onLowBatteryChange: (Boolean) -> Unit,
    val onHealthyChargeLevelChange: (Int) -> Unit,
    val onLowBatteryLevelChange: (Int) -> Unit,
    val onUnlockClick: () -> Unit,
    val onUpdateClick: () -> Unit,
    val onRateClick: () -> Unit
)
