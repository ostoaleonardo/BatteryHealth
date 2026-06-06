package com.monospace.battery.ui.components

import android.os.BatteryManager
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.monospace.battery.R
import com.monospace.battery.core.constants.Constants

fun getHealthStatusRes(health: Int) = when (health) {
    BatteryManager.BATTERY_HEALTH_GOOD -> R.string.battery_health_good
    BatteryManager.BATTERY_HEALTH_OVERHEAT -> R.string.battery_health_overheat
    BatteryManager.BATTERY_HEALTH_DEAD -> R.string.battery_health_dead
    BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> R.string.battery_health_over_voltage
    BatteryManager.BATTERY_HEALTH_COLD -> R.string.battery_health_description_cold
    BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> R.string.battery_health_unspecified_failure
    else -> R.string.battery_health_unknown
}

fun getHealthDescriptionRes(health: Int) = when (health) {
    BatteryManager.BATTERY_HEALTH_GOOD -> R.string.battery_health_description_good
    BatteryManager.BATTERY_HEALTH_OVERHEAT -> R.string.battery_health_description_overheat
    BatteryManager.BATTERY_HEALTH_DEAD -> R.string.battery_health_description_dead
    BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> R.string.battery_health_description_over_voltage
    BatteryManager.BATTERY_HEALTH_COLD -> R.string.battery_health_description_cold
    else -> R.string.battery_health_description_unknown
}

fun getChargingStatusRes(isCharging: Boolean) =
    if (isCharging) R.string.battery_charging else R.string.battery_unplugged

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

@Composable
fun getHealthColor(health: Int): Color = when (health) {
    BatteryManager.BATTERY_HEALTH_GOOD -> MaterialTheme.colorScheme.primary
    BatteryManager.BATTERY_HEALTH_OVERHEAT,
    BatteryManager.BATTERY_HEALTH_DEAD,
    BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> MaterialTheme.colorScheme.error

    else -> MaterialTheme.colorScheme.onSurfaceVariant
}

fun getStaticHealthColor(health: Int): Color = when (health) {
    BatteryManager.BATTERY_HEALTH_GOOD -> Constants.ColorGreen
    BatteryManager.BATTERY_HEALTH_OVERHEAT -> Constants.ColorOrange
    BatteryManager.BATTERY_HEALTH_DEAD -> Constants.ColorRed
    BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> Constants.ColorDeepOrange
    else -> Constants.ColorGray
}

fun getBatteryLevelColor(level: Int): Color = when {
    level >= 90 -> Constants.ColorGreen
    level >= 60 -> Constants.ColorLightGreen
    level >= 35 -> Constants.ColorOrange
    level >= 20 -> Constants.ColorDeepOrange
    else -> Constants.ColorRed
}

@Composable
fun getHealthBgColor(health: Int): Color = when (health) {
    BatteryManager.BATTERY_HEALTH_GOOD -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
    BatteryManager.BATTERY_HEALTH_OVERHEAT,
    BatteryManager.BATTERY_HEALTH_DEAD,
    BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> MaterialTheme.colorScheme.errorContainer.copy(
        alpha = 0.4f
    )

    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
}

@Composable
fun formatResourceOrDash(value: Int, resId: Int, vararg args: Any): String {
    return if (value > 0) {
        stringResource(resId, *args)
    } else {
        Constants.EMPTY_VALUE_DASH
    }
}
