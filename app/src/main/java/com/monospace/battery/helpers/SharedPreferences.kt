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
}
