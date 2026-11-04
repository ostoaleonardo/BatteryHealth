package com.monospace.battery

import android.app.Application
import android.content.Context
import com.monospace.battery.core.constants.Constants

class BatteryApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        getSharedPreferences(Constants.PREFS_ALERTS, Context.MODE_PRIVATE)
        getSharedPreferences(Constants.PREFS_WIDGETS, Context.MODE_PRIVATE)
    }
}
