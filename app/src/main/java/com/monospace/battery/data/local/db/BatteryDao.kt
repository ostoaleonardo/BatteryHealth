package com.monospace.battery.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.monospace.battery.data.models.BatteryHistoryEntry
import com.monospace.battery.data.models.ChargeSession
import com.monospace.battery.data.models.ScreenEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface BatteryDao {
    // Battery History
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatteryEntry(entry: BatteryHistoryEntry)

    @Query("SELECT * FROM battery_history WHERE timestamp >= :since ORDER BY timestamp ASC")
    fun getHistorySince(since: Long): Flow<List<BatteryHistoryEntry>>

    @Query("SELECT * FROM battery_history WHERE timestamp >= :since ORDER BY timestamp ASC")
    suspend fun getHistorySinceSync(since: Long): List<BatteryHistoryEntry>

    // Charge Sessions
    @Insert
    suspend fun insertChargeSession(session: ChargeSession): Long

    @Update
    suspend fun updateChargeSession(session: ChargeSession)

    @Query("SELECT * FROM charge_sessions ORDER BY startTime DESC LIMIT :limit")
    fun getLastSessions(limit: Int): Flow<List<ChargeSession>>

    @Query("SELECT * FROM charge_sessions WHERE endTime IS NULL ORDER BY startTime DESC LIMIT 1")
    suspend fun getActiveSession(): ChargeSession?

    // Screen Events (SOT)
    @Insert
    suspend fun insertScreenEvent(event: ScreenEvent)

    @Query("SELECT * FROM screen_events WHERE timestamp >= :since ORDER BY timestamp ASC")
    suspend fun getScreenEventsSince(since: Long): List<ScreenEvent>

    @Query("SELECT timestamp FROM battery_history WHERE level = 100 ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastFullChargeTime(): Long?
}
