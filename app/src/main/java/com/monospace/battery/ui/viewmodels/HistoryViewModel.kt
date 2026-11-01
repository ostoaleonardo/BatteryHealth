package com.monospace.battery.ui.viewmodels

import android.app.Application
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.monospace.battery.core.constants.Constants
import com.monospace.battery.core.utils.ChargerStatsCalculator
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
import kotlinx.coroutines.withTimeoutOrNull

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
            while (true) {
                val since = System.currentTimeMillis() - Constants.HISTORY_CHART_WINDOW_MS
                withTimeoutOrNull(Constants.HISTORY_CHART_REFRESH) {
                    dao.getHistorySince(since).collect { _history.value = it }
                }
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
                val currentBatteryLevel = currentLevel()

                val stats = ChargerStatsCalculator.calculate(
                    sessionList, fullHistory, currentBatteryLevel, System.currentTimeMillis()
                )
                _chargerStats.value = stats
            }.onFailure { e ->
                Log.e(TAG, "Error calculating charger stats", e)
            }
        }

    private fun currentLevel(): Int? {
        val intent = getApplication<Application>()
            .registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
        return if (level >= 0 && scale > 0) level * 100 / scale else _history.value.lastOrNull()?.level
    }

    companion object {
        private const val TAG = "HistoryViewModel"
    }
}
