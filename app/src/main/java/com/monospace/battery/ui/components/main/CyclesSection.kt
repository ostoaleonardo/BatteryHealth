package com.monospace.battery.ui.components.main

import android.os.Build
import androidx.compose.runtime.Composable
import com.monospace.battery.R
import com.monospace.battery.data.models.LocalBatteryState
import com.monospace.battery.ui.components.common.card.LargeHorizontalCard
import com.monospace.battery.ui.utils.LocalIconTint
import com.monospace.battery.ui.utils.LocalOnShowDialog
import com.monospace.battery.ui.utils.rememberBatteryDialogData

@Composable
fun CyclesSection() {
    val state = LocalBatteryState.current
    val onShowDialog = LocalOnShowDialog.current

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        val cyclesData = rememberBatteryDialogData(
            titleRes = R.string.battery_charging_cycles,
            value = "${state.chargeCycles}",
            descriptionRes = R.string.description_cycles,
            iconRes = R.drawable.power
        )

        LargeHorizontalCard(
            title = cyclesData.title,
            value = cyclesData.value,
            iconRes = R.drawable.power,
            iconTint = LocalIconTint.current,
            onClick = { onShowDialog(cyclesData) }
        )
    }
}
