package com.monospace.battery.core.constants

object AppConstants {

    // Notification Channels
    const val NOTIFICATION_CHANNEL_ID = "battery_alerts_channel"

    // Notification IDs
    const val NOTIFICATION_HEALTHY_CHARGE_ID = 1001
    const val NOTIFICATION_LOW_BATTERY_ID = 1002
    const val NOTIFICATION_TEMP_ALERT_ID = 1003
    const val NOTIFICATION_SERVICE_ID = 1

    // Preference Files
    const val PREFS_ALERTS = "battery_alerts_prefs"
    const val PREFS_WIDGETS = "widgets_purchase"

    // Preference Keys - Alerts
    const val KEY_HEALTHY_CHARGE_ENABLED = "healthy_charge_enabled"
    const val KEY_HEALTHY_CHARGE_LEVEL = "healthy_charge_level"
    const val KEY_LOW_BATTERY_ENABLED = "low_battery_enabled"
    const val KEY_LOW_BATTERY_LEVEL = "low_battery_level"
    const val KEY_TEMP_ALERT_ENABLED = "temp_alert_enabled"

    // Preference Keys - Widgets
    const val KEY_WIDGETS_PURCHASE_TOKEN = "purchase_token"
    const val KEY_WIDGETS_LAST_CHECK = "last_check_timestamp"

    // App Info
    const val DEFAULT_VERSION_NAME = "1.0.0"
    const val PLAY_STORE_MARKET_URL = "market://details?id="
    const val PLAY_STORE_WEB_URL = "https://play.google.com/store/apps/details?id="
}
