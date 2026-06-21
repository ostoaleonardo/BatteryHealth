package com.monospace.battery.ui.viewmodels

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.monospace.battery.core.constants.Constants
import com.monospace.battery.data.local.PreferenceManager
import com.monospace.battery.data.local.db.BatteryDatabase
import com.monospace.battery.data.models.SettingsUiState
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = PreferenceManager(application)
    private val db = BatteryDatabase.getDatabase(application)
    private val dao = db.batteryDao()

    private val _uiState = mutableStateOf(loadInitialState())
    val uiState: State<SettingsUiState> = _uiState

    private fun loadInitialState(): SettingsUiState {
        return SettingsUiState(
            isWidgetsPurchased = false,
            hasNotificationPermission = false,
            versionName = "",
            hasOverlayPermission = false,
            healthyChargeEnabled = prefs.get(
                Constants.PREFS_ALERTS,
                Constants.KEY_HEALTHY_CHARGE_ENABLED,
                false
            ),
            tempAlertEnabled = prefs.get(
                Constants.PREFS_ALERTS,
                Constants.KEY_TEMP_ALERT_ENABLED,
                false
            ),
            lowBatteryEnabled = prefs.get(
                Constants.PREFS_ALERTS,
                Constants.KEY_LOW_BATTERY_ENABLED,
                false
            ),
            fastDischargeEnabled = prefs.get(
                Constants.PREFS_ALERTS,
                Constants.KEY_FAST_DISCHARGE_ENABLED,
                false
            ),
            slowChargeEnabled = prefs.get(
                Constants.PREFS_ALERTS,
                Constants.KEY_SLOW_CHARGE_ENABLED,
                false
            ),
            healthyChargeLevel = prefs.get(
                Constants.PREFS_ALERTS,
                Constants.KEY_HEALTHY_CHARGE_LEVEL,
                80
            ),
            lowBatteryLevel = prefs.get(
                Constants.PREFS_ALERTS,
                Constants.KEY_LOW_BATTERY_LEVEL,
                20
            ),
            alwaysOnDisplayEnabled = prefs.get(
                Constants.PREFS_ALERTS,
                Constants.KEY_AOD_ENABLED,
                false
            ),
            activeMonitoringEnabled = prefs.get(
                Constants.PREFS_ALERTS,
                Constants.KEY_ACTIVE_MONITORING,
                false
            ),
            aodClockStyle = prefs.get(
                Constants.PREFS_ALERTS,
                Constants.KEY_AOD_CLOCK_STYLE,
                0
            ),
            aodMeterStyle = prefs.get(
                Constants.PREFS_ALERTS,
                Constants.KEY_AOD_METER_STYLE,
                0
            ),
            aodColor = prefs.get(
                Constants.PREFS_ALERTS,
                Constants.KEY_AOD_COLOR,
                0xFF00A25B
            ),
            aodShowDate = prefs.get(
                Constants.PREFS_ALERTS,
                Constants.KEY_AOD_SHOW_DATE,
                true
            ),
            aodShowClock = prefs.get(
                Constants.PREFS_ALERTS,
                Constants.KEY_AOD_SHOW_CLOCK,
                true
            ),
            aod24hFormat = prefs.get(
                Constants.PREFS_ALERTS,
                Constants.KEY_AOD_24H_FORMAT,
                true
            ),
            aodFontSizeClock = prefs.get(
                Constants.PREFS_ALERTS,
                Constants.KEY_AOD_FONT_SIZE_CLOCK,
                80
            ),
            aodFontSizeDate = prefs.get(
                Constants.PREFS_ALERTS,
                Constants.KEY_AOD_FONT_SIZE_DATE,
                14
            ),
            aodDimAmount = prefs.get(
                Constants.PREFS_ALERTS,
                Constants.KEY_AOD_DIM_AMOUNT,
                0
            ),
            aodShowShortcuts = prefs.get(
                Constants.PREFS_ALERTS,
                Constants.KEY_AOD_SHOW_SHORTCUTS,
                false
            )
        )
    }

    fun updatePermissionState(notifications: Boolean, overlay: Boolean) {
        _uiState.value = _uiState.value.copy(
            hasNotificationPermission = notifications,
            hasOverlayPermission = overlay
        )
    }

    fun updatePurchaseState(purchased: Boolean) {
        _uiState.value = _uiState.value.copy(isWidgetsPurchased = purchased)
    }

    fun updateVersionName(name: String) {
        _uiState.value = _uiState.value.copy(versionName = name)
    }

    fun toggleHealthyCharge(enabled: Boolean) {
        updatePref(Constants.KEY_HEALTHY_CHARGE_ENABLED, enabled)
        _uiState.value = _uiState.value.copy(healthyChargeEnabled = enabled)
    }

    fun setHealthyChargeLevel(level: Int) {
        updatePref(Constants.KEY_HEALTHY_CHARGE_LEVEL, level)
        _uiState.value = _uiState.value.copy(healthyChargeLevel = level)
    }

    fun toggleTempAlert(enabled: Boolean) {
        updatePref(Constants.KEY_TEMP_ALERT_ENABLED, enabled)
        _uiState.value = _uiState.value.copy(tempAlertEnabled = enabled)
    }

    fun toggleLowBattery(enabled: Boolean) {
        updatePref(Constants.KEY_LOW_BATTERY_ENABLED, enabled)
        _uiState.value = _uiState.value.copy(lowBatteryEnabled = enabled)
    }

    fun setLowBatteryLevel(level: Int) {
        updatePref(Constants.KEY_LOW_BATTERY_LEVEL, level)
        _uiState.value = _uiState.value.copy(lowBatteryLevel = level)
    }

    fun toggleFastDischarge(enabled: Boolean) {
        updatePref(Constants.KEY_FAST_DISCHARGE_ENABLED, enabled)
        _uiState.value = _uiState.value.copy(fastDischargeEnabled = enabled)
    }

    fun toggleSlowCharge(enabled: Boolean) {
        updatePref(Constants.KEY_SLOW_CHARGE_ENABLED, enabled)
        _uiState.value = _uiState.value.copy(slowChargeEnabled = enabled)
    }

    fun toggleAod(enabled: Boolean) {
        updatePref(Constants.KEY_AOD_ENABLED, enabled)
        _uiState.value = _uiState.value.copy(alwaysOnDisplayEnabled = enabled)
    }

    fun toggleActiveMonitoring(enabled: Boolean) {
        updatePref(Constants.KEY_ACTIVE_MONITORING, enabled)
        _uiState.value = _uiState.value.copy(activeMonitoringEnabled = enabled)
    }

    fun setAodClockStyle(style: Int) {
        updatePref(Constants.KEY_AOD_CLOCK_STYLE, style)
        _uiState.value = _uiState.value.copy(aodClockStyle = style)
    }

    fun setAodMeterStyle(style: Int) {
        updatePref(Constants.KEY_AOD_METER_STYLE, style)
        _uiState.value = _uiState.value.copy(aodMeterStyle = style)
    }

    fun setAodColor(color: Long) {
        updatePref(Constants.KEY_AOD_COLOR, color)
        _uiState.value = _uiState.value.copy(aodColor = color)
    }

    fun toggleAodShowDate(show: Boolean) {
        updatePref(Constants.KEY_AOD_SHOW_DATE, show)
        _uiState.value = _uiState.value.copy(aodShowDate = show)
    }

    fun toggleAodShowClock(show: Boolean) {
        updatePref(Constants.KEY_AOD_SHOW_CLOCK, show)
        _uiState.value = _uiState.value.copy(aodShowClock = show)
    }

    fun toggleAod24h(is24h: Boolean) {
        updatePref(Constants.KEY_AOD_24H_FORMAT, is24h)
        _uiState.value = _uiState.value.copy(aod24hFormat = is24h)
    }

    fun setAodFontSizeClock(size: Int) {
        updatePref(Constants.KEY_AOD_FONT_SIZE_CLOCK, size)
        _uiState.value = _uiState.value.copy(aodFontSizeClock = size)
    }

    fun setAodFontSizeDate(size: Int) {
        updatePref(Constants.KEY_AOD_FONT_SIZE_DATE, size)
        _uiState.value = _uiState.value.copy(aodFontSizeDate = size)
    }

    fun setAodDimAmount(amount: Int) {
        updatePref(Constants.KEY_AOD_DIM_AMOUNT, amount)
        _uiState.value = _uiState.value.copy(aodDimAmount = amount)
    }

    fun toggleAodShowShortcuts(show: Boolean) {
        updatePref(Constants.KEY_AOD_SHOW_SHORTCUTS, show)
        _uiState.value = _uiState.value.copy(aodShowShortcuts = show)
    }

    fun clearAllData() {
        viewModelScope.launch {
            dao.clearAllData()
        }
    }

    private fun updatePref(key: String, value: Any) {
        when (value) {
            is Boolean -> prefs.set(Constants.PREFS_ALERTS, key, value)
            is Int -> prefs.set(Constants.PREFS_ALERTS, key, value)
            is Long -> prefs.set(Constants.PREFS_ALERTS, key, value)
        }
    }
}
