package com.monospace.battery.helpers

import android.annotation.SuppressLint
import android.content.Context
import android.os.BatteryManager
import android.os.Build
import java.util.Locale

class BatteryUtils(private val context: Context?) {

    fun getChargeTimeRemaining(isCharging: Boolean): String {
        if (isCharging && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val batteryManager = context?.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

            val millis = batteryManager.computeChargeTimeRemaining()
            val totalMinutes = millis / 1000 / 60
            val hours = totalMinutes / 60
            val minutes = totalMinutes % 60
            return String.format(Locale.getDefault(), "%02d:%02d", hours, minutes)
        }

        return "00:00"
    }

    @SuppressLint("PrivateApi")
    fun getBatteryCapacity(): Int {
        try {
            val powerProfileClass = Class.forName("com.android.internal.os.PowerProfile")
            val constructor = powerProfileClass.getConstructor(Context::class.java)
            val powerProfile = constructor.newInstance(context)

            val method = powerProfileClass.getMethod("getBatteryCapacity")
            val batteryCapacity = method.invoke(powerProfile) as Double
            return batteryCapacity.toInt()
        } catch (e: Exception) {
            return -1
        }
    }
}