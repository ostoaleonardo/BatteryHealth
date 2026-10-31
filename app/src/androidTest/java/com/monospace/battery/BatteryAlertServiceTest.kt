package com.monospace.battery

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.monospace.battery.core.constants.Constants
import com.monospace.battery.data.local.PreferenceManager
import com.monospace.battery.data.local.db.BatteryDatabase
import com.monospace.battery.data.models.ChargeSession
import com.monospace.battery.service.alerts.BatteryAlertService
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BatteryAlertServiceTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val dao get() = BatteryDatabase.getDatabase(context).batteryDao()

    @Before
    fun setUp() {
        PreferenceManager(context).set(Constants.PREFS_ALERTS, Constants.KEY_ACTIVE_MONITORING, false)
        runBlocking { dao.clearAllData() }
    }

    @After
    fun tearDown() {
        context.stopService(Intent(context, BatteryAlertService::class.java))
        runBlocking { dao.clearAllData() }
    }

    @Test
    fun startsAndClosesSessionsLeftOpenByAKilledProcess() {
        runBlocking {
            dao.insertChargeSession(
                ChargeSession(startTime = System.currentTimeMillis() - 3_600_000, startLevel = 40, chargeSource = 1)
            )
        }

        context.startForegroundService(Intent(context, BatteryAlertService::class.java))

        assertTrue(waitUntil { runBlocking { dao.getActiveSession() } == null })
        assertTrue(waitUntil { isServiceRunning() })
    }

    @Test
    fun survivesRestartsWithoutCrashing() {
        val intent = Intent(context, BatteryAlertService::class.java)

        repeat(3) {
            context.startForegroundService(intent)
            assertTrue(waitUntil { isServiceRunning() })
            context.stopService(intent)
            assertTrue(waitUntil { !isServiceRunning() })
        }

        context.startForegroundService(intent)
        assertTrue(waitUntil { isServiceRunning() })
    }

    @Suppress("DEPRECATION")
    private fun isServiceRunning(): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return manager.getRunningServices(Int.MAX_VALUE).any {
            it.service.className == BatteryAlertService::class.java.name
        }
    }

    private fun waitUntil(timeoutMs: Long = 10_000, condition: () -> Boolean): Boolean {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            if (condition()) return true
            Thread.sleep(100)
        }
        return false
    }
}
