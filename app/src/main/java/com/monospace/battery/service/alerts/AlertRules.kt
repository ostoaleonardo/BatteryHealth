package com.monospace.battery.service.alerts

import com.monospace.battery.data.models.ScreenEvent
import kotlin.time.Duration.Companion.seconds

object AlertRules {
    const val TEMP_ALERT_CELSIUS = 40
    const val TEMP_REARM_CELSIUS = 38
    const val SLOW_CHARGE_WATTS = 2.0
    val SLOW_CHARGE_DELAY = 45.seconds
    val SLOW_CHARGE_SAMPLE_GAP = 5.seconds
    const val SLOW_CHARGE_SAMPLES = 3
    const val SLOW_CHARGE_MAX_LEVEL = 90
    const val HEALTHY_PLUG_COOLDOWN_MS = 30 * 60_000L
    const val FAST_DISCHARGE_MAX_SCREEN_ON_MS = 6 * 60_000L
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

    fun isFastDischarge(
        oldestLevelInWindow: Int,
        currentLevel: Int,
        samples: Int,
        screenOnMs: Long = 0L
    ) = samples >= 2 &&
            oldestLevelInWindow - currentLevel >= FAST_DISCHARGE_DROP &&
            screenOnMs <= FAST_DISCHARGE_MAX_SCREEN_ON_MS

    fun screenOnMillis(events: List<ScreenEvent>, since: Long, now: Long): Long {
        if (events.isEmpty()) return 0L

        var total = 0L
        var onSince = if (!events.first().isScreenOn) since else -1L

        for ((_, timestamp, isScreenOn) in events) {
            if (isScreenOn) {
                if (onSince < 0) onSince = timestamp
            } else if (onSince >= 0) {
                total += timestamp - onSince
                onSince = -1L
            }
        }

        if (onSince >= 0) total += now - onSince
        return total
    }

    fun cooldownElapsed(now: Long, lastFired: Long, cooldownMs: Long) =
        lastFired == 0L || now - lastFired >= cooldownMs
}
