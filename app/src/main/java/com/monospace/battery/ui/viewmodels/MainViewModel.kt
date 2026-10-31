package com.monospace.battery.ui.viewmodels

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.monospace.battery.core.utils.BatteryUtils
import com.monospace.battery.data.models.BatteryInfo
import com.monospace.battery.data.models.BatteryState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val batteryUtils = BatteryUtils(application)
    private val _batteryState = mutableStateOf(BatteryState())
    val batteryState: State<BatteryState> = _batteryState

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            updateBatteryState(intent)
        }
    }

    init {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val stickyIntent = application.registerReceiver(batteryReceiver, filter)
        updateBatteryState(stickyIntent)
    }

    private var sensorJob: Job? = null

    private fun updateBatteryState(intent: Intent?) {
        val info = BatteryInfo(intent)

        _batteryState.value = _batteryState.value.copy(
            health = info.health,
            level = info.level,
            isCharging = info.isCharging,
            chargeSource = info.chargeSource,
            chargeCycles = info.chargeCycles,
            technology = info.technology,
            temperature = info.temperature,
            voltage = info.voltage
        )

        sensorJob?.cancel()
        sensorJob = viewModelScope.launch(Dispatchers.IO) {
            val capacity = batteryUtils.getBatteryCapacity()
            val capacityRemaining = batteryUtils.getCapacityRemaining()
            val currentNow = batteryUtils.getCurrentNow()
            val timeRemaining = batteryUtils.getChargeTimeRemaining(info.isCharging)
            val chargeSpeed = batteryUtils.getChargeSpeed(info.voltage, info.isCharging)

            withContext(Dispatchers.Main) {
                _batteryState.value = _batteryState.value.copy(
                    capacity = capacity,
                    capacityRemaining = capacityRemaining,
                    currentNow = currentNow,
                    timeRemaining = timeRemaining,
                    chargeSpeed = chargeSpeed
                )
            }
        }
    }

    override fun onCleared() {
        runCatching {
            getApplication<Application>().unregisterReceiver(batteryReceiver)
        }
    }
}
