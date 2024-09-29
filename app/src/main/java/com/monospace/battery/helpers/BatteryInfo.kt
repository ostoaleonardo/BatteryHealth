package com.monospace.battery.helpers

import android.content.Intent
import android.os.BatteryManager
import android.os.Build

class BatteryInfo(intent: Intent?) : Intent(intent) {

    private var _health = 0
    private var _level = 0
    private var _isCharging = false
    private var _chargeSource = 0
    private var _chargeCycles = 0
    private var _technology = ""
    private var _temperature = 0
    private var _voltage = 0
    private var _capacity = 0

    val health: Int
        get() = _health

    val level: Int
        get() = _level

    val isCharging: Boolean
        get() = _isCharging

    val chargeSource: Int
        get() = _chargeSource

    val chargeCycles: Int
        get() = _chargeCycles

    val technology: String
        get() = _technology

    val temperature: Int
        get() = _temperature

    val voltage: Int
        get() = _voltage

    val capacity: Int
        get() = _capacity

    init {
        _health = getIntExtra(
            BatteryManager.EXTRA_HEALTH,
            BatteryManager.BATTERY_HEALTH_UNKNOWN
        )

        _level = let {
            val level = getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
            val scale = getIntExtra(BatteryManager.EXTRA_SCALE, 100)
            level * 100 / scale
        }

        _isCharging = getIntExtra(
            BatteryManager.EXTRA_STATUS,
            BatteryManager.BATTERY_STATUS_UNKNOWN
        ) == BatteryManager.BATTERY_STATUS_CHARGING

        _chargeSource = getIntExtra(
            BatteryManager.EXTRA_PLUGGED,
            BatteryManager.BATTERY_PLUGGED_AC
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            _chargeCycles = getIntExtra(BatteryManager.EXTRA_CYCLE_COUNT, -1)
        }

        _technology = getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: ""

        _temperature = getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)

        _voltage = getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)

        _capacity = getIntExtra(BatteryManager.BATTERY_PROPERTY_CAPACITY.toString(), -1)
    }
}