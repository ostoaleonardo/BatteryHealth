package com.monospace.battery.data.models

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "battery_history")
data class BatteryHistoryEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val level: Int,
    val temperature: Int = 0
)

@Entity(tableName = "charge_sessions")
data class ChargeSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startTime: Long,
    val endTime: Long? = null,
    val startLevel: Int,
    val endLevel: Int? = null,
    val chargeSource: Int
)

@Entity(tableName = "screen_events")
data class ScreenEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val isScreenOn: Boolean,
    val batteryLevel: Int
)

data class BatteryTip(
    val titleRes: Int,
    val descriptionRes: Int,
    val iconVector: ImageVector
)
