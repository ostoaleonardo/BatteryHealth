package com.monospace.battery.ui.screens

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.monospace.battery.core.constants.Constants
import com.monospace.battery.data.models.LocalBatteryState
import com.monospace.battery.data.models.LocalSettingsState
import com.monospace.battery.ui.components.aod.AodMainContent
import com.monospace.battery.ui.theme.BatteryTheme
import com.monospace.battery.ui.viewmodels.AodViewModel
import com.monospace.battery.ui.viewmodels.SettingsViewModel

class AlwaysOnDisplayActivity : ComponentActivity() {

    private val settingsViewModel: SettingsViewModel by viewModels()
    private val aodViewModel: AodViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        aodViewModel.isTestMode = intent.getBooleanExtra(Constants.EXTRA_AOD_TEST, false)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        enableEdgeToEdge()

        // Immersive mode using WindowInsetsController
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        setContent {
            val settingsState by settingsViewModel.uiState
            val batteryState by aodViewModel.batteryState
            val shouldFinish by aodViewModel.shouldFinish

            LaunchedEffect(shouldFinish) {
                if (shouldFinish) finish()
            }

            BatteryTheme {
                CompositionLocalProvider(
                    LocalSettingsState provides settingsState,
                    LocalBatteryState provides batteryState
                ) {
                    AodMainContent()
                }
            }
        }
    }
}
