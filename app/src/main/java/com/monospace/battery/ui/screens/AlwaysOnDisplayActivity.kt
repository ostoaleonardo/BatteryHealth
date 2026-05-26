package com.monospace.battery.ui.screens

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.monospace.battery.R
import com.monospace.battery.core.constants.Constants
import com.monospace.battery.core.utils.BatteryUtils
import com.monospace.battery.data.local.PreferenceManager
import com.monospace.battery.data.models.BatteryInfo
import com.monospace.battery.ui.components.drawAodMeter
import com.monospace.battery.ui.theme.BatteryTheme
import com.monospace.battery.ui.theme.Font
import java.text.SimpleDateFormat
import java.util.Date

class AlwaysOnDisplayActivity : ComponentActivity() {

    private var batteryLevel by mutableIntStateOf(0)
    private var isCharging by mutableStateOf(false)
    private var chargingSpeed by mutableDoubleStateOf(0.0)
    private var timeRemaining by mutableStateOf("")

    private var clockStyle by mutableIntStateOf(0)
    private var meterStyle by mutableIntStateOf(0)
    private var colorLong by mutableLongStateOf(0xFF00A25B)
    private var showDate by mutableStateOf(true)
    private var showClock by mutableStateOf(true)
    private var is24h by mutableStateOf(true)
    private var fontSizeClock by mutableIntStateOf(80)
    private var fontSizeDate by mutableIntStateOf(14)
    private var dimAmount by mutableIntStateOf(0)
    private var showShortcuts by mutableStateOf(false)

    private val batteryUtils by lazy { BatteryUtils(this) }
    private val prefs by lazy { PreferenceManager(this) }

    private var isTestMode = false

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val info = BatteryInfo(intent)
            batteryLevel = info.level
            isCharging = info.isCharging
            chargingSpeed = batteryUtils.getChargeSpeed(info.voltage, info.isCharging)
            timeRemaining = batteryUtils.getChargeTimeRemaining(info.isCharging)

            // Close AOD if disconnected (unless in test mode)
            if (!isTestMode && !info.isCharging && intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                finish()
            }

            loadSettings()
        }
    }

    private fun loadSettings() {
        clockStyle = prefs.getInt(Constants.PREFS_ALERTS, Constants.KEY_AOD_CLOCK_STYLE, 0)
        meterStyle = prefs.getInt(Constants.PREFS_ALERTS, Constants.KEY_AOD_METER_STYLE, 0)
        colorLong = prefs.getLong(Constants.PREFS_ALERTS, Constants.KEY_AOD_COLOR, 0xFF00A25B)
        showDate = prefs.getBoolean(Constants.PREFS_ALERTS, Constants.KEY_AOD_SHOW_DATE, true)
        showClock = prefs.getBoolean(Constants.PREFS_ALERTS, Constants.KEY_AOD_SHOW_CLOCK, true)
        is24h = prefs.getBoolean(Constants.PREFS_ALERTS, Constants.KEY_AOD_24H_FORMAT, true)
        fontSizeClock = prefs.getInt(Constants.PREFS_ALERTS, Constants.KEY_AOD_FONT_SIZE_CLOCK, 80)
        fontSizeDate = prefs.getInt(Constants.PREFS_ALERTS, Constants.KEY_AOD_FONT_SIZE_DATE, 14)
        dimAmount = prefs.getInt(Constants.PREFS_ALERTS, Constants.KEY_AOD_DIM_AMOUNT, 0)
        showShortcuts =
            prefs.getBoolean(Constants.PREFS_ALERTS, Constants.KEY_AOD_SHOW_SHORTCUTS, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isTestMode = intent.getBooleanExtra(Constants.EXTRA_AOD_TEST, false)
        loadSettings()

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
            BatteryTheme {
                val themePrimary = MaterialTheme.colorScheme.primary
                val finalAccentColor = if (colorLong == 0L) themePrimary else Color(colorLong)

                AODContent(
                    level = batteryLevel,
                    isCharging = isCharging,
                    speed = chargingSpeed,
                    remaining = timeRemaining,
                    clockStyle = clockStyle,
                    meterStyle = meterStyle,
                    accentColor = finalAccentColor,
                    showDate = showDate,
                    showClock = showClock,
                    is24h = is24h,
                    fontSizeClock = fontSizeClock,
                    fontSizeDate = fontSizeDate,
                    dimAmount = dimAmount,
                    showShortcuts = showShortcuts
                )
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(
                batteryReceiver,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED),
                RECEIVER_NOT_EXPORTED
            )
        } else {
            registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        runCatching { unregisterReceiver(batteryReceiver) }
    }
}

@Composable
fun AODContent(
    level: Int,
    isCharging: Boolean,
    speed: Double,
    remaining: String,
    clockStyle: Int,
    meterStyle: Int,
    accentColor: Color,
    showDate: Boolean,
    showClock: Boolean,
    is24h: Boolean,
    fontSizeClock: Int,
    fontSizeDate: Int,
    dimAmount: Int,
    showShortcuts: Boolean
) {
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val context = LocalContext.current

    LaunchedEffect(meterStyle) {
        val delayTime = if (meterStyle == 5) 50L else 1000L
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
        if (meterStyle == 5) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawAodMeter(
                    meterStyle,
                    level,
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
            AODClockSection(
                currentTime = currentTime,
                showClock = showClock,
                showDate = showDate,
                is24h = is24h,
                fontSizeClock = fontSizeClock,
                fontSizeDate = fontSizeDate,
                clockStyle = clockStyle
            )

            Spacer(modifier = Modifier.height(48.dp))

            AODMeterSection(
                level = level,
                isCharging = isCharging,
                speed = speed,
                meterStyle = meterStyle,
                accentColor = accentColor,
                currentTime = currentTime,
                clockStyle = clockStyle
            )

            if (isCharging && remaining.isNotEmpty() && remaining != "00:00") {
                Spacer(modifier = Modifier.height(24.dp))
                AODRemainingTimeSection(
                    remaining = remaining,
                    clockStyle = clockStyle
                )
            }
        }

        // 3. Shortcuts Row
        if (showShortcuts) {
            AODShortcutsSection()
        }

        // 4. Dim Overlay
        if (dimAmount > 0) {
            AODDimOverlay(dimAmount)
        }
    }
}

@Composable
fun AODClockSection(
    currentTime: Long,
    showClock: Boolean,
    showDate: Boolean,
    is24h: Boolean,
    fontSizeClock: Int,
    fontSizeDate: Int,
    clockStyle: Int
) {
    val dateFormat = SimpleDateFormat("EEEE, d MMMM", LocalLocale.current.platformLocale)
    val timeFormat = SimpleDateFormat(
        if (is24h) "HH:mm" else "hh:mm", LocalLocale.current.platformLocale
    )

    if (showClock) {
        Text(
            text = timeFormat.format(Date(currentTime)),
            color = Color.White,
            fontSize = fontSizeClock.sp,
            fontFamily = Font.getAodFont(clockStyle),
            style = TextStyle(
                platformStyle = PlatformTextStyle(includeFontPadding = false)
            )
        )
    }

    if (showDate) {
        Text(
            text = dateFormat.format(Date(currentTime)).uppercase(),
            color = Color.Gray,
            fontSize = fontSizeDate.sp,
            fontFamily = Font.AzeretMonoLight,
            style = TextStyle(
                platformStyle = PlatformTextStyle(includeFontPadding = false)
            )
        )
    }
}

@Composable
fun AODMeterSection(
    level: Int,
    isCharging: Boolean,
    speed: Double,
    meterStyle: Int,
    accentColor: Color,
    currentTime: Long,
    clockStyle: Int
) {
    if (meterStyle != 5) {
        Box(contentAlignment = Alignment.Center) {
            if (meterStyle != 0) {
                Canvas(modifier = Modifier.size(200.dp)) {
                    drawAodMeter(
                        meterStyle,
                        level,
                        if (isCharging) accentColor else Color.White,
                        currentTime
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$level%",
                    color = Color.White,
                    fontSize = 44.sp,
                    fontFamily = Font.getAodFont(clockStyle)
                )
                if (isCharging) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.aod_charging_speed_label).uppercase(),
                            color = Color.Gray,
                            fontSize = 10.sp,
                            fontFamily = Font.AzeretMonoLight
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${speed}W",
                            color = accentColor,
                            fontSize = 16.sp,
                            fontFamily = Font.getAodFont(clockStyle)
                        )
                    }
                }
            }
        }
    } else {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$level%",
                color = Color.White,
                fontSize = 54.sp,
                fontFamily = Font.getAodFont(clockStyle)
            )
            if (isCharging) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.aod_charging_speed_label).uppercase(),
                        color = Color.Gray.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        fontFamily = Font.AzeretMonoLight
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${speed}W",
                        color = accentColor,
                        fontSize = 20.sp,
                        fontFamily = Font.getAodFont(clockStyle)
                    )
                }
            }
        }
    }
}

@Composable
fun AODRemainingTimeSection(
    remaining: String,
    clockStyle: Int
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(R.string.aod_time_remaining_label).uppercase(),
            color = Color.Gray,
            fontSize = 10.sp,
            fontFamily = Font.AzeretMonoLight
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = remaining,
            color = Color.Gray,
            fontSize = 18.sp,
            fontFamily = Font.getAodFont(clockStyle)
        )
    }
}

@Composable
fun AODShortcutsSection() {
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
fun AODDimOverlay(dimAmount: Int) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = dimAmount / 100f))
    )
}
