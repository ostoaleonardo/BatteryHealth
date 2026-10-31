package com.monospace.battery

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.monospace.battery.data.local.db.BatteryDao
import com.monospace.battery.data.local.db.BatteryDatabase
import com.monospace.battery.data.models.BatteryHistoryEntry
import com.monospace.battery.data.models.ChargeSession
import com.monospace.battery.data.models.ScreenEvent
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BatteryDaoTest {

    private lateinit var db: BatteryDatabase
    private lateinit var dao: BatteryDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, BatteryDatabase::class.java).build()
        dao = db.batteryDao()
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun closeAllActiveSessionsOnlyTouchesOpenOnes() = runBlocking {
        dao.insertChargeSession(ChargeSession(startTime = 1, endTime = 5, startLevel = 10, endLevel = 20, chargeSource = 1))
        dao.insertChargeSession(ChargeSession(startTime = 10, startLevel = 30, chargeSource = 2))
        dao.insertChargeSession(ChargeSession(startTime = 20, startLevel = 40, chargeSource = 1))

        dao.closeAllActiveSessions(endTime = 99, endLevel = 80)

        assertNull(dao.getActiveSession())
        val sessions = dao.getAllActiveSessions()
        assertEquals(0, sessions.size)

        db.openHelper.readableDatabase.query("SELECT startTime, endTime, endLevel FROM charge_sessions ORDER BY startTime").use {
            it.moveToFirst()
            assertEquals(5L, it.getLong(1))
            assertEquals(20, it.getInt(2))
            it.moveToNext()
            assertEquals(99L, it.getLong(1))
            assertEquals(80, it.getInt(2))
            it.moveToNext()
            assertEquals(99L, it.getLong(1))
        }
    }

    @Test
    fun activeSessionIsTheLatestOpenOne() = runBlocking {
        dao.insertChargeSession(ChargeSession(startTime = 1, startLevel = 10, chargeSource = 1))
        dao.insertChargeSession(ChargeSession(startTime = 50, startLevel = 60, chargeSource = 2))

        assertEquals(60, dao.getActiveSession()?.startLevel)
    }

    @Test
    fun historyQueriesReturnOrderedRanges() = runBlocking {
        listOf(300L, 100L, 200L, 400L).forEach {
            dao.insertBatteryEntry(BatteryHistoryEntry(timestamp = it, level = (it / 10).toInt(), temperature = 300))
        }

        assertEquals(listOf(200L, 300L, 400L), dao.getHistorySinceSync(200).map { it.timestamp })
        assertEquals(listOf(100L, 200L, 300L), dao.getHistoryInRange(100, 300).map { it.timestamp })
    }

    @Test
    fun deleteBeforeRemovesOnlyOldRows() = runBlocking {
        listOf(100L, 200L, 300L).forEach {
            dao.insertBatteryEntry(BatteryHistoryEntry(timestamp = it, level = 50, temperature = 300))
            dao.insertScreenEvent(ScreenEvent(timestamp = it, isScreenOn = true, batteryLevel = 50))
        }

        dao.deleteHistoryBefore(250)
        dao.deleteScreenEventsBefore(250)

        assertEquals(listOf(300L), dao.getHistorySinceSync(0).map { it.timestamp })
        db.openHelper.readableDatabase.query("SELECT COUNT(*) FROM screen_events").use {
            it.moveToFirst()
            assertEquals(1, it.getInt(0))
        }
    }

    @Test
    fun clearAllDataEmptiesEveryTable() = runBlocking {
        dao.insertBatteryEntry(BatteryHistoryEntry(timestamp = 1, level = 50, temperature = 300))
        dao.insertChargeSession(ChargeSession(startTime = 1, startLevel = 10, chargeSource = 1))
        dao.insertScreenEvent(ScreenEvent(timestamp = 1, isScreenOn = true, batteryLevel = 50))

        dao.clearAllData()

        assertEquals(0, dao.getHistorySinceSync(0).size)
        assertNull(dao.getActiveSession())
        assertNotNull(db)
    }
}
