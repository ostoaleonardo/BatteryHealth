package com.monospace.battery.core.utils

import android.content.Intent
import android.os.BatteryManager
import android.os.Build

object BatteryMockUtils {
    fun createMockBatteryIntent(): Intent {
        return Intent().apply {
            putExtra(BatteryManager.EXTRA_LEVEL, 85)
            putExtra(BatteryManager.EXTRA_SCALE, 100)
            putExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_CHARGING)
            putExtra(BatteryManager.EXTRA_PLUGGED, BatteryManager.BATTERY_PLUGGED_AC)
            putExtra(BatteryManager.EXTRA_VOLTAGE, 4000)
            putExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_GOOD)
            putExtra(BatteryManager.EXTRA_TEMPERATURE, 350) // 35.0°C
            putExtra(BatteryManager.EXTRA_TECHNOLOGY, "Li-ion")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                putExtra(BatteryManager.EXTRA_CYCLE_COUNT, 128)
            }
        }
    }
}
