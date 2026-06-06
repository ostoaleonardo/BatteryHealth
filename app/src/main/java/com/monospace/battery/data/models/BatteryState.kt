package com.monospace.battery.data.models

import android.os.BatteryManager
import androidx.compose.runtime.staticCompositionLocalOf
import com.monospace.battery.core.constants.Constants

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
    val capacityRemaining: Int = 0,
    val currentNow: Int = 0,
    val timeRemaining: String = Constants.ZERO_TIME,
    val chargeSpeed: Double = 0.0
)

val LocalBatteryState = staticCompositionLocalOf { BatteryState() }
