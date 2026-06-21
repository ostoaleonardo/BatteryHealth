package com.monospace.battery.ui.components.aod

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.hardware.camera2.CameraManager
import android.provider.MediaStore
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.monospace.battery.R
import com.monospace.battery.core.constants.Constants
import com.monospace.battery.data.models.LocalBatteryState
import com.monospace.battery.data.models.LocalSettingsState
import com.monospace.battery.ui.components.AodDimensions
import com.monospace.battery.ui.components.AodLayoutContent
import com.monospace.battery.ui.components.MeterStyle
import com.monospace.battery.ui.components.ShortcutIcon
import com.monospace.battery.ui.components.drawAodMeter
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AodMainContent() {
    val state = LocalSettingsState.current
    val batteryState = LocalBatteryState.current
    val themePrimary = MaterialTheme.colorScheme.primary
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    val accentColor = if (state.aodColor == 0L) themePrimary else Color(state.aodColor)
    val isWaterGlass = state.aodMeterStyle == MeterStyle.WATER_GLASS.index

    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val context = LocalContext.current

    LaunchedEffect(state.aodMeterStyle) {
        val delayTime = if (isWaterGlass) Constants.WATER_GLASS_DELAY
        else Constants.PREVIEW_TIME_DELAY

        while (true) {
            currentTime = System.currentTimeMillis()
            delay(delayTime)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable { (context as? Activity)?.finish() },
    ) {
        if (isWaterGlass) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawAodMeter(
                    timeMillis = currentTime,
                    style = MeterStyle.WATER_GLASS,
                    level = batteryState.level,
                    color = accentColor
                )
            }
        }

        AodLayoutContent(
            modifier = Modifier.fillMaxSize(),
            currentTime = currentTime,
            dimensions = AodDimensions.Default,
            verticalArrangement = Arrangement.Center,
            isLandscape = isLandscape
        )

        if (state.aodShowShortcuts) AODShortcutsSection()
        if (state.aodDimAmount > 0) AODDimOverlay(state.aodDimAmount)
    }
}

@Composable
private fun AODShortcutsSection() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 24.dp)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val context = LocalContext.current
            var isFlashlightOn by remember { mutableStateOf(false) }

            ShortcutIcon(
                iconRes = if (isFlashlightOn) R.drawable.flashlight_on else R.drawable.flashlight_off,
                onClick = {
                    runCatching {
                        val cameraManager =
                            context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
                        val cameraId = cameraManager.cameraIdList[0]
                        isFlashlightOn = !isFlashlightOn
                        cameraManager.setTorchMode(cameraId, isFlashlightOn)
                    }
                }
            )

            ShortcutIcon(
                iconRes = R.drawable.photo_camera,
                onClick = {
                    runCatching {
                        val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    }
                }
            )
        }
    }
}

@Composable
private fun AODDimOverlay(dimAmount: Int) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = dimAmount / 100f))
    )
}
