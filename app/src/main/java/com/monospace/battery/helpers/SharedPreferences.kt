package com.monospace.battery.helpers

import android.content.Context
import androidx.core.content.edit

class SharedPreferences(
    private val context: Context
) {

    fun getItem(file: String, key: String): String? {
        return context.getSharedPreferences(file, Context.MODE_PRIVATE).getString(key, null)
    }

    fun setItem(file: String, key: String, value: String) {
        context.getSharedPreferences(file, Context.MODE_PRIVATE).edit {
            putString(key, value)
        }
    }

    companion object {
        const val WIDGETS_FILE = "widgets"
        const val WIDGETS_PURCHASED = "purchased"
    }
}