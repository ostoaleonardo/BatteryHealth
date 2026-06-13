package com.monospace.battery.data.mocks

import android.os.BatteryManager
import com.monospace.battery.data.local.BatteryTipsProvider
import com.monospace.battery.data.models.BatteryHistoryEntry
import com.monospace.battery.data.models.BatteryTip
import com.monospace.battery.data.models.ChargeSession
import com.monospace.battery.data.models.ChargerStats

object MockDataProvider {
    val dummyHistory: List<BatteryHistoryEntry>
        get() {
            val now = System.currentTimeMillis()
            return listOf(
                BatteryHistoryEntry(timestamp = now - 3600000 * 4, level = 90),
                BatteryHistoryEntry(timestamp = now - 3600000 * 3, level = 85),
                BatteryHistoryEntry(timestamp = now - 3600000 * 2, level = 70),
                BatteryHistoryEntry(timestamp = now - 3600000, level = 60),
                BatteryHistoryEntry(timestamp = now, level = 45)
            )
        }

    val dummySessions: List<ChargeSession>
        get() {
            val now = System.currentTimeMillis()
            return listOf(
                ChargeSession(
                    startTime = now - 7200000,
                    endTime = now - 3600000,
                    startLevel = 20,
                    endLevel = 80,
                    chargeSource = BatteryManager.BATTERY_PLUGGED_AC
                ),
                ChargeSession(
                    startTime = now - 14400000,
                    endTime = now - 10800000,
                    startLevel = 15,
                    endLevel = 90,
                    chargeSource = BatteryManager.BATTERY_PLUGGED_USB
                )
            )
        }

    val dummyChargerStats: List<ChargerStats>
        get() = listOf(
            ChargerStats(
                source = BatteryManager.BATTERY_PLUGGED_AC,
                sessionCount = 12,
                averageRate = 1.45f,
                averageTemp = 34.5f,
                stability = 0.98f
            ),
            ChargerStats(
                source = BatteryManager.BATTERY_PLUGGED_USB,
                sessionCount = 4,
                averageRate = 0.65f,
                averageTemp = 31.2f,
                stability = 0.85f
            )
        )

    val dummyTip: BatteryTip
        get() = BatteryTipsProvider.getInitialTip().second
}
