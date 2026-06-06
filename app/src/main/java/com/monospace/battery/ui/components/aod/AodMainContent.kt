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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monospace.battery.R
import com.monospace.battery.core.constants.Constants
import com.monospace.battery.data.models.LocalBatteryState
import com.monospace.battery.data.models.LocalSettingsState
import com.monospace.battery.ui.components.drawAodMeter
import com.monospace.battery.ui.theme.Font
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun AodMainContent() {
    val state = LocalSettingsState.current
    val batteryState = LocalBatteryState.current

    val themePrimary = MaterialTheme.colorScheme.primary
    val accentColor = if (state.aodColor == 0L) themePrimary else Color(state.aodColor)

    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val context = LocalContext.current

    LaunchedEffect(state.aodMeterStyle) {
        val delayTime = if (state.aodMeterStyle == 5) 50L else 1000L
        while (true) {
            currentTime = System.currentTimeMillis()
            kotlinx.coroutines.delay(delayTime)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable { (context as? Activity)?.finish() }
    ) {
        // 1. Water Background
        if (state.aodMeterStyle == 5) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawAodMeter(
                    state.aodMeterStyle,
                    batteryState.level,
                    accentColor,
                    currentTime
                )
            }
        }

        // 2. Main Content
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AODClockSection(currentTime)

            Spacer(modifier = Modifier.height(48.dp))

            AODMeterSection(currentTime, accentColor)

            if (batteryState.isCharging) {
                Spacer(modifier = Modifier.height(32.dp))
                AODMetricsSection(accentColor)
            }
        }

        // 3. Shortcuts Row
        if (state.aodShowShortcuts) {
            AODShortcutsSection()
        }

        // 4. Dim Overlay
        if (state.aodDimAmount > 0) {
            AODDimOverlay(state.aodDimAmount)
        }
    }
}

@Composable
private fun AODClockSection(currentTime: Long) {
    val state = LocalSettingsState.current
    val dateFormat = SimpleDateFormat("EEEE, d MMMM", LocalLocale.current.platformLocale)
    val timeFormat = SimpleDateFormat(
        if (state.aod24hFormat) "HH:mm" else "hh:mm", LocalLocale.current.platformLocale
    )

    if (state.aodShowClock) {
        Text(
            text = timeFormat.format(Date(currentTime)),
            color = Color.White,
            fontSize = state.aodFontSizeClock.sp,
            fontFamily = Font.getAodFont(state.aodClockStyle),
            style = TextStyle(
                platformStyle = PlatformTextStyle(includeFontPadding = false)
            )
        )
    }

    if (state.aodShowDate) {
        Text(
            text = dateFormat.format(Date(currentTime)).uppercase(),
            color = Color.Gray,
            fontSize = state.aodFontSizeDate.sp,
            fontFamily = Font.AzeretMonoLight,
            style = TextStyle(
                platformStyle = PlatformTextStyle(includeFontPadding = false)
            )
        )
    }
}

@Composable
private fun AODMeterSection(
    currentTime: Long,
    accentColor: Color
) {
    val state = LocalSettingsState.current
    val batteryState = LocalBatteryState.current

    if (state.aodMeterStyle != 5) {
        Box(contentAlignment = Alignment.Center) {
            if (state.aodMeterStyle != 0) {
                Canvas(modifier = Modifier.size(200.dp)) {
                    drawAodMeter(
                        state.aodMeterStyle,
                        batteryState.level,
                        accentColor,
                        currentTime
                    )
                }
            }

            Text(
                text = "${batteryState.level}%",
                color = Color.White,
                fontSize = 44.sp,
                fontFamily = Font.getAodFont(state.aodClockStyle)
            )
        }
    } else {
        Text(
            text = "${batteryState.level}%",
            color = Color.White,
            fontSize = 54.sp,
            fontFamily = Font.getAodFont(state.aodClockStyle)
        )
    }
}

@Composable
private fun AODMetricsSection(accentColor: Color) {
    val state = LocalSettingsState.current
    val batteryState = LocalBatteryState.current

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // 1. Charging Speed
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${batteryState.chargeSpeed}W",
                color = accentColor,
                fontSize = 20.sp,
                fontFamily = Font.getAodFont(state.aodClockStyle),
                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
            )
            Text(
                text = stringResource(R.string.aod_charging_speed_label).uppercase(),
                color = Color.Gray,
                fontSize = 10.sp,
                fontFamily = Font.AzeretMonoLight
            )
        }

        // 2. Remaining Time
        if (batteryState.timeRemaining.isNotEmpty() && batteryState.timeRemaining != Constants.ZERO_TIME) {
            Spacer(modifier = Modifier.height(20.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = batteryState.timeRemaining,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontFamily = Font.getAodFont(state.aodClockStyle),
                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                )
                Text(
                    text = stringResource(R.string.aod_time_remaining_label).uppercase(),
                    color = Color.Gray,
                    fontSize = 10.sp,
                    fontFamily = Font.AzeretMonoLight
                )
            }
        }
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

            // Flashlight
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f))
                    .clickable {
                        runCatching {
                            val cameraManager =
                                context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
                            val cameraId = cameraManager.cameraIdList[0]
                            isFlashlightOn = !isFlashlightOn
                            cameraManager.setTorchMode(cameraId, isFlashlightOn)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(if (isFlashlightOn) R.drawable.flashlight_on else R.drawable.flashlight_off),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Camera
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f))
                    .clickable {
                        runCatching {
                            val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            context.startActivity(intent)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.photo_camera),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
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
