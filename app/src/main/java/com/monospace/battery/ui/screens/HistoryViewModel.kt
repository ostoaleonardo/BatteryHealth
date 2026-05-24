package com.monospace.battery.ui.screens

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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

    companion object {
        private const val TAG = "HistoryViewModel"
    }
}
