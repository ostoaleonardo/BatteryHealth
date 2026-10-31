package com.monospace.battery.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.monospace.battery.core.constants.Constants
import com.monospace.battery.data.models.BatteryHistoryEntry
import com.monospace.battery.data.models.ChargeSession
import com.monospace.battery.data.models.ScreenEvent

@Database(
    entities = [BatteryHistoryEntry::class, ChargeSession::class, ScreenEvent::class],
    version = 2,
    exportSchema = true
)
abstract class BatteryDatabase : RoomDatabase() {
    abstract fun batteryDao(): BatteryDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_battery_history_timestamp` ON `battery_history` (`timestamp`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_charge_sessions_endTime` ON `charge_sessions` (`endTime`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_screen_events_timestamp` ON `screen_events` (`timestamp`)")
            }
        }

        @Volatile
        private var INSTANCE: BatteryDatabase? = null

        fun getDatabase(context: Context): BatteryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BatteryDatabase::class.java,
                    Constants.DATABASE_NAME
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
