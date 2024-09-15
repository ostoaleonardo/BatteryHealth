package com.monospace.battery

import android.annotation.SuppressLint
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
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.monospace.battery.databinding.FragmentFirstBinding

class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!

    private val batteryReceiver = object : BroadcastReceiver() {
        @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
        override fun onReceive(context: Context?, intent: Intent?) {
            displayBatteryInfo(intent)
        }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)

        // Register the battery receiver to get battery status updates
        val batteryStatusIntentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatusIntent =
            requireActivity().registerReceiver(batteryReceiver, batteryStatusIntentFilter)
        displayBatteryInfo(batteryStatusIntent)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().unregisterReceiver(batteryReceiver)
        _binding = null
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun displayBatteryInfo(batteryStatusIntent: Intent?) {
        batteryStatusIntent?.let {
            // Battery health
            val health = it.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)
            displayBatteryHealth(health)

            // Battery level
            val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val batteryPercentage = level * 100 / scale

            // Battery status
            val status = it.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING
            displayBatteryStatus(isCharging)
            displayBatteryLevel(batteryPercentage, isCharging)

            // Charging source
            val chargePlug = it.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
            val usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB
            val acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC
            val wsCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_WIRELESS
            val dockCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_DOCK
            displayChargingSource(usbCharge, acCharge, wsCharge, dockCharge)

            // Battery cycles
            val cycles = it.getIntExtra(BatteryManager.EXTRA_CYCLE_COUNT, -1)
            displayBatteryCycles(cycles)

            // Battery technology
            val technology = it.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY)
            displayBatteryTechnology(technology)

            // Battery temperature
            val temperature = it.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)
            displayBatteryTemperature(temperature)

            // Battery voltage
            val voltage = it.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
            displayBatteryVoltage(voltage)

            // Battery capacity
            val capacity = it.getIntExtra(BatteryManager.BATTERY_PROPERTY_CAPACITY.toString(), -1)
            displayBatteryCapacity(capacity)
        }
    }

    private fun displayBatteryHealth(health: Int) {
        val healthString = when (health) {
            BatteryManager.BATTERY_HEALTH_GOOD -> R.string.battery_health_good
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> R.string.battery_health_overheat
            BatteryManager.BATTERY_HEALTH_DEAD -> R.string.battery_health_dead
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> R.string.battery_health_over_voltage
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> R.string.battery_health_unspecified_failure
            else -> R.string.battery_health_unknown
        }

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

    @SuppressLint("SetTextI18n")
    private fun displayBatteryLevel(
        batteryPercentage: Int,
        isCharging: Boolean
    ) {
        val batteryTextView = binding.battery
        val batteryImageView = binding.batteryImage

        batteryTextView.text = "$batteryPercentage%"

        val batteryImage = if (isCharging) {
            when {
                batteryPercentage >= 100 -> R.drawable.battery_full
                batteryPercentage >= 90 -> R.drawable.battery_charging_80
                batteryPercentage >= 80 -> R.drawable.battery_charging_80
                batteryPercentage >= 60 -> R.drawable.battery_charging_60
                batteryPercentage >= 50 -> R.drawable.battery_charging_50
                batteryPercentage >= 30 -> R.drawable.battery_charging_30
                batteryPercentage >= 10 -> R.drawable.battery_charging_20
                else -> R.drawable.battery_charging_0
            }
        } else {
            when {
                batteryPercentage >= 100 -> R.drawable.battery_full
                batteryPercentage >= 90 -> R.drawable.battery_6
                batteryPercentage >= 70 -> R.drawable.battery_5
                batteryPercentage >= 60 -> R.drawable.battery_4
                batteryPercentage >= 40 -> R.drawable.battery_3
                batteryPercentage >= 20 -> R.drawable.battery_2
                batteryPercentage >= 10 -> R.drawable.battery_1
                else -> R.drawable.battery_0
            }
        }

        batteryImageView.setImageResource(batteryImage)
    }

    private fun displayBatteryStatus(isCharging: Boolean) {
        val statusTextView = binding.status

        statusTextView.text =
            if (isCharging) getString(R.string.battery_charging)
            else getString(R.string.battery_unplugged)
    }

    private fun displayChargingSource(
        usbCharge: Boolean,
        acCharge: Boolean,
        wsCharge: Boolean,
        dockCharge: Boolean
    ) {
        val chargingSourceTextView = binding.chargingSource

        chargingSourceTextView.text = when {
            usbCharge -> getString(R.string.charging_source_usb)
            acCharge -> getString(R.string.charging_source_ac)
            wsCharge -> getString(R.string.charging_source_wireless)
            dockCharge -> getString(R.string.charging_source_dock)
            else -> getString(R.string.charging_source_none)
        }
    }

    private fun displayBatteryCycles(cycles: Int) {
        val cyclesTextView = binding.cycles
        cyclesTextView.text =
            if (cycles == -1) getString(R.string.battery_health_unknown)
            else cycles.toString()
    }

    private fun displayBatteryTechnology(batteryTechnology: String?) {
        val batteryTypeTextView = binding.batteryType
        batteryTypeTextView.text = batteryTechnology ?: getString(R.string.battery_health_unknown)
    }

    @SuppressLint("SetTextI18n")
    private fun displayBatteryTemperature(temperature: Int) {
        val temperatureTextView = binding.temperature
        temperatureTextView.text = "${temperature / 10}°C"
    }

    @SuppressLint("SetTextI18n")
    private fun displayBatteryVoltage(voltage: Int) {
        val temperatureTextView = binding.voltage
        temperatureTextView.text = "$voltage mV"
    }

    @SuppressLint("SetTextI18n")
    private fun displayBatteryCapacity(capacity: Int) {
        val capacityCardView = binding.capacityCard
        val capacityTextView = binding.capacity

        capacityCardView.visibility =
            if (capacity == -1) View.GONE
            else View.VISIBLE

        capacityTextView.text = "$capacity mAh"
    }
}