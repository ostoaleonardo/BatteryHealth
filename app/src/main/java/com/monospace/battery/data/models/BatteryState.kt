package com.monospace.battery.data.models

import android.os.BatteryManager

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
    val timeRemaining: String = "00:00",
    val chargeSpeed: Double = 0.0
)
