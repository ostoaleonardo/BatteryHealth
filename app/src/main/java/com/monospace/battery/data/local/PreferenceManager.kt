package com.monospace.battery.data.local

import android.content.Context
import androidx.core.content.edit

class PreferenceManager(private val context: Context) {

    private fun getPrefs(file: String) = context.getSharedPreferences(file, Context.MODE_PRIVATE)

    // Overloaded Getters
    fun get(file: String, key: String, defaultValue: String): String =
        getPrefs(file).getString(key, defaultValue) ?: defaultValue

    fun get(file: String, key: String, defaultValue: Int): Int =
        getPrefs(file).getInt(key, defaultValue)

    fun get(file: String, key: String, defaultValue: Long): Long =
        getPrefs(file).getLong(key, defaultValue)

    fun get(file: String, key: String, defaultValue: Boolean): Boolean =
        getPrefs(file).getBoolean(key, defaultValue)

    fun get(file: String, key: String, defaultValue: Float): Float =
        getPrefs(file).getFloat(key, defaultValue)

    fun <T> set(file: String, key: String, value: T) {
        getPrefs(file).edit {
            when (value) {
                is String -> putString(key, value)
                is Int -> putInt(key, value)
                is Long -> putLong(key, value)
                is Boolean -> putBoolean(key, value)
                is Float -> putFloat(key, value)
            }
        }
    }

    fun remove(file: String, key: String) {
        getPrefs(file).edit { remove(key) }
    }
}
