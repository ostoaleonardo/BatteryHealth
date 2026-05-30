package com.monospace.battery.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.monospace.battery.R
import com.monospace.battery.core.utils.AppUtils
import com.monospace.battery.data.models.LocalSettingsActions
import com.monospace.battery.data.models.LocalSettingsState
import com.monospace.battery.ui.components.SettingsSection
import com.monospace.battery.ui.theme.BatteryTheme

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val state = LocalSettingsState.current
    val actions = LocalSettingsActions.current

    SettingsContent(
        isPremium = state.isWidgetsPurchased,
        versionName = state.versionName,
        onUnlockClick = actions.onUnlockClick,
        onGoogleClick = { AppUtils.openPlayStore(context) }
    )
}

@Composable
fun SettingsContent(
    isPremium: Boolean,
    versionName: String,
    onUnlockClick: () -> Unit,
    onGoogleClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 8.dp, bottom = 24.dp)
        ) {
            if (!isPremium) {
                SettingsSection(stringResource(R.string.premium_title)) {
                    val unlockTitle = stringResource(R.string.settings_unlock_full)
                    val unlockDesc = stringResource(R.string.widget_purchase_description)

                    item(
                        title = unlockTitle,
                        description = unlockDesc,
                        onClick = onUnlockClick
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            SettingsSection(stringResource(R.string.settings_about_app)) {
                val updateTitle = stringResource(R.string.settings_software_update)
                val updateDesc = stringResource(R.string.settings_version, versionName)
                val rateTitle = stringResource(R.string.settings_rate_us)

                item(
                    title = updateTitle,
                    description = updateDesc,
                    onClick = onGoogleClick
                )

                item(
                    title = rateTitle,
                    onClick = onGoogleClick
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    BatteryTheme {
        SettingsContent(
            isPremium = false,
            versionName = "1.1.2",
            onUnlockClick = {},
            onGoogleClick = {}
        )
    }
}
