package com.monospace.battery.ui.components.main

import androidx.compose.runtime.Composable
import com.monospace.battery.R
import com.monospace.battery.data.models.LocalBatteryState
import com.monospace.battery.ui.components.HealthCard
import com.monospace.battery.ui.components.getHealthDescriptionRes
import com.monospace.battery.ui.components.getHealthIcon
import com.monospace.battery.ui.components.getHealthStatusRes

@Composable
fun HealthSection() {
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
