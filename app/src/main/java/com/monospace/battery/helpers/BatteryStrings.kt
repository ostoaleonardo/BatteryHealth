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

    fun getCapacityValue(capacity: Int, capacityRemaining: Int): String? {
        return if (capacityRemaining > 0 && capacity > 0) {
            "$capacityRemaining / $capacity mAh"
        } else if (capacity > 0) {
            "$capacity mAh"
        } else null
    }

    fun getChargerQuality(chargeSpeed: Double): Int {
        return when {
            chargeSpeed <= 0 -> R.string.charging_source_none
            chargeSpeed < 10 -> R.string.charger_quality_slow
            chargeSpeed < 25 -> R.string.charger_quality_normal
            else -> R.string.charger_quality_fast
        }
    }
}