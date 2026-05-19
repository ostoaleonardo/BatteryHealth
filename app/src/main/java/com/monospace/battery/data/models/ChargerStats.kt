package com.monospace.battery.data.models

data class ChargerStats(
    val source: Int,
    val sessionCount: Int,
    val averageRate: Float, // % per minute
    val averageTemp: Float, // °C
    val stability: Float // 1.0 = highly stable, lower = unstable
)
