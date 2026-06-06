package com.monospace.battery.ui.screens

import android.content.res.Configuration
import android.os.BatteryManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.monospace.battery.data.models.BatteryState
import com.monospace.battery.data.models.LocalBatteryState
import com.monospace.battery.ui.components.BatteryDialog
import com.monospace.battery.ui.components.getHealthColor
import com.monospace.battery.ui.components.main.ChargingSection
import com.monospace.battery.ui.components.main.CyclesSection
import com.monospace.battery.ui.components.main.DialogData
import com.monospace.battery.ui.components.main.HealthSection
import com.monospace.battery.ui.components.main.LocalHealthColor
import com.monospace.battery.ui.components.main.LocalIconTint
import com.monospace.battery.ui.components.main.LocalOnShowDialog
import com.monospace.battery.ui.components.main.StatusLevelSection
import com.monospace.battery.ui.components.main.TechnicalSection
import com.monospace.battery.ui.components.main.TimeRemainingSection
import com.monospace.battery.ui.theme.BatteryTheme

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
