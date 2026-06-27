package com.monospace.battery.ui.viewmodels

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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
            dao.getLastSessions(50).collect { sessionList ->
                _sessions.value = sessionList

                if (sessionList.isNotEmpty()) {
                    calculateChargerStats(sessionList)
                } else {
                    _chargerStats.value = emptyList()
                }
            }
        }
    }

    private suspend fun calculateChargerStats(sessionList: List<ChargeSession>) =
        withContext(Dispatchers.Default) {
            runCatching {
                // 1. Get total time range for a single DB query
                val minTime = sessionList.minOf { it.startTime }
                val maxTime = sessionList.maxOf { it.endTime ?: System.currentTimeMillis() }

                // 2. Fetch all relevant history at once
                val fullHistory = dao.getHistoryInRange(minTime, maxTime)
                val currentBatteryLevel = _history.value.lastOrNull()?.level

                // 3. Process groups in memory
                val stats = sessionList.groupBy { it.chargeSource }.map { (source, sessions) ->
                    val rates = mutableListOf<Float>()
                    var totalTemp = 0f
                    var tempCount = 0

                    for (session in sessions) {
                        val endTime = session.endTime ?: System.currentTimeMillis()
                        val durationMins = (endTime - session.startTime) / 60000f

                        if (durationMins > 0.5f) {
                            val endLevel =
                                session.endLevel ?: currentBatteryLevel ?: session.startLevel
                            rates.add((endLevel - session.startLevel).coerceAtLeast(0) / durationMins)
                        }

                        // Filter history for this specific session from the full list
                        val sessionHistory = fullHistory.filter {
                            it.timestamp in session.startTime..endTime
                        }

                        if (sessionHistory.isNotEmpty()) {
                            totalTemp += sessionHistory.map { it.temperature }.average().toFloat()
                            tempCount++
                        }
                    }

                    val avgRate = if (rates.isNotEmpty()) rates.average().toFloat() else 0f
                    val avgTemp = if (tempCount > 0) totalTemp / tempCount else 0f

                    val stability = if (rates.size >= 2 && avgRate > 0) {
                        val variance = rates.map {
                            (it - avgRate) * (it - avgRate)
                        }.average().toFloat()

                        val stdDev = sqrt(variance.toDouble()).toFloat()

                        (1f - (stdDev / avgRate)).coerceIn(0f, 1f)
                    } else 1f

                    ChargerStats(
                        source = source,
                        sessionCount = sessions.size,
                        averageRate = avgRate,
                        averageTemp = avgTemp / 10f,
                        stability = stability
                    )
                }
                _chargerStats.value = stats
            }.onFailure { e ->
                Log.e(TAG, "Error calculating charger stats", e)
            }
        }

    companion object {
        private const val TAG = "HistoryViewModel"
    }
}
