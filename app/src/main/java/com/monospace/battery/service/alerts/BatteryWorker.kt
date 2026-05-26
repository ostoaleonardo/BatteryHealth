package com.monospace.battery.service.alerts

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.monospace.battery.data.local.db.BatteryDatabase
import com.monospace.battery.data.models.BatteryHistoryEntry
import com.monospace.battery.data.models.ChargeSession

class BatteryWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val level = inputData.getInt("level", -1)
        val temperature = inputData.getInt("temperature", -1)
        val isCharging = inputData.getBoolean("is_charging", false)
        val wasCharging = inputData.getBoolean("was_charging", false)
        val source = inputData.getInt("source", 0)
        val timestamp = System.currentTimeMillis()

        if (level == -1) return Result.failure()

        val db = BatteryDatabase.getDatabase(applicationContext)

        return runCatching {
            // 1. Record History Entry
            db.batteryDao().insertBatteryEntry(
                BatteryHistoryEntry(
                    timestamp = timestamp,
                    level = level,
                    temperature = temperature
                )
            )

            // 2. Record Charge Session Logic
            if (isCharging && !wasCharging) {
                db.batteryDao().insertChargeSession(
                    ChargeSession(
                        startTime = timestamp,
                        startLevel = level,
                        chargeSource = source
                    )
                )
            } else if (!isCharging && wasCharging) {
                val activeSession = db.batteryDao().getActiveSession()
                activeSession?.let {
                    db.batteryDao().updateChargeSession(
                        it.copy(
                            endTime = timestamp,
                            endLevel = level
                        )
                    )
                }
            }
            Result.success()
        }.getOrElse { e ->
            Log.e(TAG, "Error recording battery data", e)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "BatteryWorker"

        fun enqueue(
            context: Context,
            level: Int,
            temperature: Int,
            isCharging: Boolean,
            wasCharging: Boolean,
            source: Int
        ) {
            val data = Data.Builder()
                .putInt("level", level)
                .putInt("temperature", temperature)
                .putBoolean("is_charging", isCharging)
                .putBoolean("was_charging", wasCharging)
                .putInt("source", source)
                .build()

            val request = OneTimeWorkRequestBuilder<BatteryWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setInputData(data)
                .build()

            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
