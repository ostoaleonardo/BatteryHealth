package com.monospace.battery.ui.components.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.monospace.battery.R
import com.monospace.battery.core.constants.Constants
import com.monospace.battery.core.utils.BatteryStrings
import com.monospace.battery.data.models.LocalBatteryState
import com.monospace.battery.ui.components.InfoCard
import com.monospace.battery.ui.components.LargeVerticalInfoCard
import com.monospace.battery.ui.components.SmallInfoCard

@Composable
fun ChargingSection() {
    val state = LocalBatteryState.current
    val iconTint = LocalIconTint.current
    val onShowDialog = LocalOnShowDialog.current

    val cardCapacity = remember(state.capacity) {
        if (state.capacity > 0) "${state.capacity} mAh" else null
    }

    val modalCapacity = remember(state.capacity, state.capacityRemaining) {
        BatteryStrings().getCapacityValue(state.capacity, state.capacityRemaining)
    }

    val currentData = rememberBatteryDialogData(
        titleRes = R.string.battery_current,
        value = stringResource(R.string.current_ma, state.currentNow),
        descriptionRes = R.string.description_current,
        iconRes = R.drawable.bolt
    )

    val capData = modalCapacity?.let {
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
            val speedWatts = stringResource(R.string.charge_speed_watts, state.chargeSpeed)
            val qualityRes = remember(state.chargeSpeed) {
                BatteryStrings().getChargerQuality(state.chargeSpeed)
            }
            val isGood = remember(qualityRes) {
                qualityRes == R.string.charger_quality_fast || qualityRes == R.string.charger_quality_normal
            }
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
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                valueIconRes = qualityIcon,
                valueIconTint = qualityColor,
                onClick = { onShowDialog(speedData) },
                iconContent = {
                    ChargingSpeedChart(
                        speed = state.chargeSpeed,
                        tint = iconTint
                    )
                }
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
                        value = cardCapacity ?: it.value,
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
                    value = cardCapacity ?: it.value,
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
