package com.monospace.battery.ui.screens

import android.os.BatteryManager
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.monospace.battery.R
import com.monospace.battery.ui.components.*
import com.monospace.battery.ui.theme.BatteryTheme

@Composable
fun MainScreen(state: BatteryState) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HealthCard(health = state.health)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InfoCard(
                        title = stringResource(R.string.battery_status),
                        value = stringResource(getChargingStatusRes(state.isCharging)),
                        iconRes = R.drawable.bolt,
                        iconTint = if (state.isCharging) getHealthColor(BatteryManager.BATTERY_HEALTH_GOOD) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    InfoCard(
                        title = stringResource(R.string.charging_source),
                        value = stringResource(getChargingSourceRes(state.chargeSource)),
                        iconRes = R.drawable.cable,
                        iconTint = if (state.isCharging) getHealthColor(BatteryManager.BATTERY_HEALTH_GOOD) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                LargeVerticalInfoCard(
                    title = stringResource(R.string.battery_level),
                    value = stringResource(R.string.battery_percentage, state.level),
                    iconRes = getBatteryIcon(state.level, state.isCharging),
                    iconTint = if (state.isCharging) getHealthColor(BatteryManager.BATTERY_HEALTH_GOOD) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                LargeHorizontalInfoCard(
                    title = stringResource(R.string.battery_charging_cycles),
                    value = "${state.chargeCycles}",
                    iconRes = R.drawable.power,
                    iconTint = getHealthColor(BatteryManager.BATTERY_HEALTH_GOOD)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SmallInfoCard(
                    title = stringResource(R.string.battery_type),
                    value = state.technology ?: stringResource(R.string.battery_health_unknown),
                    iconRes = R.drawable.battery_10,
                    iconTint = getHealthColor(BatteryManager.BATTERY_HEALTH_GOOD),
                    modifier = Modifier.weight(1f)
                )
                SmallInfoCard(
                    title = stringResource(R.string.battery_temperature),
                    value = stringResource(R.string.temperature_celsius, state.temperature / 10),
                    iconRes = R.drawable.thermometer,
                    iconTint = getHealthColor(BatteryManager.BATTERY_HEALTH_GOOD),
                    modifier = Modifier.weight(1f)
                )
                SmallInfoCard(
                    title = stringResource(R.string.battery_voltage),
                    value = stringResource(R.string.voltage_mv, state.voltage),
                    iconRes = R.drawable.bolt,
                    iconTint = getHealthColor(BatteryManager.BATTERY_HEALTH_GOOD),
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (state.capacity > 0) {
                    SmallInfoCard(
                        title = stringResource(R.string.battery_capacity),
                        value = stringResource(R.string.capacity_mah, state.capacity),
                        iconRes = R.drawable.battery_full,
                        iconTint = getHealthColor(BatteryManager.BATTERY_HEALTH_GOOD),
                        modifier = Modifier.weight(1f)
                    )
                }
                if (state.chargeSpeed > 0) {
                    SmallInfoCard(
                        title = stringResource(R.string.battery_speed),
                        value = stringResource(R.string.charge_speed_watts, state.chargeSpeed),
                        iconRes = R.drawable.rocket,
                        iconTint = getHealthColor(BatteryManager.BATTERY_HEALTH_GOOD),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (state.timeRemaining != "00:00") {
                LargeHorizontalInfoCard(
                    title = stringResource(R.string.battery_charge_time_remaining),
                    value = state.timeRemaining,
                    iconRes = R.drawable.schedule,
                    iconTint = getHealthColor(BatteryManager.BATTERY_HEALTH_GOOD)
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
fun MainScreenPreview() {
    BatteryTheme {
        MainScreen(
            state = BatteryState(
                health = BatteryManager.BATTERY_HEALTH_GOOD,
                level = 85,
                isCharging = true,
                chargeSource = BatteryManager.BATTERY_PLUGGED_AC,
                chargeCycles = 120,
                technology = "Li-ion",
                temperature = 320,
                voltage = 4100,
                capacity = 5000,
                timeRemaining = "00:30",
                chargeSpeed = 18.5
            )
        )
    }
}

@Preview(showBackground = true, name = "Critical State")
@Composable
fun MainScreenCriticalPreview() {
    BatteryTheme {
        MainScreen(
            state = BatteryState(
                health = BatteryManager.BATTERY_HEALTH_OVERHEAT,
                level = 15,
                isCharging = false,
                technology = "Li-ion",
                temperature = 450,
                voltage = 3700,
                capacity = 5000
            )
        )
    }
}
