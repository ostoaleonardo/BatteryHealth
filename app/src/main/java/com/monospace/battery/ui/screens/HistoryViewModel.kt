package com.monospace.battery.ui.screens

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.monospace.battery.data.local.db.BatteryDatabase
import com.monospace.battery.data.models.BatteryHistoryEntry
import com.monospace.battery.data.models.ChargeSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val db = BatteryDatabase.getDatabase(application)
    private val dao = db.batteryDao()

    private val _history = MutableStateFlow<List<BatteryHistoryEntry>>(emptyList())
    val history: StateFlow<List<BatteryHistoryEntry>> = _history.asStateFlow()

    private val _sessions = MutableStateFlow<List<ChargeSession>>(emptyList())
    val sessions: StateFlow<List<ChargeSession>> = _sessions.asStateFlow()

    private val _sot = MutableStateFlow("0h 0m")
    val sot: StateFlow<String> = _sot.asStateFlow()

    private val _batteryUsed = MutableStateFlow(0)
    val batteryUsed: StateFlow<Int> = _batteryUsed.asStateFlow()

    private val _activeDrainRate = MutableStateFlow(0f)
    val activeDrainRate: StateFlow<Float> = _activeDrainRate.asStateFlow()

    private val _estimatedFullSot = MutableStateFlow("0h 0m")
    val estimatedFullSot: StateFlow<String> = _estimatedFullSot.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            // Load 24h history
            val since = System.currentTimeMillis() - 24 * 60 * 60 * 1000
            dao.getHistorySince(since).collect {
                Log.d("HistoryViewModel", "Collected history: size=${it.size}")
                _history.value = it
            }
        }

        viewModelScope.launch {
            // Load last 10 charge sessions
            dao.getLastSessions(10).collect {
                _sessions.value = it
            }
        }

        viewModelScope.launch {
            calculateStats()
        }
    }

    private suspend fun calculateStats() {
        val lastFullCharge = dao.getLastFullChargeTime() ?: (System.currentTimeMillis() - 24 * 60 * 60 * 1000)
        Log.d("HistoryViewModel", "Calculating stats since: $lastFullCharge")
        
        // 1. Calculate SOT
        val events = dao.getScreenEventsSince(lastFullCharge)
        Log.d("HistoryViewModel", "Found ${events.size} screen events")
        
        var totalMillis = 0L
        var lastOnTime: Long? = null
        
        for (event in events) {
            if (event.isScreenOn) {
                lastOnTime = event.timestamp
            } else if (lastOnTime != null) {
                totalMillis += (event.timestamp - lastOnTime)
                lastOnTime = null
            }
        }
        
        val hours = totalMillis / 3_600_000
        val minutes = (totalMillis / 60_000) % 60
        _sot.value = "${hours}h ${minutes}m"

        // 2. Calculate Battery Drop
        // Get first level recorded since full charge and current level
        val historySinceFull = dao.getHistorySinceSync(lastFullCharge)
        if (historySinceFull.isNotEmpty()) {
            val startLevel = historySinceFull.first().level
            val currentLevel = historySinceFull.last().level
            val drop = (startLevel - currentLevel).coerceAtLeast(0)
            _batteryUsed.value = drop

            // 3. Extrapolate stats
            if (drop > 0 && totalMillis > 0) {
                // Active Drain Rate (% per hour)
                val hoursFloat = totalMillis.toFloat() / 3_600_000f
                _activeDrainRate.value = drop.toFloat() / hoursFloat
                
                // Estimated SOT for 100%
                val estimatedTotalMillis = (totalMillis.toFloat() / drop.toFloat() * 100f).toLong()
                val estHours = estimatedTotalMillis / 3_600_000
                val estMins = (estimatedTotalMillis / 60_000) % 60
                _estimatedFullSot.value = "${estHours}h ${estMins}m"
            }
        }
    }
}
