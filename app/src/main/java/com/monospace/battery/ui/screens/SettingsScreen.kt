package com.monospace.battery.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import com.monospace.battery.R
import com.monospace.battery.helpers.WidgetsUtils
import com.monospace.battery.ui.components.SettingsItem
import com.monospace.battery.ui.theme.BatteryTheme

@Composable
fun SettingsScreen(
    onUnlockClick: () -> Unit,
    onUpdateClick: () -> Unit,
    onRateClick: () -> Unit
) {
    val context = LocalContext.current
    val isWidgetsPurchased by remember { mutableStateOf(WidgetsUtils.isWidgetsPurchased(context)) }

    val versionName = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
    }

    SettingsContent(
        isWidgetsPurchased = isWidgetsPurchased,
        versionName = versionName,
        onUnlockClick = onUnlockClick,
        onUpdateClick = onUpdateClick,
        onRateClick = onRateClick
    )
}

@Composable
fun SettingsContent(
    isWidgetsPurchased: Boolean,
    versionName: String,
    onUnlockClick: () -> Unit,
    onUpdateClick: () -> Unit,
    onRateClick: () -> Unit
) {
    val azeretMonoLight = FontFamily(Font(R.font.azeret_mono_light))

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            if (!isWidgetsPurchased) {
                SettingsItem(
                    title = stringResource(R.string.settings_unlock_full),
                    description = stringResource(R.string.widget_purchase_description),
                    onClick = onUnlockClick
                )
            }

            SettingsItem(
                title = stringResource(R.string.settings_software_update),
                description = stringResource(R.string.settings_version, versionName),
                onClick = onUpdateClick
            )

            SettingsItem(
                title = stringResource(R.string.settings_rate_us),
                onClick = onRateClick
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    BatteryTheme {
        SettingsContent(
            isWidgetsPurchased = false,
            versionName = "1.0.0",
            onUnlockClick = {},
            onUpdateClick = {},
            onRateClick = {}
        )
    }
}
