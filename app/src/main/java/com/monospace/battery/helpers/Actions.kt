package com.monospace.battery.helpers

import android.content.Intent

object Actions {
    val actions = arrayOf(
        Intent.ACTION_BATTERY_CHANGED,
        Intent.ACTION_PACKAGE_REPLACED
    )
}