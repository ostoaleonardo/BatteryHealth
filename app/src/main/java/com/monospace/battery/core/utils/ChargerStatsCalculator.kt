package com.monospace.battery.core.utils

import com.monospace.battery.data.models.BatteryHistoryEntry
import com.monospace.battery.data.models.ChargeSession
import com.monospace.battery.data.models.ChargerStats
import kotlin.math.sqrt

object ChargerStatsCalculator {

    fun calculate(
        sessions: List<ChargeSession>,
        history: List<BatteryHistoryEntry>,
        currentLevel: Int?,
        now: Long
    ): List<ChargerStats> = sessions.groupBy { it.chargeSource }.map { (source, group) ->
        val rates = mutableListOf<Float>()
        var totalTemp = 0f
        var tempCount = 0

        for ((_, startTime, rawEnd, startLevel, rawEndLevel) in group) {
            val endTime = rawEnd ?: now
            val durationMins = (endTime - startTime) / 60000f

            if (durationMins > 0.5f) {
                val endLevel = rawEndLevel ?: currentLevel ?: startLevel
                rates.add((endLevel - startLevel).coerceAtLeast(0) / durationMins)
            }

            val sessionHistory = history.filter { it.timestamp in startTime..endTime }

            if (sessionHistory.isNotEmpty()) {
                totalTemp += sessionHistory.map { it.temperature }.average().toFloat()
                tempCount++
            }
        }

        val avgRate = if (rates.isNotEmpty()) rates.average().toFloat() else 0f
        val avgTemp = if (tempCount > 0) totalTemp / tempCount else 0f

        val stability = if (rates.size >= 2 && avgRate > 0) {
            val variance = rates.map { (it - avgRate) * (it - avgRate) }.average().toFloat()
            val stdDev = sqrt(variance.toDouble()).toFloat()

            (1f - (stdDev / avgRate)).coerceIn(0f, 1f)
        } else 1f

        ChargerStats(
            source = source,
            sessionCount = group.size,
            averageRate = avgRate,
            averageTemp = avgTemp / 10f,
            stability = stability
        )
    }
}
