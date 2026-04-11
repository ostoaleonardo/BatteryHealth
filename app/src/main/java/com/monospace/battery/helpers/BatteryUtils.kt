package com.monospace.battery.helpers

import android.annotation.SuppressLint
import android.content.Context
import android.os.BatteryManager
import android.os.Build
import java.io.File
import java.util.Locale
import kotlin.math.abs
import kotlin.math.round

class BatteryUtils(context: Context) {

    private val appContext = context.applicationContext

    fun getChargeTimeRemaining(isCharging: Boolean): String {
        if (!isCharging || Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return "00:00"

        val batteryManager = appContext.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

        val millis = runCatching {
            batteryManager.computeChargeTimeRemaining()
        }.getOrDefault(-1L)

        if (millis <= 0) return "00:00"

        val hours = millis / 3_600_000
        val minutes = (millis / 60_000) % 60

        return String.format(Locale.US, "%02d:%02d", hours, minutes)
    }

    @SuppressLint("PrivateApi")
    fun getBatteryCapacity(): Int {
        // 1. PowerProfile
        val profileCapacity = runCatching {
            val powerProfileClass = Class.forName("com.android.internal.os.PowerProfile")
            val powerProfile = powerProfileClass.getConstructor(Context::class.java)
                .newInstance(appContext)
            val capacity = powerProfileClass.getMethod("getBatteryCapacity")
                .invoke(powerProfile) as Double

            capacity.toInt()
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

                val value = file.readText().trim().toInt()
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

        val batteryManager = appContext.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

        // Get current in microamperes (μA)
        val currentMicroAmps = batteryManager.getLongProperty(
            BatteryManager.BATTERY_PROPERTY_CURRENT_NOW
        )

        if (currentMicroAmps == Long.MIN_VALUE) return 0.0

        // Calculate Watts (W = A * V)
        val currentAmps = abs(currentMicroAmps) / 1_000_000.0
        val voltageVolts = voltageMilliVolts / 1_000.0
        val chargeWatts = currentAmps * voltageVolts

        // Mathematical rounding to 1 decimal place
        return round(chargeWatts * 10) / 10.0
    }
}
