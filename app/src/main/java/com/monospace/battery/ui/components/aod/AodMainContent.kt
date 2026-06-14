package com.monospace.battery.ui.components.aod

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.provider.MediaStore
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monospace.battery.R
import com.monospace.battery.core.constants.Constants
import com.monospace.battery.data.models.LocalBatteryState
import com.monospace.battery.data.models.LocalSettingsState
import com.monospace.battery.ui.components.ClockDisplay
import com.monospace.battery.ui.components.DateDisplay
import com.monospace.battery.ui.components.MeterDisplay
import com.monospace.battery.ui.components.MeterStyle
import com.monospace.battery.ui.components.MetricItem
import com.monospace.battery.ui.components.PercentageDisplay
import com.monospace.battery.ui.components.ShortcutIcon
import com.monospace.battery.ui.components.drawAodMeter
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AodMainContent() {
    val state = LocalSettingsState.current
    val batteryState = LocalBatteryState.current
    val themePrimary = MaterialTheme.colorScheme.primary

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

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (state.aodShowClock) {
                ClockDisplay(
                    currentTime = currentTime,
                    styleIndex = state.aodClockStyle,
                    fontSize = state.aodFontSizeClock.sp,
                    is24h = state.aod24hFormat
                )
            }

            if (state.aodShowDate) {
                DateDisplay(
                    currentTime = currentTime,
                    fontSize = state.aodFontSizeDate.sp,
                    color = if (isWaterGlass) Color.White else Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Box(contentAlignment = Alignment.Center) {
                if (!isWaterGlass && state.aodMeterStyle != MeterStyle.NONE.index) {
                    MeterDisplay(
                        styleIndex = state.aodMeterStyle,
                        level = batteryState.level,
                        color = accentColor,
                        modifier = Modifier.size(200.dp),
                        strokeWidth = 10.dp,
                        size = 200.dp
                    )
                }

                PercentageDisplay(
                    level = batteryState.level,
                    fontSize = if (isWaterGlass) 54.sp else 44.sp,
                    styleIndex = state.aodClockStyle,
                    color = Color.White
                )
            }

            if (batteryState.isCharging) {
                Spacer(modifier = Modifier.height(32.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    MetricItem(
                        value = stringResource(
                            R.string.charge_speed_watts,
                            batteryState.chargeSpeed
                        ),
                        labelRes = R.string.aod_charging_speed_label,
                        valueColor = if (isWaterGlass) Color.White else accentColor,
                        labelColor = if (isWaterGlass) Color.White else Color.Gray,
                        styleIndex = state.aodClockStyle
                    )

                    if (batteryState.timeRemaining.isNotEmpty() && batteryState.timeRemaining != Constants.ZERO_TIME) {
                        Spacer(modifier = Modifier.height(20.dp))
                        MetricItem(
                            value = batteryState.timeRemaining,
                            labelRes = R.string.aod_time_remaining_label,
                            valueColor = Color.White,
                            labelColor = if (isWaterGlass) Color.White else Color.Gray,
                            styleIndex = state.aodClockStyle
                        )
                    }
                }
            }
        }

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
