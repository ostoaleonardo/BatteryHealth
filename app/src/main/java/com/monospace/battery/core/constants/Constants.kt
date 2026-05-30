package com.monospace.battery.core.constants

import androidx.compose.ui.graphics.Color

object Constants {

    // Notification Channels
    const val NOTIFICATION_CHANNEL_ID = "battery_alerts_channel"

    // Notification IDs
    const val NOTIFICATION_HEALTHY_CHARGE_ID = 1001
    const val NOTIFICATION_LOW_BATTERY_ID = 1002
    const val NOTIFICATION_TEMP_ALERT_ID = 1003
    const val NOTIFICATION_FAST_DISCHARGE_ID = 1004
    const val NOTIFICATION_SLOW_CHARGE_ID = 1005
    const val NOTIFICATION_SERVICE_ID = 1

    // Database
    const val DATABASE_NAME = "battery_database"

    // Preference Files
    const val PREFS_ALERTS = "battery_alerts_prefs"
    const val PREFS_WIDGETS = "widgets_purchase"

    // Preference Keys - Alerts
    const val KEY_HEALTHY_CHARGE_ENABLED = "healthy_charge_enabled"
    const val KEY_HEALTHY_CHARGE_LEVEL = "healthy_charge_level"
    const val KEY_LOW_BATTERY_ENABLED = "low_battery_enabled"
    const val KEY_LOW_BATTERY_LEVEL = "low_battery_level"
    const val KEY_TEMP_ALERT_ENABLED = "temp_alert_enabled"
    const val KEY_FAST_DISCHARGE_ENABLED = "fast_discharge_enabled"
    const val KEY_SLOW_CHARGE_ENABLED = "slow_charge_enabled"
    const val KEY_AOD_ENABLED = "aod_enabled"
    const val EXTRA_AOD_TEST = "extra_aod_test"
    const val KEY_AOD_CLOCK_STYLE = "aod_clock_style"
    const val KEY_AOD_METER_STYLE = "aod_meter_style"
    const val KEY_AOD_COLOR = "aod_color"
    const val KEY_AOD_SHOW_DATE = "aod_show_date"
    const val KEY_AOD_24H_FORMAT = "aod_24h_format"
    const val KEY_AOD_FONT_SIZE_CLOCK = "aod_font_size_clock"
    const val KEY_AOD_FONT_SIZE_DATE = "aod_font_size_date"
    const val KEY_AOD_SHOW_CLOCK = "aod_show_clock"
    const val KEY_AOD_DIM_AMOUNT = "aod_dim_amount"
    const val KEY_AOD_SHOW_SHORTCUTS = "aod_show_shortcuts"
    const val KEY_ACTIVE_MONITORING = "active_monitoring"

    // Navigation Routes
    const val ROUTE_HOME = "home"
    const val ROUTE_HISTORY = "history"
    const val ROUTE_ALERTS = "alerts"
    const val ROUTE_AOD = "aod"
    const val ROUTE_SETTINGS = "settings"

    // General
    const val OFF = "off"
    const val ON = "on"

    // Preference Keys - Widgets
    const val KEY_WIDGETS_PURCHASE_TOKEN = "purchase_token"
    const val KEY_WIDGETS_LAST_CHECK = "last_check_timestamp"

    // App Info
    const val DEFAULT_VERSION_NAME = "1.0.0"
    const val PLAY_STORE_MARKET_URL = "market://details?id="
    const val PLAY_STORE_WEB_URL = "https://play.google.com/store/apps/details?id="

    const val EMPTY_VALUE_DASH = "--"

    // Colors
    val ColorGreen = Color(0xFF00A25B)
    val ColorLightGreen = Color(0xFF8BC34A)
    val ColorOrange = Color(0xFFFF9800)
    val ColorDeepOrange = Color(0xFFFF5722)
    val ColorRed = Color(0xFFF44336)
    val ColorGray = Color(0xFF9E9E9E)
}
