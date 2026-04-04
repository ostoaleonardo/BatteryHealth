package com.monospace.battery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.monospace.battery.helpers.BatteryInfo
import com.monospace.battery.helpers.BatteryUtils
import com.monospace.battery.ui.components.BatteryState
import com.monospace.battery.ui.screens.MainScreen
import com.monospace.battery.ui.theme.BatteryTheme

class FirstFragment : Fragment() {

    private var batteryState by mutableStateOf(BatteryState())

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            updateBatteryState(intent)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                BatteryTheme {
                    MainScreen(state = batteryState)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryIntent = requireActivity().registerReceiver(batteryReceiver, intentFilter)
        updateBatteryState(batteryIntent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().unregisterReceiver(batteryReceiver)
    }

    private fun updateBatteryState(intent: Intent?) {
        val batteryInfo = BatteryInfo(intent)
        val batteryUtils = BatteryUtils(requireContext())
        
        batteryState = BatteryState(
            health = batteryInfo.health,
            level = batteryInfo.level,
            isCharging = batteryInfo.isCharging,
            chargeSource = batteryInfo.chargeSource,
            chargeCycles = batteryInfo.chargeCycles,
            technology = batteryInfo.technology,
            temperature = batteryInfo.temperature,
            voltage = batteryInfo.voltage,
            capacity = batteryUtils.getBatteryCapacity(),
            timeRemaining = batteryUtils.getChargeTimeRemaining(batteryInfo.isCharging),
            chargeSpeed = batteryUtils.getChargeSpeed(intent!!, batteryInfo.isCharging)
        )
    }
}
