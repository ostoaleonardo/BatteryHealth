package com.monospace.battery.service.alerts

object AlertRules {
    const val TEMP_ALERT_CELSIUS = 40
    const val TEMP_REARM_CELSIUS = 38
    const val SLOW_CHARGE_WATTS = 2.0
    const val SLOW_CHARGE_DELAY_MS = 45_000L
    const val FAST_DISCHARGE_DROP = 15
    const val FAST_DISCHARGE_WINDOW_MS = 3_600_000L
    const val FAST_DISCHARGE_COOLDOWN_MS = 30 * 60_000L

    data class TempStep(val fire: Boolean, val armed: Boolean)

    fun crossedUp(previous: Int, current: Int, threshold: Int) =
        previous != -1 && previous < threshold && current >= threshold

    fun crossedDown(previous: Int, current: Int, threshold: Int) =
        previous != -1 && previous > threshold && current <= threshold

    fun tempStep(tempCelsius: Int, armed: Boolean): TempStep = when {
        armed && tempCelsius >= TEMP_ALERT_CELSIUS -> TempStep(fire = true, armed = false)
        !armed && tempCelsius < TEMP_REARM_CELSIUS -> TempStep(fire = false, armed = true)
        else -> TempStep(fire = false, armed = armed)
    }

    fun isSlowCharge(watts: Double) = watts > 0 && watts < SLOW_CHARGE_WATTS

    fun isFastDischarge(oldestLevelInWindow: Int, currentLevel: Int, samples: Int) =
        samples >= 2 && oldestLevelInWindow - currentLevel >= FAST_DISCHARGE_DROP

    fun cooldownElapsed(now: Long, lastFired: Long, cooldownMs: Long) =
        lastFired == 0L || now - lastFired >= cooldownMs
}
