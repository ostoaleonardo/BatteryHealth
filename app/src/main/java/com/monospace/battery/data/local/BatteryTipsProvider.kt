package com.monospace.battery.data.local

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PhonelinkSetup
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Snowboarding
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Wifi
import com.monospace.battery.R
import com.monospace.battery.data.models.BatteryTip
import kotlin.random.Random

object BatteryTipsProvider {
    private val allTips = listOf(
        BatteryTip(
            titleRes = R.string.tips_20_80_rule,
            descriptionRes = R.string.tips_20_80_rule_desc,
            iconVector = Icons.Default.Info
        ),
        BatteryTip(
            titleRes = R.string.tips_heat_kill,
            descriptionRes = R.string.tips_heat_kill_desc,
            iconVector = Icons.Default.DeviceThermostat
        ),
        BatteryTip(
            titleRes = R.string.tips_original_acc,
            descriptionRes = R.string.tips_original_acc_desc,
            iconVector = Icons.Default.Cable
        ),
        BatteryTip(
            titleRes = R.string.tips_dark_mode,
            descriptionRes = R.string.tips_dark_mode_desc,
            iconVector = Icons.Default.DarkMode
        ),
        BatteryTip(
            titleRes = R.string.tips_brightness,
            descriptionRes = R.string.tips_brightness_desc,
            iconVector = Icons.Default.Brightness6
        ),
        BatteryTip(
            titleRes = R.string.tips_fast_charge,
            descriptionRes = R.string.tips_fast_charge_desc,
            iconVector = Icons.Default.RocketLaunch
        ),
        BatteryTip(
            titleRes = R.string.tips_background_apps,
            descriptionRes = R.string.tips_background_apps_desc,
            iconVector = Icons.Default.Sync
        ),
        BatteryTip(
            titleRes = R.string.tips_avoid_zero,
            descriptionRes = R.string.tips_avoid_zero_desc,
            iconVector = Icons.Default.BatteryAlert
        ),
        BatteryTip(
            titleRes = R.string.tips_cold_care,
            descriptionRes = R.string.tips_cold_care_desc,
            iconVector = Icons.Default.Snowboarding
        ),
        BatteryTip(
            titleRes = R.string.tips_software_update,
            descriptionRes = R.string.tips_software_update_desc,
            iconVector = Icons.Default.Smartphone
        ),
        BatteryTip(
            titleRes = R.string.tips_location,
            descriptionRes = R.string.tips_location_desc,
            iconVector = Icons.Default.GpsFixed
        ),
        BatteryTip(
            titleRes = R.string.tips_signal,
            descriptionRes = R.string.tips_signal_desc,
            iconVector = Icons.Default.AirplanemodeActive
        ),
        BatteryTip(
            titleRes = R.string.tips_wifi,
            descriptionRes = R.string.tips_wifi_desc,
            iconVector = Icons.Default.Wifi
        ),
        BatteryTip(
            titleRes = R.string.tips_vibration,
            descriptionRes = R.string.tips_vibration_desc,
            iconVector = Icons.Default.Vibration
        ),
        BatteryTip(
            titleRes = R.string.tips_hot_storage,
            descriptionRes = R.string.tips_hot_storage_desc,
            iconVector = Icons.Default.DeviceThermostat
        ),
        BatteryTip(
            titleRes = R.string.tips_heavy_apps,
            descriptionRes = R.string.tips_heavy_apps_desc,
            iconVector = Icons.Default.FlashOn
        ),
        BatteryTip(
            titleRes = R.string.tips_push,
            descriptionRes = R.string.tips_push_desc,
            iconVector = Icons.Default.NotificationsActive
        ),
        BatteryTip(
            titleRes = R.string.tips_refresh_rate,
            descriptionRes = R.string.tips_refresh_rate_desc,
            iconVector = Icons.Default.PhonelinkSetup
        ),
        BatteryTip(
            titleRes = R.string.tips_charging_habits,
            descriptionRes = R.string.tips_charging_habits_desc,
            iconVector = Icons.Default.Smartphone
        ),
        BatteryTip(
            titleRes = R.string.tips_sync_freq,
            descriptionRes = R.string.tips_sync_freq_desc,
            iconVector = Icons.Default.Sync
        )
    )

    fun getRandomTip(currentIndex: Int = -1): Pair<Int, BatteryTip> {
        var nextIndex: Int

        do {
            nextIndex = Random.nextInt(allTips.size)
        } while (nextIndex == currentIndex && allTips.size > 1)

        return nextIndex to allTips[nextIndex]
    }

    fun getInitialTip(): Pair<Int, BatteryTip> {
        val index = Random.nextInt(allTips.size)
        return index to allTips[index]
    }
}
