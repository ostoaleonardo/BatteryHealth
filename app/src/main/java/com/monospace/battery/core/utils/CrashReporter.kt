package com.monospace.battery.core.utils

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics

object CrashReporter {

    fun log(message: String) {
        runCatching { FirebaseCrashlytics.getInstance().log(message) }
    }

    fun key(name: String, value: Any) {
        runCatching {
            val crashlytics = FirebaseCrashlytics.getInstance()
            when (value) {
                is Boolean -> crashlytics.setCustomKey(name, value)
                is Int -> crashlytics.setCustomKey(name, value)
                is Long -> crashlytics.setCustomKey(name, value)
                is Float -> crashlytics.setCustomKey(name, value)
                is Double -> crashlytics.setCustomKey(name, value)
                else -> crashlytics.setCustomKey(name, value.toString())
            }
        }
    }

    fun record(tag: String, message: String, error: Throwable, vararg keys: Pair<String, Any>) {
        Log.e(tag, message, error)
        keys.forEach { (name, value) -> key(name, value) }
        log("$tag: $message")
        runCatching { FirebaseCrashlytics.getInstance().recordException(error) }
    }
}
