package com.monospace.battery.ui.components

import android.os.BatteryManager
import androidx.compose.ui.graphics.Color
import com.monospace.battery.R

data class BatteryState(
    val health: Int = BatteryManager.BATTERY_HEALTH_UNKNOWN,
    val level: Int = 0,
    val isCharging: Boolean = false,
    val chargeSource: Int = 0,
    val chargeCycles: Int = 0,
    val technology: String? = null,
    val temperature: Int = 0,
    val voltage: Int = 0,
    val capacity: Int = 0,
    val timeRemaining: String = "00:00",
    val chargeSpeed: Double = 0.0
)

fun getHealthStatusRes(health: Int) = when (health) {
    BatteryManager.BATTERY_HEALTH_GOOD -> R.string.battery_health_good
    BatteryManager.BATTERY_HEALTH_OVERHEAT -> R.string.battery_health_overheat
    BatteryManager.BATTERY_HEALTH_DEAD -> R.string.battery_health_dead
    BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> R.string.battery_health_over_voltage
    BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> R.string.battery_health_unspecified_failure
    else -> R.string.battery_health_unknown
}

fun getChargingStatusRes(isCharging: Boolean) = if (isCharging) R.string.battery_charging else R.string.battery_unplugged

fun getChargingSourceRes(source: Int) = when (source) {
    BatteryManager.BATTERY_PLUGGED_AC -> R.string.charging_source_ac
    BatteryManager.BATTERY_PLUGGED_USB -> R.string.charging_source_usb
    BatteryManager.BATTERY_PLUGGED_WIRELESS -> R.string.charging_source_wireless
    BatteryManager.BATTERY_PLUGGED_DOCK -> R.string.charging_source_dock
    else -> R.string.charging_source_none
}

fun getHealthIcon(health: Int) = when (health) {
    BatteryManager.BATTERY_HEALTH_GOOD -> R.drawable.battery_status_good
    BatteryManager.BATTERY_HEALTH_OVERHEAT -> R.drawable.battery_alert
    BatteryManager.BATTERY_HEALTH_DEAD -> R.drawable.battery_error
    BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> R.drawable.battery_alert
    BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> R.drawable.battery_error
    else -> R.drawable.battery_unknown
}

fun getBatteryIcon(level: Int, isCharging: Boolean) = if (isCharging) {
    when {
        level >= 100 -> R.drawable.battery_full
        level >= 90 -> R.drawable.battery_charging_90
        level >= 80 -> R.drawable.battery_charging_80
        level >= 60 -> R.drawable.battery_charging_60
        level >= 50 -> R.drawable.battery_charging_50
        level >= 30 -> R.drawable.battery_charging_30
        level >= 10 -> R.drawable.battery_charging_20
        else -> R.drawable.battery_charging_0
    }
} else {
    when {
        level >= 100 -> R.drawable.battery_full
        level >= 90 -> R.drawable.battery_6
        level >= 70 -> R.drawable.battery_5
        level >= 60 -> R.drawable.battery_4
        level >= 40 -> R.drawable.battery_3
        level >= 20 -> R.drawable.battery_2
        level >= 10 -> R.drawable.battery_1
        else -> R.drawable.battery_0
    }
}

fun getHealthColor(health: Int): Color = when (health) {
    BatteryManager.BATTERY_HEALTH_GOOD -> ColorGreen
    BatteryManager.BATTERY_HEALTH_OVERHEAT -> ColorOrange
    BatteryManager.BATTERY_HEALTH_DEAD -> ColorRed
    BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> ColorDeepOrange
    else -> ColorGray
}

fun getBatteryLevelColor(level: Int): Color = when {
    level >= 80 -> ColorGreen
    level >= 50 -> ColorOrange
    level >= 20 -> ColorDeepOrange
    else -> ColorRed
}

fun getHealthBgColor(health: Int): Color = getHealthColor(health).copy(alpha = 0.1f)

private val ColorGreen = Color(0xFF00A25B)
private val ColorOrange = Color(0xFFFF9800)
private val ColorDeepOrange = Color(0xFFFF5722)
private val ColorRed = Color(0xFFF44336)
private val ColorGray = Color(0xFF9E9E9E)
