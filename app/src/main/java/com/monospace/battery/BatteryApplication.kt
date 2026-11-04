package com.monospace.battery

import android.app.Application
import com.monospace.battery.core.constants.Constants

class BatteryApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        getSharedPreferences(Constants.PREFS_ALERTS, MODE_PRIVATE)
        getSharedPreferences(Constants.PREFS_WIDGETS, MODE_PRIVATE)
    }
}
