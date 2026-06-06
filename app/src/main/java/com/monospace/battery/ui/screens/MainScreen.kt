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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.monospace.battery.R
import com.monospace.battery.core.constants.Constants
import com.monospace.battery.core.utils.BatteryStrings
import com.monospace.battery.data.models.BatteryState
import com.monospace.battery.data.models.LocalBatteryState
import com.monospace.battery.ui.components.BatteryDialog
import com.monospace.battery.ui.components.HealthCard
import com.monospace.battery.ui.components.InfoCard
import com.monospace.battery.ui.components.LargeHorizontalInfoCard
import com.monospace.battery.ui.components.LargeVerticalInfoCard
import com.monospace.battery.ui.components.SmallInfoCard
import com.monospace.battery.ui.components.formatResourceOrDash
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

val LocalIconTint = staticCompositionLocalOf<Color> { error("No icon tint provided") }
val LocalHealthColor = staticCompositionLocalOf<Color> { error("No health color provided") }
val LocalOnShowDialog = staticCompositionLocalOf<(DialogData) -> Unit> { {} }

@Composable
fun MainScreen() {
    val state = LocalBatteryState.current

    val healthColor = getHealthColor(state.health)

    val iconTint = remember(state.isCharging, healthColor) {
        if (state.isCharging) healthColor else Color.Unspecified
    }.let {
        if (it == Color.Unspecified) MaterialTheme.colorScheme.onSurfaceVariant else it
    }

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

    CompositionLocalProvider(
        LocalIconTint provides iconTint,
        LocalHealthColor provides healthColor,
        LocalOnShowDialog provides { activeDialogData.value = it }
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 8.dp, bottom = 16.dp)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HealthSection()
                StatusLevelSection()
                CyclesSection()
                TechnicalSection()
                ChargingSection()
                TimeRemainingSection()
            }
        }
    }
}

@Composable
private fun HealthSection() {
    val state = LocalBatteryState.current
    val healthColor = LocalHealthColor.current
    val onShowDialog = LocalOnShowDialog.current

    val dialogData = rememberBatteryDialogData(
        titleRes = R.string.battery_health,
        value = getHealthStatusRes(state.health),
        descriptionRes = getHealthDescriptionRes(state.health),
        iconRes = getHealthIcon(state.health),
        iconTint = healthColor
    )

    HealthCard(
        health = state.health,
        onClick = { onShowDialog(dialogData) }
    )
}

@Composable
private fun StatusLevelSection() {
    val state = LocalBatteryState.current
    val iconTint = LocalIconTint.current
    val onShowDialog = LocalOnShowDialog.current

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
            val statusData = rememberBatteryDialogData(
                titleRes = R.string.battery_status,
                value = getChargingStatusRes(state.isCharging),
                descriptionRes = R.string.description_status,
                iconRes = R.drawable.bolt
            )

            InfoCard(
                title = statusData.title,
                value = statusData.value,
                iconRes = R.drawable.bolt,
                iconTint = iconTint,
                onClick = { onShowDialog(statusData) }
            )

            val sourceData = rememberBatteryDialogData(
                titleRes = R.string.charging_source,
                value = getChargingSourceRes(state.chargeSource),
                descriptionRes = R.string.description_source,
                iconRes = R.drawable.cable
            )

            InfoCard(
                title = sourceData.title,
                value = sourceData.value,
                iconRes = R.drawable.cable,
                iconTint = iconTint,
                onClick = { onShowDialog(sourceData) }
            )
        }

        val levelValue = stringResource(R.string.battery_percentage, state.level)
        val levelIcon = getBatteryIcon(state.level, state.isCharging)
        val levelData = rememberBatteryDialogData(
            titleRes = R.string.battery_level,
            value = levelValue,
            descriptionRes = R.string.description_level,
            iconRes = levelIcon
        )

        LargeVerticalInfoCard(
            title = levelData.title,
            value = levelValue,
            iconRes = levelIcon,
            iconTint = iconTint,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            onClick = { onShowDialog(levelData) }
        )
    }
}

@Composable
private fun CyclesSection() {
    val state = LocalBatteryState.current
    val onShowDialog = LocalOnShowDialog.current

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        val cyclesData = rememberBatteryDialogData(
            titleRes = R.string.battery_charging_cycles,
            value = "${state.chargeCycles}",
            descriptionRes = R.string.description_cycles,
            iconRes = R.drawable.power
        )

        LargeHorizontalInfoCard(
            title = cyclesData.title,
            value = cyclesData.value,
            iconRes = R.drawable.power,
            iconTint = LocalIconTint.current,
            onClick = { onShowDialog(cyclesData) }
        )
    }
}

@Composable
private fun TechnicalSection() {
    val state = LocalBatteryState.current
    val iconTint = LocalIconTint.current
    val onShowDialog = LocalOnShowDialog.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val typeValue = state.technology.orEmpty().ifBlank {
            stringResource(R.string.battery_health_unknown)
        }
        val typeData = rememberBatteryDialogData(
            R.string.battery_type,
            typeValue,
            R.string.description_type,
            R.drawable.battery_10
        )

        SmallInfoCard(
            title = typeData.title,
            value = typeValue,
            iconRes = R.drawable.battery_10,
            iconTint = iconTint,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            onClick = { onShowDialog(typeData) }
        )

        val tempValue = formatResourceOrDash(
            state.temperature,
            R.string.temperature_celsius,
            state.temperature / 10
        )
        val tempData = rememberBatteryDialogData(
            R.string.battery_temperature,
            tempValue,
            R.string.description_temperature,
            R.drawable.thermometer
        )

        SmallInfoCard(
            title = tempData.title,
            value = tempValue,
            iconRes = R.drawable.thermometer,
            iconTint = iconTint,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            onClick = { onShowDialog(tempData) }
        )

        val voltageValue = formatResourceOrDash(
            state.voltage,
            R.string.voltage_mv,
            state.voltage
        )
        val voltageData = rememberBatteryDialogData(
            R.string.battery_voltage,
            voltageValue,
            R.string.description_voltage,
            R.drawable.bolt
        )

        SmallInfoCard(
            title = voltageData.title,
            value = voltageValue,
            iconRes = R.drawable.bolt,
            iconTint = iconTint,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            onClick = { onShowDialog(voltageData) }
        )
    }
}

@Composable
private fun ChargingSection() {
    val state = LocalBatteryState.current
    val iconTint = LocalIconTint.current
    val onShowDialog = LocalOnShowDialog.current

    val capacityValue = remember(state.capacity, state.capacityRemaining) {
        BatteryStrings().getCapacityValue(state.capacity, state.capacityRemaining)
    }

    val currentData = rememberBatteryDialogData(
        titleRes = R.string.battery_current,
        value = stringResource(R.string.current_ma, state.currentNow),
        descriptionRes = R.string.description_current,
        iconRes = R.drawable.bolt
    )

    val capData = capacityValue?.let {
        rememberBatteryDialogData(
            R.string.battery_capacity,
            it,
            R.string.description_capacity,
            R.drawable.battery_full
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (state.chargeSpeed > 0) {
            val qualityRes = remember(state.chargeSpeed) {
                BatteryStrings().getChargerQuality(state.chargeSpeed)
            }
            val isGood = remember(qualityRes) {
                qualityRes == R.string.charger_quality_fast || qualityRes == R.string.charger_quality_normal
            }
            val speedWatts = stringResource(R.string.charge_speed_watts, state.chargeSpeed)
            val qualityIcon = if (isGood) R.drawable.arrow_drop_up else R.drawable.arrow_drop_down
            val qualityColor = if (isGood) Constants.ColorGreen else Constants.ColorRed
            val qualityText = stringResource(qualityRes)

            val speedData = rememberBatteryDialogData(
                titleRes = R.string.battery_speed,
                value = "$speedWatts ($qualityText)",
                descriptionRes = R.string.description_speed,
                iconRes = R.drawable.rocket
            )

            LargeVerticalInfoCard(
                title = speedData.title,
                value = speedWatts,
                valueIconRes = qualityIcon,
                valueIconTint = qualityColor,
                iconRes = R.drawable.rocket,
                iconTint = iconTint,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                onClick = { onShowDialog(speedData) }
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoCard(
                    title = currentData.title,
                    value = currentData.value,
                    iconRes = R.drawable.bolt,
                    iconTint = iconTint,
                    onClick = { onShowDialog(currentData) }
                )
                capData?.let {
                    InfoCard(
                        title = it.title,
                        value = it.value,
                        iconRes = R.drawable.battery_full,
                        iconTint = iconTint,
                        onClick = { onShowDialog(it) }
                    )
                }
            }
        } else {
            SmallInfoCard(
                title = currentData.title,
                value = currentData.value,
                iconRes = R.drawable.bolt,
                iconTint = iconTint,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                onClick = { onShowDialog(currentData) }
            )

            capData?.let {
                SmallInfoCard(
                    title = it.title,
                    value = it.value,
                    iconRes = R.drawable.battery_full,
                    iconTint = iconTint,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    onClick = { onShowDialog(it) }
                )
            }
        }
    }
}

@Composable
private fun TimeRemainingSection() {
    val state = LocalBatteryState.current
    val onShowDialog = LocalOnShowDialog.current

    if (state.timeRemaining != "00:00") {
        val timeData = rememberBatteryDialogData(
            titleRes = R.string.battery_charge_time_remaining,
            value = state.timeRemaining,
            descriptionRes = R.string.description_time,
            iconRes = R.drawable.schedule
        )

        LargeHorizontalInfoCard(
            title = timeData.title,
            value = state.timeRemaining,
            iconRes = R.drawable.schedule,
            iconTint = LocalIconTint.current,
            onClick = { onShowDialog(timeData) }
        )
    }
}

@Composable
private fun rememberBatteryDialogData(
    titleRes: Int,
    value: Any,
    descriptionRes: Int,
    iconRes: Int,
    iconTint: Color = LocalIconTint.current
): DialogData {
    val title = stringResource(titleRes)
    val description = stringResource(descriptionRes)

    val valueText = when (value) {
        is Int -> stringResource(value)
        else -> value.toString()
    }

    return remember(title, valueText, description, iconRes, iconTint) {
        DialogData(title, valueText, description, iconRes, iconTint)
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MainScreenLightPreview() {
    BatteryTheme {
        CompositionLocalProvider(
            LocalBatteryState provides BatteryState(
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
        ) {
            MainScreen()
        }
    }
}
