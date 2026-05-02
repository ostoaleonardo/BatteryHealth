package com.monospace.battery.data.models

import android.content.Intent
import android.os.BatteryManager
import android.os.Build

class BatteryInfo(intent: Intent?) {

    private var _health = BatteryManager.BATTERY_HEALTH_UNKNOWN
    private var _level = 0
    private var _isCharging = false
    private var _chargeSource = 0
    private var _chargeCycles = 0
    private var _technology = ""
    private var _temperature = 0
    private var _voltage = 0

    val health: Int get() = _health
    val level: Int get() = _level
    val isCharging: Boolean get() = _isCharging
    val chargeSource: Int get() = _chargeSource
    val chargeCycles: Int get() = _chargeCycles
    val technology: String get() = _technology
    val temperature: Int get() = _temperature
    val voltage: Int get() = _voltage

    init {
        intent?.let {
            _health = it.getIntExtra(
                BatteryManager.EXTRA_HEALTH,
                BatteryManager.BATTERY_HEALTH_UNKNOWN
            )

            val rawLevel = it.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
            val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
            _level = if (scale > 0) (rawLevel * 100 / scale) else 0

            val status = it.getIntExtra(
                BatteryManager.EXTRA_STATUS,
                BatteryManager.BATTERY_STATUS_UNKNOWN
            )
            _isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL

            _chargeSource = it.getIntExtra(
                BatteryManager.EXTRA_PLUGGED,
                0
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                _chargeCycles = it.getIntExtra(BatteryManager.EXTRA_CYCLE_COUNT, 0)
            }

            _technology = it.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: ""
            _temperature = it.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
            _voltage = it.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
        }
    }
}
