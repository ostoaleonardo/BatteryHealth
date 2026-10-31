package com.monospace.battery

import android.content.Context
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.monospace.battery.data.local.db.BatteryDatabase
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BatteryDatabaseMigrationTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        BatteryDatabase::class.java
    )

    @After
    fun cleanUp() {
        context.deleteDatabase(MIGRATED_DB)
        context.deleteDatabase(FRESH_DB)
    }

    @Test
    fun migration1To2KeepsDataAndAddsIndexes() {
        helper.createDatabase(MIGRATED_DB, 1).apply {
            execSQL("INSERT INTO battery_history (timestamp, level, temperature) VALUES (1000, 50, 300)")
            execSQL("INSERT INTO charge_sessions (startTime, endTime, startLevel, endLevel, chargeSource) VALUES (1000, NULL, 20, NULL, 2)")
            execSQL("INSERT INTO screen_events (timestamp, isScreenOn, batteryLevel) VALUES (1000, 1, 50)")
            close()
        }

        val db = helper.runMigrationsAndValidate(
            MIGRATED_DB, 2, true, BatteryDatabase.MIGRATION_1_2
        )

        listOf("battery_history", "charge_sessions", "screen_events").forEach { table ->
            db.query("SELECT COUNT(*) FROM $table").use {
                it.moveToFirst()
                assertEquals(table, 1, it.getInt(0))
            }
        }

        val indexes = mutableSetOf<String>()
        db.query("SELECT name FROM sqlite_master WHERE type = 'index' AND name LIKE 'index_%'").use {
            while (it.moveToNext()) indexes.add(it.getString(0))
        }
        assertEquals(
            setOf(
                "index_battery_history_timestamp",
                "index_charge_sessions_endTime",
                "index_screen_events_timestamp"
            ),
            indexes
        )
    }

    @Test
    fun roomOpensMigratedDatabaseWithoutLosingRows() {
        helper.createDatabase(MIGRATED_DB, 1).apply {
            execSQL("INSERT INTO charge_sessions (startTime, endTime, startLevel, endLevel, chargeSource) VALUES (1000, NULL, 20, NULL, 2)")
            close()
        }

        val db = Room.databaseBuilder(context, BatteryDatabase::class.java, MIGRATED_DB)
            .addMigrations(BatteryDatabase.MIGRATION_1_2)
            .build()

        try {
            runBlocking {
                val session = db.batteryDao().getActiveSession()
                assertNotNull(session)
                assertEquals(20, session!!.startLevel)
            }
        } finally {
            db.close()
        }
    }

    @Test
    fun freshInstallCreatesCurrentSchema() {
        val db = Room.databaseBuilder(context, BatteryDatabase::class.java, FRESH_DB)
            .addMigrations(BatteryDatabase.MIGRATION_1_2)
            .build()

        try {
            runBlocking { assertNull(db.batteryDao().getActiveSession()) }
            assertEquals(2, db.openHelper.readableDatabase.version)
        } finally {
            db.close()
        }
    }

    private companion object {
        const val MIGRATED_DB = "migration-test"
        const val FRESH_DB = "fresh-install-test"
    }
}
