package com.monospace.battery.ui.components.aod

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.monospace.battery.R
import com.monospace.battery.core.constants.Constants
import com.monospace.battery.data.models.LocalBatteryState
import com.monospace.battery.data.models.LocalSettingsActions
import com.monospace.battery.data.models.LocalSettingsState
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
        // Smartphone Preview (Always visible)
        AodPreviewCard(
            modifier = Modifier.fillMaxWidth().height(280.dp),
            level = batteryState.level
        )

        // Activation Controls Row (Only for Premium)
        if (isPremium) {
            val height = 40.dp
            val iconSize = ButtonDefaults.iconSizeFor(height)
            val playIconSize = iconSize * 1.9f

            CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(
                        ButtonGroupDefaults.ConnectedSpaceBetween,
                        Alignment.CenterHorizontally
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ToggleButton(
                        checked = state.alwaysOnDisplayEnabled,
                        onCheckedChange = actions.onAlwaysOnDisplayChange,
                        modifier = Modifier.size(height),
                        shapes = ButtonGroupDefaults.connectedLeadingButtonShapes(),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.power_settings_new),
                            contentDescription = stringResource(R.string.screen_aod_title),
                            modifier = Modifier.size(iconSize)
                        )
                    }

                    FilledTonalButton(
                        onClick = {
                            val intent = Intent(
                                context, AlwaysOnDisplayActivity::class.java
                            ).apply {
                                putExtra(Constants.EXTRA_AOD_TEST, true)
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier.size(height),
                        shapes = ButtonDefaults.shapes(
                            shape = ButtonGroupDefaults.connectedTrailingButtonShape,
                            pressedShape = ButtonGroupDefaults.connectedTrailingButtonPressShape
                        ),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.play_arrow),
                            contentDescription = stringResource(R.string.aod_try_now),
                            modifier = Modifier.size(playIconSize)
                        )
                    }
                }
            }
        }
    }
}
