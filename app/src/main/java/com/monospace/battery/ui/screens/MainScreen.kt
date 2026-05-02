package com.monospace.battery.ui.screens

import android.content.res.Configuration
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.monospace.battery.R
import com.monospace.battery.core.utils.BatteryStrings
import com.monospace.battery.data.models.BatteryState
import com.monospace.battery.ui.components.BatteryDialog
import com.monospace.battery.ui.components.HealthCard
import com.monospace.battery.ui.components.InfoCard
import com.monospace.battery.ui.components.LargeHorizontalInfoCard
import com.monospace.battery.ui.components.LargeVerticalInfoCard
import com.monospace.battery.ui.components.SmallInfoCard
import com.monospace.battery.ui.components.getBatteryIcon
import com.monospace.battery.ui.components.getChargingSourceRes
import com.monospace.battery.ui.components.getChargingStatusRes
import com.monospace.battery.ui.components.getHealthColor
import com.monospace.battery.ui.components.getHealthDescriptionRes
import com.monospace.battery.ui.components.getHealthIcon
import com.monospace.battery.ui.components.getHealthStatusRes
import com.monospace.battery.ui.theme.BatteryTheme

data class DialogData(
    val title: String,
    val value: String,
    val description: String,
    val iconRes: Int,
    val iconTint: Color
)

@Composable
fun MainScreen(state: BatteryState) {
    val healthColor = getHealthColor(state.health)
    val iconTint = if (state.isCharging) healthColor else MaterialTheme.colorScheme.onSurfaceVariant

    val activeDialogData = remember { mutableStateOf<DialogData?>(null) }

    activeDialogData.value?.let { data ->
        BatteryDialog(
            onDismissRequest = { activeDialogData.value = null },
            title = data.title,
            value = data.value,
            description = data.description,
            iconRes = data.iconRes,
            iconTint = data.iconTint
        )
    }

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
            HealthSection(state, healthColor) { activeDialogData.value = it }
            StatusLevelSection(state, iconTint) { activeDialogData.value = it }
            CyclesSection(state, iconTint) { activeDialogData.value = it }
            TechnicalSection(state, iconTint) { activeDialogData.value = it }
            ChargingSection(state, iconTint) { activeDialogData.value = it }
            TimeRemainingSection(state, iconTint) { activeDialogData.value = it }
        }
    }
}

@Composable
private fun HealthSection(
    state: BatteryState,
    healthColor: Color,
    onShowDialog: (DialogData) -> Unit
) {
    val title = stringResource(R.string.battery_health)
    val value = stringResource(getHealthStatusRes(state.health))
    val description = stringResource(getHealthDescriptionRes(state.health))
    val icon = getHealthIcon(state.health)

    HealthCard(
        health = state.health,
        onClick = {
            onShowDialog(
                DialogData(
                    title,
                    value,
                    description,
                    icon,
                    healthColor
                )
            )
        }
    )
}

@Composable
private fun StatusLevelSection(
    state: BatteryState,
    iconTint: Color,
    onShowDialog: (DialogData) -> Unit
) {
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
            val statusTitle = stringResource(R.string.battery_status)
            val statusValue = stringResource(getChargingStatusRes(state.isCharging))
            val statusDesc = stringResource(R.string.description_status)

            InfoCard(
                title = statusTitle,
                value = statusValue,
                iconRes = R.drawable.bolt,
                iconTint = iconTint,
                onClick = {
                    onShowDialog(
                        DialogData(
                            statusTitle,
                            statusValue,
                            statusDesc,
                            R.drawable.bolt,
                            iconTint
                        )
                    )
                }
            )

            val sourceTitle = stringResource(R.string.charging_source)
            val sourceValue = stringResource(getChargingSourceRes(state.chargeSource))
            val sourceDesc = stringResource(R.string.description_source)

            InfoCard(
                title = sourceTitle,
                value = sourceValue,
                iconRes = R.drawable.cable,
                iconTint = iconTint,
                onClick = {
                    onShowDialog(
                        DialogData(
                            sourceTitle,
                            sourceValue,
                            sourceDesc,
                            R.drawable.cable,
                            iconTint
                        )
                    )
                }
            )
        }

        val levelTitle = stringResource(R.string.battery_level)
        val levelValue = stringResource(R.string.battery_percentage, state.level)
        val levelDesc = stringResource(R.string.description_level)
        val levelIcon = getBatteryIcon(state.level, state.isCharging)

        LargeVerticalInfoCard(
            title = levelTitle,
            value = levelValue,
            iconRes = levelIcon,
            iconTint = iconTint,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            onClick = {
                onShowDialog(
                    DialogData(
                        levelTitle,
                        levelValue,
                        levelDesc,
                        levelIcon,
                        iconTint
                    )
                )
            }
        )
    }
}

@Composable
private fun CyclesSection(
    state: BatteryState,
    iconTint: Color,
    onShowDialog: (DialogData) -> Unit
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        val cyclesTitle = stringResource(R.string.battery_charging_cycles)
        val cyclesValue = "${state.chargeCycles}"
        val cyclesDesc = stringResource(R.string.description_cycles)

        LargeHorizontalInfoCard(
            title = cyclesTitle,
            value = cyclesValue,
            iconRes = R.drawable.power,
            iconTint = iconTint,
            onClick = {
                onShowDialog(
                    DialogData(
                        cyclesTitle,
                        cyclesValue,
                        cyclesDesc,
                        R.drawable.power,
                        iconTint
                    )
                )
            }
        )
    }
}

@Composable
private fun TechnicalSection(
    state: BatteryState,
    iconTint: Color,
    onShowDialog: (DialogData) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val typeTitle = stringResource(R.string.battery_type)
        val typeValue =
            state.technology.orEmpty().ifBlank { stringResource(R.string.battery_health_unknown) }
        val typeDesc = stringResource(R.string.description_type)

        SmallInfoCard(
            title = typeTitle,
            value = typeValue,
            iconRes = R.drawable.battery_10,
            iconTint = iconTint,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            onClick = {
                onShowDialog(
                    DialogData(
                        typeTitle,
                        typeValue,
                        typeDesc,
                        R.drawable.battery_10,
                        iconTint
                    )
                )
            }
        )

        val tempTitle = stringResource(R.string.battery_temperature)
        val tempValue = stringResource(R.string.temperature_celsius, state.temperature / 10)
        val tempDesc = stringResource(R.string.description_temperature)

        SmallInfoCard(
            title = tempTitle,
            value = tempValue,
            iconRes = R.drawable.thermometer,
            iconTint = iconTint,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            onClick = {
                onShowDialog(
                    DialogData(
                        tempTitle,
                        tempValue,
                        tempDesc,
                        R.drawable.thermometer,
                        iconTint
                    )
                )
            }
        )

        val voltageTitle = stringResource(R.string.battery_voltage)
        val voltageValue = stringResource(R.string.voltage_mv, state.voltage)
        val voltageDesc = stringResource(R.string.description_voltage)

        SmallInfoCard(
            title = voltageTitle,
            value = voltageValue,
            iconRes = R.drawable.bolt,
            iconTint = iconTint,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            onClick = {
                onShowDialog(
                    DialogData(
                        voltageTitle,
                        voltageValue,
                        voltageDesc,
                        R.drawable.bolt,
                        iconTint
                    )
                )
            }
        )
    }
}

@Composable
private fun ChargingSection(
    state: BatteryState,
    iconTint: Color,
    onShowDialog: (DialogData) -> Unit
) {
    val capacityValue = BatteryStrings().getCapacityValue(state.capacity, state.capacityRemaining)

    val currentTitle = stringResource(R.string.battery_current)
    val currentValue = stringResource(R.string.current_ma, state.currentNow)
    val currentDesc = stringResource(R.string.description_current)
    val currentData = DialogData(currentTitle, currentValue, currentDesc, R.drawable.bolt, iconTint)

    val capTitle = stringResource(R.string.battery_capacity)
    val capDesc = stringResource(R.string.description_capacity)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (state.chargeSpeed > 0) {
            val speedTitle = stringResource(R.string.battery_speed)
            val speedWatts = stringResource(R.string.charge_speed_watts, state.chargeSpeed)
            val qualitySpeed = stringResource(BatteryStrings().getChargerQuality(state.chargeSpeed))
            val speedDesc = stringResource(R.string.description_speed)
            val speedValue = "$speedWatts (${qualitySpeed})"

            LargeVerticalInfoCard(
                title = speedTitle,
                value = speedValue,
                iconRes = R.drawable.rocket,
                iconTint = iconTint,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                onClick = {
                    onShowDialog(
                        DialogData(
                            speedTitle,
                            speedValue,
                            speedDesc,
                            R.drawable.rocket,
                            iconTint
                        )
                    )
                }
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoCard(
                    title = currentTitle,
                    value = currentValue,
                    iconRes = R.drawable.bolt,
                    iconTint = iconTint,
                    onClick = { onShowDialog(currentData) }
                )
                capacityValue?.let {
                    InfoCard(
                        title = capTitle,
                        value = it,
                        iconRes = R.drawable.battery_full,
                        iconTint = iconTint,
                        onClick = {
                            onShowDialog(
                                DialogData(
                                    capTitle,
                                    it,
                                    capDesc,
                                    R.drawable.battery_full,
                                    iconTint
                                )
                            )
                        }
                    )
                }
            }
        } else {
            SmallInfoCard(
                title = currentTitle,
                value = currentValue,
                iconRes = R.drawable.bolt,
                iconTint = iconTint,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                onClick = { onShowDialog(currentData) }
            )

            capacityValue?.let {
                SmallInfoCard(
                    title = capTitle,
                    value = it,
                    iconRes = R.drawable.battery_full,
                    iconTint = iconTint,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    onClick = {
                        onShowDialog(
                            DialogData(
                                capTitle,
                                it,
                                capDesc,
                                R.drawable.battery_full,
                                iconTint
                            )
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun TimeRemainingSection(
    state: BatteryState,
    iconTint: Color,
    onShowDialog: (DialogData) -> Unit
) {
    if (state.timeRemaining != "00:00") {
        val timeTitle = stringResource(R.string.battery_charge_time_remaining)
        val timeDesc = stringResource(R.string.description_time)

        LargeHorizontalInfoCard(
            title = timeTitle,
            value = state.timeRemaining,
            iconRes = R.drawable.schedule,
            iconTint = iconTint,
            onClick = {
                onShowDialog(
                    DialogData(
                        timeTitle,
                        state.timeRemaining,
                        timeDesc,
                        R.drawable.schedule,
                        iconTint
                    )
                )
            }
        )
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Composable
fun MainScreenLightPreview() {
    BatteryTheme {
        MainScreen(
            state = BatteryState(
                health = BatteryManager.BATTERY_HEALTH_GOOD,
                level = 85,
                isCharging = false,
                chargeCycles = 120,
                technology = "Li-ion",
                temperature = 320,
                voltage = 4100,
                capacity = 5000,
                capacityRemaining = 4250,
                currentNow = -250
            )
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
fun MainScreenDarkPreview() {
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
                capacityRemaining = 4900,
                currentNow = 3500,
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
                capacity = 5000,
                capacityRemaining = 750,
                currentNow = -1200
            )
        )
    }
}
