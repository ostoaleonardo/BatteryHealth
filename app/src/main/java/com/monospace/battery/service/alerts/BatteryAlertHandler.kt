package com.monospace.battery.service.alerts

import android.content.Context
import android.os.BatteryManager
import com.monospace.battery.core.constants.Constants
import com.monospace.battery.core.utils.BatteryUtils
import com.monospace.battery.data.local.PreferenceManager
import com.monospace.battery.data.local.WidgetsUtils
import com.monospace.battery.data.local.db.BatteryDatabase
import com.monospace.battery.notifications.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class BatteryAlertHandler(
    private val context: Context,
    private val scope: CoroutineScope,
    private val prefs: PreferenceManager,
    private val notificationHelper: NotificationHelper,
    private val batteryUtils: BatteryUtils,
    private val db: BatteryDatabase
) {
    private var lastLevel = -1
    private var lastTemp = -1

    fun checkAlerts(
        level: Int,
        temperature: Int,
        isCharging: Boolean,
        voltage: Int,
        lastStatus: Int
    ) {
        if (level == -1 || temperature == -1) return
        if (!WidgetsUtils.isWidgetsPurchased(context)) return

        checkHealthyChargeAlert(level, isCharging)
        checkLowBatteryAlert(level, isCharging)
        checkHighTemperatureAlert(temperature)
        checkSlowChargeAlert(isCharging, voltage, lastStatus)
        checkFastDischargeAlert(level, isCharging)
        
        lastLevel = level
    }

    private fun checkHealthyChargeAlert(level: Int, isCharging: Boolean) {
        val enabled = prefs.getAlert(Constants.KEY_HEALTHY_CHARGE_ENABLED, false)
        val threshold = prefs.getAlert(Constants.KEY_HEALTHY_CHARGE_LEVEL, 80)

        if (enabled && isCharging && level >= threshold && lastLevel != -1 && lastLevel < threshold) {
            notificationHelper.showHealthyChargeNotification()
        }
    }

    private fun checkLowBatteryAlert(level: Int, isCharging: Boolean) {
        val enabled = prefs.getAlert(Constants.KEY_LOW_BATTERY_ENABLED, false)
        val threshold = prefs.getAlert(Constants.KEY_LOW_BATTERY_LEVEL, 20)

        if (enabled && !isCharging && level <= threshold && lastLevel != -1 && lastLevel > threshold) {
            notificationHelper.showLowBatteryNotification(threshold)
        }
    }

    private fun checkHighTemperatureAlert(temperature: Int) {
        val enabled = prefs.getAlert(Constants.KEY_TEMP_ALERT_ENABLED, false)
        val tempCelsius = temperature / 10

        if (enabled && tempCelsius >= 40 && lastTemp != -1 && lastTemp < 40) {
            notificationHelper.showTempAlertNotification()
        }

        lastTemp = tempCelsius
    }

    private fun checkSlowChargeAlert(isCharging: Boolean, voltage: Int, lastStatus: Int) {
        val enabled = prefs.getAlert(Constants.KEY_SLOW_CHARGE_ENABLED, false)

        if (enabled && isCharging && lastStatus != BatteryManager.BATTERY_STATUS_CHARGING) {
            scope.launch {
                val speed = batteryUtils.getChargeSpeed(voltage, true)
                if (speed > 0 && speed < 2.0) {
                    notificationHelper.showSlowChargeNotification()
                }
            }
        }
    }

    private fun checkFastDischargeAlert(level: Int, isCharging: Boolean) {
        val enabled = prefs.getAlert(Constants.KEY_FAST_DISCHARGE_ENABLED, false)

        if (enabled && !isCharging && lastLevel != -1 && level < lastLevel) {
            scope.launch {
                val oneHourAgo = System.currentTimeMillis() - 3600000
                val history = db.batteryDao().getHistorySinceSync(oneHourAgo)
                if (history.size >= 2) {
                    val drop = history.first().level - level
                    if (drop >= 15) {
                        notificationHelper.showFastDischargeNotification()
                    }
                }
            }
        }
    }
}
