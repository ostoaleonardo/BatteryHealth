package com.monospace.battery.core.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.BatteryManager
import android.os.Build
import android.util.Log
import java.io.File
import java.util.Locale
import kotlin.math.abs
import kotlin.math.round

class BatteryUtils(context: Context) {

    private val appContext = context.applicationContext

    private val batteryManager by lazy {
        appContext.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
    }

    fun getChargeTimeRemaining(isCharging: Boolean): String {
        if (!isCharging || Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return TIME_ZERO

        val manager = batteryManager ?: return TIME_ZERO

        val millis = runCatching {
            manager.computeChargeTimeRemaining()
        }.getOrDefault(-1L)

        if (millis <= 0) return TIME_ZERO

        val hours = millis / 3_600_000
        val minutes = (millis / 60_000) % 60

        return runCatching {
            String.format(Locale.US, TIME_FORMAT, hours, minutes)
        }.getOrDefault(TIME_ZERO)
    }

    @SuppressLint("PrivateApi")
    fun getBatteryCapacity(): Int {
        // 1. PowerProfile
        val profileCapacity = runCatching {
            val powerProfileClass = Class.forName(POWER_PROFILE_CLASS)

            val powerProfile = powerProfileClass
                .getConstructor(Context::class.java)
                .newInstance(appContext)

            val capacityObject = powerProfileClass
                .getMethod(GET_BATTERY_CAPACITY_METHOD)
                .invoke(powerProfile)

            when (capacityObject) {
                is Double -> capacityObject.toInt()
                is Float -> capacityObject.toInt()
                is Long -> capacityObject.toInt()
                is Int -> capacityObject
                else -> capacityObject?.toString()?.toDoubleOrNull()?.toInt() ?: -1
            }
        }.onFailure {
            Log.w(TAG, "PowerProfile capacity lookup failed: ${it.message}")
        }.getOrDefault(-1)

        if (profileCapacity > 0) return profileCapacity

        // 2. System files
        val filePaths = listOf(
            "/sys/class/power_supply/battery/charge_full",
            "/sys/class/power_supply/battery/charge_full_design",
            "/sys/class/power_supply/max170xx_battery/charge_full_design"
        )

        for (path in filePaths) {
            val capacity = runCatching {
                val file = File(path)
                if (!file.exists()) return@runCatching -1

                val text = file.readText().trim()
                val value = text.toDoubleOrNull()?.toInt() ?: -1

                // Some files store microamperes (μAh), we convert to mAh
                if (value > 10000) value / 1000 else value
            }.getOrDefault(-1)

            if (capacity > 0) return capacity
        }

        return -1
    }

    fun getChargeSpeed(
        voltageMilliVolts: Int,
        isCharging: Boolean
    ): Double {
        if (!isCharging || voltageMilliVolts <= 0) return 0.0

        val manager = batteryManager ?: return 0.0

        // Get current in microamperes (μA)
        val currentRaw = runCatching {
            manager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        }.getOrDefault(0L)

        if (currentRaw == Long.MIN_VALUE || currentRaw == 0L) return 0.0

        val absoluteCurrent = abs(currentRaw)

        // Scale heuristic: Handle mA vs μA
        val currentInAmps = if (absoluteCurrent < 20000) {
            absoluteCurrent / 1000.0
        } else {
            absoluteCurrent / 1_000_000.0
        }

        val voltageVolts = voltageMilliVolts / 1_000.0
        val chargeWatts = currentInAmps * voltageVolts

        // Sanity check
        if (chargeWatts > 250) return 0.0

        // Mathematical rounding to 1 decimal place
        return round(chargeWatts * 10) / 10.0
    }

    fun getCurrentNow(): Int {
        val manager = batteryManager ?: return 0
        val currentNow = runCatching {
            manager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        }.onFailure {
            Log.w(TAG, "Error reading current: ${it.message}")
        }.getOrDefault(0L)

        if (currentNow == Long.MIN_VALUE) return 0

        // Handle scaling (mA vs μA)
        return if (abs(currentNow) > 100_000) {
            (currentNow / 1000).toInt()
        } else {
            currentNow.toInt()
        }
    }

    fun getCapacityRemaining(): Int {
        val manager = batteryManager ?: return -1
        val remaining = runCatching {
            manager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
        }.onFailure {
            Log.w(TAG, "Error reading charge counter: ${it.message}")
        }.getOrDefault(-1L)

        if (remaining <= 0) return -1

        // Handle scaling (mAh vs μAh)
        return if (remaining > 100_000) {
            (remaining / 1000).toInt()
        } else {
            remaining.toInt()
        }
    }

    companion object {
        private const val TAG = "BatteryUtils"

        // Time formats
        private const val TIME_ZERO = "00:00"
        private const val TIME_FORMAT = "%02d:%02d"

        // Reflection constants
        private const val POWER_PROFILE_CLASS = "com.android.internal.os.PowerProfile"
        private const val GET_BATTERY_CAPACITY_METHOD = "getBatteryCapacity"
    }
}
