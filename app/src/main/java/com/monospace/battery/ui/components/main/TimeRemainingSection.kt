package com.monospace.battery.ui.components.main

import androidx.compose.runtime.Composable
import com.monospace.battery.R
import com.monospace.battery.core.constants.Constants
import com.monospace.battery.data.models.LocalBatteryState
import com.monospace.battery.ui.components.LargeHorizontalInfoCard

@Composable
fun TimeRemainingSection() {
    val state = LocalBatteryState.current
    val onShowDialog = LocalOnShowDialog.current

    if (state.timeRemaining != Constants.ZERO_TIME) {
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
