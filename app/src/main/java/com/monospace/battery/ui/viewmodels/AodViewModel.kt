package com.monospace.battery.ui.viewmodels

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.monospace.battery.core.utils.BatteryUtils
import com.monospace.battery.data.models.BatteryInfo
import com.monospace.battery.data.models.BatteryState

class AodViewModel(application: Application) : AndroidViewModel(application) {
    private val batteryUtils = BatteryUtils(application)

    private val _batteryState = mutableStateOf(BatteryState())
    val batteryState: State<BatteryState> = _batteryState

    private val _shouldFinish = mutableStateOf(false)
    val shouldFinish: State<Boolean> = _shouldFinish

    var isTestMode = false

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val info = BatteryInfo(intent)

            _batteryState.value = _batteryState.value.copy(
                level = info.level,
                isCharging = info.isCharging,
                voltage = info.voltage,
                chargeSpeed = batteryUtils.getChargeSpeed(info.voltage, info.isCharging),
                timeRemaining = batteryUtils.getChargeTimeRemaining(info.isCharging)
            )

            if (!isTestMode && !info.isCharging && intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                _shouldFinish.value = true
            }
        }
    }

    init {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            application.registerReceiver(batteryReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            application.registerReceiver(batteryReceiver, filter)
        }

        // Initial state
        val stickyIntent = application.registerReceiver(null, filter)
        val info = BatteryInfo(stickyIntent)

        _batteryState.value = _batteryState.value.copy(
            level = info.level,
            isCharging = info.isCharging,
            voltage = info.voltage,
            chargeSpeed = batteryUtils.getChargeSpeed(info.voltage, info.isCharging),
            timeRemaining = batteryUtils.getChargeTimeRemaining(info.isCharging)
        )
    }

    override fun onCleared() {
        runCatching {
            getApplication<Application>().unregisterReceiver(batteryReceiver)
        }
    }
}
