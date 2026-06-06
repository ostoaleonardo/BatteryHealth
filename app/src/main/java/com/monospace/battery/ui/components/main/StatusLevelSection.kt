package com.monospace.battery.ui.components.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import com.monospace.battery.ui.components.InfoCard
import com.monospace.battery.ui.components.LargeVerticalInfoCard
import com.monospace.battery.ui.components.getBatteryIcon
import com.monospace.battery.ui.components.getChargingSourceRes
import com.monospace.battery.ui.components.getChargingStatusRes

@Composable
fun StatusLevelSection() {
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
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            iconRes = levelIcon,
            iconTint = iconTint,
            onClick = { onShowDialog(levelData) }
        )
    }
}
