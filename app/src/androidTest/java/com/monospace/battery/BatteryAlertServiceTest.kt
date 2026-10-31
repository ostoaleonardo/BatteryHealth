package com.monospace.battery

import android.app.ActivityManager
import android.content.Context
import android.os.Build
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
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BatteryAlertServiceTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val dao get() = BatteryDatabase.getDatabase(context).batteryDao()

    @Before
    fun setUp() {
        assumeTrue(Build.FINGERPRINT.startsWith("generic") || Build.PRODUCT.contains("sdk"))
        BatteryAlertService.stop(context)
        assertTrue(waitUntil { !isServiceRunning() })
        PreferenceManager(context).set(Constants.PREFS_ALERTS, Constants.KEY_ACTIVE_MONITORING, false)
        runBlocking { dao.clearAllData() }
    }

    @After
    fun tearDown() {
        BatteryAlertService.stop(context)
        waitUntil { !isServiceRunning() }
        runBlocking { dao.clearAllData() }
    }

    @Test
    fun startsAndClosesSessionsLeftOpenByAKilledProcess() {
        runBlocking {
            dao.insertChargeSession(
                ChargeSession(startTime = System.currentTimeMillis() - 3_600_000, startLevel = 40, chargeSource = 1)
            )
        }

        BatteryAlertService.start(context)

        assertTrue(waitUntil { runBlocking { dao.getActiveSession() } == null })
        assertTrue(waitUntil { isServiceRunning() })
    }

    @Test
    fun survivesRestartsWithoutCrashing() {
        repeat(3) {
            BatteryAlertService.start(context)
            assertTrue(waitUntil { isServiceRunning() })
            BatteryAlertService.stop(context)
            assertTrue(waitUntil { !isServiceRunning() })
        }

        BatteryAlertService.start(context)
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
