package com.monospace.battery.service.alerts

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.monospace.battery.core.constants.Constants
import com.monospace.battery.core.utils.BatteryUtils
import com.monospace.battery.data.local.PreferenceManager
import com.monospace.battery.data.local.WidgetsUtils
import com.monospace.battery.data.local.db.BatteryDatabase
import com.monospace.battery.notifications.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BatteryAlertHandler(
    private val context: Context,
    private val scope: CoroutineScope,
    private val prefs: PreferenceManager,
    private val notificationHelper: NotificationHelper,
    private val batteryUtils: BatteryUtils,
    private val db: BatteryDatabase,
    private val clock: () -> Long = System::currentTimeMillis
) {
    private var lastLevel = -1
    private var tempArmed = true
    private var lastFastDischargeAt = 0L
    private var lastHealthyPlugAt = 0L
    private var slowChargeJob: Job? = null

    fun checkAlerts(
        level: Int,
        temperature: Int,
        isCharging: Boolean,
        lastStatus: Int
    ) {
        if (level == -1) return

        if (!isCharging) slowChargeJob?.cancel()
        if (!WidgetsUtils.isWidgetsPurchased(context)) {
            lastLevel = level
            return
        }

        checkHealthyChargeAlert(level, isCharging, lastStatus)
        checkLowBatteryAlert(level, isCharging)
        if (temperature != -1) checkHighTemperatureAlert(temperature)
        checkSlowChargeAlert(isCharging, lastStatus)
        checkFastDischargeAlert(level, isCharging)

        lastLevel = level
    }

    private fun checkHealthyChargeAlert(level: Int, isCharging: Boolean, lastStatus: Int) {
        val enabled = prefs.getAlert(Constants.KEY_HEALTHY_CHARGE_ENABLED, false)
        if (!enabled || !isCharging) return

        val threshold = prefs.getAlert(Constants.KEY_HEALTHY_CHARGE_LEVEL, 80)
        val justPlugged = lastStatus != BatteryManager.BATTERY_STATUS_CHARGING &&
                lastStatus != BatteryManager.BATTERY_STATUS_FULL
        val pluggedAboveThreshold = justPlugged && level >= threshold &&
                AlertRules.cooldownElapsed(clock(), lastHealthyPlugAt, AlertRules.HEALTHY_PLUG_COOLDOWN_MS)

        if (AlertRules.crossedUp(lastLevel, level, threshold) || pluggedAboveThreshold) {
            if (pluggedAboveThreshold) lastHealthyPlugAt = clock()
            notificationHelper.showHealthyChargeNotification()
        }
    }

    private fun checkLowBatteryAlert(level: Int, isCharging: Boolean) {
        val enabled = prefs.getAlert(Constants.KEY_LOW_BATTERY_ENABLED, false)
        val threshold = prefs.getAlert(Constants.KEY_LOW_BATTERY_LEVEL, 20)

        if (enabled && !isCharging && AlertRules.crossedDown(lastLevel, level, threshold)) {
            notificationHelper.showLowBatteryNotification(threshold)
        }
    }

    private fun checkHighTemperatureAlert(temperature: Int) {
        val step = AlertRules.tempStep(temperature / 10, tempArmed)
        tempArmed = step.armed

        if (step.fire && prefs.getAlert(Constants.KEY_TEMP_ALERT_ENABLED, false)) {
            notificationHelper.showTempAlertNotification()
        }
    }

    private fun checkSlowChargeAlert(isCharging: Boolean, lastStatus: Int) {
        val enabled = prefs.getAlert(Constants.KEY_SLOW_CHARGE_ENABLED, false)
        val justStarted = lastStatus != BatteryManager.BATTERY_STATUS_CHARGING

        if (!enabled || !isCharging || !justStarted) return

        slowChargeJob?.cancel()
        slowChargeJob = scope.launch {
            delay(AlertRules.SLOW_CHARGE_DELAY)

            var bestSpeed = 0.0
            repeat(AlertRules.SLOW_CHARGE_SAMPLES) { index ->
                if (index > 0) delay(AlertRules.SLOW_CHARGE_SAMPLE_GAP)

                val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
                val voltage = intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0
                val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, 0) ?: 0

                if (status != BatteryManager.BATTERY_STATUS_CHARGING ||
                    level >= AlertRules.SLOW_CHARGE_MAX_LEVEL ||
                    !prefs.getAlert(Constants.KEY_SLOW_CHARGE_ENABLED, false)
                ) return@launch

                bestSpeed = maxOf(bestSpeed, batteryUtils.getChargeSpeed(voltage, true))
            }

            if (AlertRules.isSlowCharge(bestSpeed)) notificationHelper.showSlowChargeNotification()
        }
    }

    private fun checkFastDischargeAlert(level: Int, isCharging: Boolean) {
        val enabled = prefs.getAlert(Constants.KEY_FAST_DISCHARGE_ENABLED, false)

        if (!enabled || isCharging || lastLevel == -1 || level >= lastLevel) return
        if (!AlertRules.cooldownElapsed(
                clock(), lastFastDischargeAt, AlertRules.FAST_DISCHARGE_COOLDOWN_MS
            )
        ) return

        scope.launch {
            val since = clock() - AlertRules.FAST_DISCHARGE_WINDOW_MS
            val history = db.batteryDao().getHistorySinceSync(since)
            val screenOnMs = AlertRules.screenOnMillis(
                db.batteryDao().getScreenEventsSince(since), since, clock()
            )

            if (AlertRules.isFastDischarge(
                    history.firstOrNull()?.level ?: level, level, history.size, screenOnMs
                )
            ) {
                lastFastDischargeAt = clock()
                notificationHelper.showFastDischargeNotification()
            }
        }
    }
}
