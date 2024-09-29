package com.monospace.battery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.monospace.battery.databinding.FragmentFirstBinding
import com.monospace.battery.helpers.BatteryInfo
import com.monospace.battery.helpers.BatteryStrings

class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            displayBatteryInfo(intent)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)

        // Register the battery receiver to get battery status updates
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryIntent = requireActivity().registerReceiver(batteryReceiver, intentFilter)
        displayBatteryInfo(batteryIntent)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().unregisterReceiver(batteryReceiver)
        _binding = null
    }

    private fun displayBatteryInfo(batteryStatusIntent: Intent?) {
        val batteryInfo = BatteryInfo(batteryStatusIntent)

        // Battery health
        displayBatteryHealth(batteryInfo.health)

        // Battery level
        displayBatteryLevel(batteryInfo.level, batteryInfo.isCharging)

        // Battery status
        displayBatteryStatus(batteryInfo.isCharging)

        // Charging source
        displayChargingSource(batteryInfo.chargeSource)

        // Battery cycles
        displayBatteryCycles(batteryInfo.chargeCycles)

        // Battery technology
        displayBatteryTechnology(batteryInfo.technology)

        // Battery temperature
        displayBatteryTemperature(batteryInfo.temperature)

        // Battery voltage
        displayBatteryVoltage(batteryInfo.voltage)

        // Battery capacity
        displayBatteryCapacity(batteryInfo.capacity)
    }

    private fun displayBatteryHealth(health: Int) {
        val batteryString = BatteryStrings()
        val healthString = batteryString.getHealthStatus(health)

        val healthImage = when (health) {
            BatteryManager.BATTERY_HEALTH_GOOD -> R.drawable.battery_status_good
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> R.drawable.battery_alert
            BatteryManager.BATTERY_HEALTH_DEAD -> R.drawable.battery_error
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> R.drawable.battery_alert
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> R.drawable.battery_error
            else -> R.drawable.battery_unknown
        }

        val healthTextView = binding.health
        val healthImageView = binding.healthImage

        healthTextView.text = getString(healthString)
        healthImageView.setImageResource(healthImage)
    }

    private fun displayBatteryLevel(
        level: Int,
        isCharging: Boolean
    ) {
        val batteryTextView = binding.battery
        val batteryImageView = binding.batteryImage

        batteryTextView.text = getString(R.string.battery_percentage, level)

        val batteryImage = if (isCharging) {
            when {
                level >= 100 -> R.drawable.battery_full
                level >= 90 -> R.drawable.battery_charging_90
                level >= 80 -> R.drawable.battery_charging_80
                level >= 60 -> R.drawable.battery_charging_60
                level >= 50 -> R.drawable.battery_charging_50
                level >= 30 -> R.drawable.battery_charging_30
                level >= 10 -> R.drawable.battery_charging_20
                else -> R.drawable.battery_charging_0
            }
        } else {
            when {
                level >= 100 -> R.drawable.battery_full
                level >= 90 -> R.drawable.battery_6
                level >= 70 -> R.drawable.battery_5
                level >= 60 -> R.drawable.battery_4
                level >= 40 -> R.drawable.battery_3
                level >= 20 -> R.drawable.battery_2
                level >= 10 -> R.drawable.battery_1
                else -> R.drawable.battery_0
            }
        }

        batteryImageView.setImageResource(batteryImage)
    }

    private fun displayBatteryStatus(isCharging: Boolean) {
        val batteryString = BatteryStrings()
        val statusString = batteryString.getChargingStatus(isCharging)

        val statusTextView = binding.status
        statusTextView.text = getString(statusString)
    }

    private fun displayChargingSource(chargingSource: Int) {
        val batteryString = BatteryStrings()
        val chargingSourceString = batteryString.getChargingSource(chargingSource)

        val chargingSourceTextView = binding.chargingSource
        chargingSourceTextView.text = getString(chargingSourceString)
    }

    private fun displayBatteryCycles(cycles: Int) {
        val cyclesCardView = binding.cyclesCard

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            cyclesCardView.visibility = View.GONE
            return
        }

        val cyclesTextView = binding.cycles
        cyclesTextView.text = "$cycles"
    }

    private fun displayBatteryTechnology(batteryTechnology: String?) {
        val batteryTypeTextView = binding.batteryType
        batteryTypeTextView.text = batteryTechnology ?: getString(R.string.battery_health_unknown)
    }

    private fun displayBatteryTemperature(temperature: Int) {
        val temperatureTextView = binding.temperature
        temperatureTextView.text = getString(R.string.temperature_celsius, temperature / 10)
    }

    private fun displayBatteryVoltage(voltage: Int) {
        val temperatureTextView = binding.voltage
        temperatureTextView.text = getString(R.string.voltage_mv, voltage)
    }

    private fun displayBatteryCapacity(capacity: Int) {
        val capacityCardView = binding.capacityCard
        val capacityTextView = binding.capacity

        if (capacity == -1) {
            capacityCardView.visibility = View.GONE
            return
        }

        capacityTextView.text = getString(R.string.capacity_mah, capacity)
    }
}