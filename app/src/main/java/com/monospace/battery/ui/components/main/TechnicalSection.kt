package com.monospace.battery.ui.components.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.monospace.battery.R
import com.monospace.battery.data.models.LocalBatteryState
import com.monospace.battery.ui.components.SmallInfoCard
import com.monospace.battery.ui.components.formatResourceOrDash

@Composable
fun TechnicalSection() {
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

        val voltageValue = formatResourceOrDash(state.voltage, R.string.voltage_mv, state.voltage)
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
