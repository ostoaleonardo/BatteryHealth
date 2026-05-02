package com.monospace.battery.helpers

import android.content.Context
import androidx.core.content.edit

class SharedPreferences(
    private val context: Context
) {

    private fun getPrefs(file: String) = context.getSharedPreferences(file, Context.MODE_PRIVATE)

    fun getString(file: String, key: String, defaultValue: String? = null): String? {
        return getPrefs(file).getString(key, defaultValue)
    }

    fun setString(file: String, key: String, value: String?) {
        getPrefs(file).edit {
            putString(key, value)
        }
    }

    fun setLong(file: String, key: String, value: Long) {
        getPrefs(file).edit {
            putLong(key, value)
        }
    }

    fun remove(file: String, key: String) {
        getPrefs(file).edit {
            remove(key)
        }
    }

    fun getBoolean(file: String, key: String, defaultValue: Boolean = false): Boolean {
        return getPrefs(file).getBoolean(key, defaultValue)
    }

    fun setBoolean(file: String, key: String, value: Boolean) {
        getPrefs(file).edit {
            putBoolean(key, value)
        }
    }

    fun getInt(file: String, key: String, defaultValue: Int = 0): Int {
        return getPrefs(file).getInt(key, defaultValue)
    }

    fun setInt(file: String, key: String, value: Int) {
        getPrefs(file).edit {
            putInt(key, value)
        }
    }

    companion object {
        const val ALERTS_PREFS = "battery_alerts_prefs"
        const val KEY_HEALTHY_CHARGE = "healthy_charge_enabled"
        const val KEY_HEALTHY_CHARGE_LEVEL = "healthy_charge_level"
        const val KEY_TEMP_ALERT = "temp_alert_enabled"
    }
}
