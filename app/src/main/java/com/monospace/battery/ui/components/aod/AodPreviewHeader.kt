package com.monospace.battery.ui.components.aod

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.monospace.battery.R
import com.monospace.battery.core.constants.Constants
import com.monospace.battery.data.models.LocalBatteryState
import com.monospace.battery.data.models.LocalSettingsActions
import com.monospace.battery.data.models.LocalSettingsState
import com.monospace.battery.ui.components.AodPreviewCard
import com.monospace.battery.ui.components.SegmentOption
import com.monospace.battery.ui.components.SegmentedControl
import com.monospace.battery.ui.screens.AlwaysOnDisplayActivity

@Composable
fun AodPreviewHeader() {
    val state = LocalSettingsState.current
    val actions = LocalSettingsActions.current
    val batteryState = LocalBatteryState.current
    val isPremium = state.isWidgetsPurchased
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val themePrimary = MaterialTheme.colorScheme.primary
        val previewColor = if (state.aodColor == 0L) themePrimary else Color(state.aodColor)

        // Smartphone Preview (Always visible)
        AodPreviewCard(
            level = batteryState.level,
            color = previewColor,
            meterStyle = state.aodMeterStyle,
            showShortcuts = state.aodShowShortcuts
        )

        // Activation Controls Row (Only for Premium)
        if (isPremium) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                val isAodEnabled = state.alwaysOnDisplayEnabled

                // Reusable Segmented Control for Activation
                SegmentedControl(
                    options = listOf(
                        SegmentOption(Constants.OFF, R.string.aod_disabled),
                        SegmentOption(Constants.ON, R.string.aod_enabled)
                    ),
                    selected = if (isAodEnabled) Constants.ON else Constants.OFF
                ) { id ->
                    actions.onAlwaysOnDisplayChange(id == Constants.ON)
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Manual Try Button
                Button(
                    onClick = {
                        val intent = Intent(
                            context, AlwaysOnDisplayActivity::class.java
                        ).apply {
                            putExtra(Constants.EXTRA_AOD_TEST, true)
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.size(40.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = stringResource(R.string.aod_try_now),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
