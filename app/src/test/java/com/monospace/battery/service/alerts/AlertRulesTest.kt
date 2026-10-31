package com.monospace.battery.service.alerts

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AlertRulesTest {

    @Test
    fun healthyChargeFiresOnlyWhenCrossingThresholdUpwards() {
        assertTrue(AlertRules.crossedUp(79, 80, 80))
        assertFalse(AlertRules.crossedUp(80, 81, 80))
        assertFalse(AlertRules.crossedUp(-1, 85, 80))
    }

    @Test
    fun lowBatteryFiresOnlyWhenCrossingThresholdDownwards() {
        assertTrue(AlertRules.crossedDown(21, 20, 20))
        assertFalse(AlertRules.crossedDown(20, 19, 20))
        assertFalse(AlertRules.crossedDown(-1, 10, 20))
    }

    @Test
    fun temperatureFiresOnceAndRearmsAfterCoolingDown() {
        var step = AlertRules.tempStep(41, armed = true)
        assertTrue(step.fire)
        assertFalse(step.armed)

        step = AlertRules.tempStep(39, step.armed)
        assertFalse(step.fire)
        step = AlertRules.tempStep(40, step.armed)
        assertFalse(step.fire)

        step = AlertRules.tempStep(37, step.armed)
        assertTrue(step.armed)
        assertTrue(AlertRules.tempStep(40, step.armed).fire)
    }

    @Test
    fun slowChargeIgnoresUnknownReadings() {
        assertFalse(AlertRules.isSlowCharge(0.0))
        assertTrue(AlertRules.isSlowCharge(1.5))
        assertFalse(AlertRules.isSlowCharge(2.0))
    }

    @Test
    fun fastDischargeNeedsEnoughSamplesAndDrop() {
        assertTrue(AlertRules.isFastDischarge(90, 75, samples = 2))
        assertFalse(AlertRules.isFastDischarge(90, 76, samples = 5))
        assertFalse(AlertRules.isFastDischarge(90, 70, samples = 1))
    }

    @Test
    fun cooldownBlocksRepeatsUntilElapsed() {
        val cooldown = AlertRules.FAST_DISCHARGE_COOLDOWN_MS
        assertTrue(AlertRules.cooldownElapsed(1_000, 0L, cooldown))
        assertFalse(AlertRules.cooldownElapsed(cooldown, 1L, cooldown))
        assertEquals(true, AlertRules.cooldownElapsed(cooldown + 1, 1L, cooldown))
    }
}
