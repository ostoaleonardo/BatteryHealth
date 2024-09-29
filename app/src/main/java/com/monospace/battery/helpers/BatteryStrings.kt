package com.monospace.battery.helpers

import android.os.BatteryManager
import com.monospace.battery.R

class BatteryStrings {

    fun getHealthStatus(health: Int): Int {
        return when (health) {
            BatteryManager.BATTERY_HEALTH_GOOD -> R.string.battery_health_good
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> R.string.battery_health_overheat
            BatteryManager.BATTERY_HEALTH_DEAD -> R.string.battery_health_dead
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> R.string.battery_health_over_voltage
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> R.string.battery_health_unspecified_failure
            else -> R.string.battery_health_unknown
        }
    }

    fun getChargingStatus(isCharging: Boolean): Int {
        return if (isCharging) R.string.battery_charging
        else R.string.battery_unplugged
    }

    fun getChargingSource(source: Int): Int {
        return when (source) {
            BatteryManager.BATTERY_PLUGGED_AC -> R.string.charging_source_ac
            BatteryManager.BATTERY_PLUGGED_USB -> R.string.charging_source_usb
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> R.string.charging_source_wireless
            BatteryManager.BATTERY_PLUGGED_DOCK -> R.string.charging_source_dock
            else -> R.string.charging_source_none
        }
    }
}