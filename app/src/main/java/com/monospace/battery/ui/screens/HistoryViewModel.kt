package com.monospace.battery.ui.screens

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.monospace.battery.core.constants.Constants
import com.monospace.battery.data.local.BatteryTipsProvider
import com.monospace.battery.data.local.db.BatteryDatabase
import com.monospace.battery.data.models.BatteryHistoryEntry
import com.monospace.battery.data.models.BatteryTip
import com.monospace.battery.data.models.ChargeSession
import com.monospace.battery.data.models.ChargerStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.sqrt

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val db = BatteryDatabase.getDatabase(application)
    private val dao = db.batteryDao()

    private val _history = MutableStateFlow<List<BatteryHistoryEntry>>(emptyList())
    val history: StateFlow<List<BatteryHistoryEntry>> = _history.asStateFlow()

    private val _sessions = MutableStateFlow<List<ChargeSession>>(emptyList())
    val sessions: StateFlow<List<ChargeSession>> = _sessions.asStateFlow()

    private val _chargerStats = MutableStateFlow<List<ChargerStats>>(emptyList())
    val chargerStats: StateFlow<List<ChargerStats>> = _chargerStats.asStateFlow()

    private val _currentTip = MutableStateFlow(BatteryTipsProvider.getInitialTip())
    val currentTip: StateFlow<Pair<Int, BatteryTip>> = _currentTip.asStateFlow()

    private val _sot = MutableStateFlow(Constants.EMPTY_VALUE_DASH)
    val sot: StateFlow<String> = _sot.asStateFlow()

    private val _batteryUsed = MutableStateFlow(0)
    val batteryUsed: StateFlow<Int> = _batteryUsed.asStateFlow()

    private val _activeDrainRate = MutableStateFlow(Constants.EMPTY_VALUE_DASH)
    val activeDrainRate: StateFlow<String> = _activeDrainRate.asStateFlow()

    private val _estimatedFullSot = MutableStateFlow(Constants.EMPTY_VALUE_DASH)
    val estimatedFullSot: StateFlow<String> = _estimatedFullSot.asStateFlow()

    fun nextTip() {
        _currentTip.value = BatteryTipsProvider.getRandomTip(_currentTip.value.first)
    }

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val since = System.currentTimeMillis() - 24 * 60 * 60 * 1000
            dao.getHistorySince(since).collect {
                _history.value = it
                calculateStats()
            }
        }

        viewModelScope.launch {
            dao.getLastSessions(50).collect {
                _sessions.value = it
                calculateChargerStats(it)
            }
        }
    }

    private suspend fun calculateChargerStats(sessions: List<ChargeSession>) {
        runCatching {
            if (sessions.isEmpty()) return@runCatching emptyList()

            val currentBatteryLevel = _history.value.lastOrNull()?.level

            sessions.groupBy { it.chargeSource }.map { (source, sessionList) ->
                var totalRate = 0f
                val rates = mutableListOf<Float>()
                var totalTemp = 0f
                var tempCount = 0

                for (session in sessionList) {
                    val endTime = session.endTime ?: System.currentTimeMillis()
                    val endLevel = session.endLevel ?: currentBatteryLevel ?: session.startLevel

                    val durationMins = (endTime - session.startTime) / 60000f
                    val gain = (endLevel - session.startLevel).coerceAtLeast(0)

                    if (durationMins > 0.5f) { // Need at least 30s of data
                        val rate = gain / durationMins
                        totalRate += rate
                        rates.add(rate)
                    }

                    // Get average temperature during this session
                    val history = dao.getHistoryInRange(session.startTime, endTime)

                    if (history.isNotEmpty()) {
                        totalTemp += history.map { it.temperature }.average().toFloat()
                        tempCount++
                    }
                }

                val avgRate = if (rates.isNotEmpty()) totalRate / rates.size else 0f
                val avgTemp = if (tempCount > 0) totalTemp / tempCount else 0f

                // Stability calculation: 1 - (stdDev / avgRate)
                val stability = if (rates.size >= 2 && avgRate > 0) {
                    val variance = rates.map { (it - avgRate) * (it - avgRate) }.average().toFloat()
                    val stdDev = sqrt(variance.toDouble()).toFloat()
                    (1f - (stdDev / avgRate)).coerceIn(0f, 1f)
                } else 1f

                ChargerStats(
                    source = source,
                    sessionCount = sessionList.size,
                    averageRate = avgRate,
                    averageTemp = avgTemp / 10f, // Convert to °C
                    stability = stability
                )
            }
        }.onSuccess { stats ->
            _chargerStats.value = stats
        }.onFailure { e ->
            Log.e(TAG, "Error calculating charger stats", e)
        }
    }

    private suspend fun calculateStats() {
        runCatching {
            val lastFullCharge = dao.getLastFullChargeTime()
                ?: (System.currentTimeMillis() - 24 * 60 * 60 * 1000)

            val history = dao.getHistorySinceSync(lastFullCharge)
            if (history.size < 2) {
                resetStats()
                return@runCatching
            }

            // 1. Find the peak battery level in this period to use as starting point for discharge
            val maxEntry = history.maxByOrNull { it.level } ?: history.first()
            val startTime = maxEntry.timestamp
            val startLvl = maxEntry.level
            val currentLvl = history.last().level

            // 2. SOT Calculation since the peak level
            val events = dao.getScreenEventsSince(startTime)
            var totalMillis = 0L
            var lastOnTime: Long? = null

            for (event in events) {
                if (event.isScreenOn) lastOnTime = event.timestamp
                else if (lastOnTime != null) {
                    totalMillis += (event.timestamp - lastOnTime)
                    lastOnTime = null
                }
            }
            // If screen is currently ON, add time since last event
            if (lastOnTime != null) {
                totalMillis += (System.currentTimeMillis() - lastOnTime)
            }

            if (totalMillis > 0) {
                val hrs = totalMillis / 3_600_000
                val mins = (totalMillis / 60_000) % 60
                _sot.value = "${hrs}h ${mins}m"
            }

            // 3. Battery Drop from the peak
            val drop = (startLvl - currentLvl).coerceAtLeast(0)
            _batteryUsed.value = drop

            // 4. Projections (Only if not currently charging)
            val isCharging = history.lastOrNull()?.let { _ ->
                history.size >= 2 && history.last().level > history[history.size - 2].level 
            } ?: false

            if (drop >= 1 && totalMillis > 0 && !isCharging) {
                val hrsFloat = totalMillis.toFloat() / 3_600_000f
                val rate = drop.toFloat() / hrsFloat
                _activeDrainRate.value =
                    String.format(Locale.getDefault(), Constants.FORMAT_PERCENT_ONE_DECIMAL, rate)

                val estTotalMs = (totalMillis.toFloat() / drop.toFloat() * 100f).toLong()
                val estHrs = estTotalMs / 3_600_000
                if (estHrs in 2..24) {
                    val estMins = (estTotalMs / 60_000) % 60
                    _estimatedFullSot.value = "${estHrs}h ${estMins}m"
                } else {
                    _estimatedFullSot.value = Constants.EMPTY_VALUE_DASH
                }
            } else {
                _activeDrainRate.value = Constants.EMPTY_VALUE_DASH
                _estimatedFullSot.value = Constants.EMPTY_VALUE_DASH
            }
        }.onFailure { e ->
            Log.e(TAG, "Error calculating stats", e)
            resetStats()
        }
    }

    private fun resetStats() {
        _sot.value = Constants.EMPTY_VALUE_DASH
        _batteryUsed.value = 0
        _activeDrainRate.value = Constants.EMPTY_VALUE_DASH
        _estimatedFullSot.value = Constants.EMPTY_VALUE_DASH
    }

    companion object {
        private const val TAG = "HistoryViewModel"
    }
}
