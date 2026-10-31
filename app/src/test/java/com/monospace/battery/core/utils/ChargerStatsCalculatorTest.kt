package com.monospace.battery.core.utils

import com.monospace.battery.data.models.BatteryHistoryEntry
import com.monospace.battery.data.models.ChargeSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChargerStatsCalculatorTest {

    private val minute = 60_000L

    private fun session(
        start: Long,
        durationMinutes: Long,
        startLevel: Int,
        endLevel: Int?,
        source: Int
    ) = ChargeSession(
        startTime = start,
        endTime = start + durationMinutes * minute,
        startLevel = startLevel,
        endLevel = endLevel,
        chargeSource = source
    )

    @Test
    fun groupsSessionsBySourceAndComputesRate() {
        val sessions = listOf(
            session(0, 30, 20, 80, source = 1),
            session(100 * minute, 60, 20, 80, source = 2)
        )

        val stats = ChargerStatsCalculator.calculate(sessions, emptyList(), null, 0).associateBy { it.source }

        assertEquals(2f, stats.getValue(1).averageRate, 0.001f)
        assertEquals(1f, stats.getValue(2).averageRate, 0.001f)
        assertEquals(1, stats.getValue(1).sessionCount)
    }

    @Test
    fun averagesTemperatureInCelsius() {
        val sessions = listOf(session(0, 10, 10, 20, source = 1))
        val history = listOf(
            BatteryHistoryEntry(timestamp = minute, level = 12, temperature = 300),
            BatteryHistoryEntry(timestamp = 2 * minute, level = 14, temperature = 340)
        )

        val stats = ChargerStatsCalculator.calculate(sessions, history, null, 0).single()

        assertEquals(32f, stats.averageTemp, 0.001f)
    }

    @Test
    fun openSessionUsesCurrentLevelAndNow() {
        val open = ChargeSession(startTime = 0, startLevel = 40, chargeSource = 1)

        val stats = ChargerStatsCalculator.calculate(listOf(open), emptyList(), 70, 10 * minute).single()

        assertEquals(3f, stats.averageRate, 0.001f)
    }

    @Test
    fun ignoresVeryShortSessionsInRate() {
        val sessions = listOf(session(0, 0, 10, 90, source = 1))

        val stats = ChargerStatsCalculator.calculate(sessions, emptyList(), null, 0).single()

        assertEquals(0f, stats.averageRate, 0f)
    }

    @Test
    fun stabilityDropsWhenRatesVary() {
        val steady = listOf(
            session(0, 30, 20, 80, source = 1),
            session(100 * minute, 30, 20, 80, source = 1)
        )
        val uneven = listOf(
            session(0, 30, 20, 80, source = 1),
            session(100 * minute, 30, 20, 30, source = 1)
        )

        val steadyStability = ChargerStatsCalculator.calculate(steady, emptyList(), null, 0).single().stability
        val unevenStability = ChargerStatsCalculator.calculate(uneven, emptyList(), null, 0).single().stability

        assertEquals(1f, steadyStability, 0.001f)
        assertTrue(unevenStability < steadyStability)
    }

    @Test
    fun noSessionsMeansNoStats() {
        assertTrue(ChargerStatsCalculator.calculate(emptyList(), emptyList(), null, 0).isEmpty())
    }
}
