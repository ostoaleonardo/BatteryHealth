package com.monospace.battery.core.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class BatteryUtilsTest {

    @Test
    fun readsMicroampsAndMilliampsAsTheSamePower() {
        assertEquals(6.0, BatteryUtils.chargeWatts(1_500_000L, 4000), 0.0)
        assertEquals(6.0, BatteryUtils.chargeWatts(1_500L, 4000), 0.0)
    }

    @Test
    fun ignoresCurrentSign() {
        assertEquals(6.0, BatteryUtils.chargeWatts(-1_500L, 4000), 0.0)
    }

    @Test
    fun returnsZeroForUnknownReadings() {
        assertEquals(0.0, BatteryUtils.chargeWatts(0L, 4000), 0.0)
        assertEquals(0.0, BatteryUtils.chargeWatts(Long.MIN_VALUE, 4000), 0.0)
        assertEquals(0.0, BatteryUtils.chargeWatts(1_500L, 0), 0.0)
    }

    @Test
    fun discardsImpossiblePower() {
        assertEquals(0.0, BatteryUtils.chargeWatts(100_000_000_000L, 4000), 0.0)
    }

    @Test
    fun roundsToOneDecimal() {
        assertEquals(4.8, BatteryUtils.chargeWatts(1_234L, 3900), 0.0)
    }
}
