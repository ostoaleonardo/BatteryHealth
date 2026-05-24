package com.monospace.battery.ui.screens

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monospace.battery.core.constants.Constants
import com.monospace.battery.core.utils.BatteryUtils
import com.monospace.battery.data.local.PreferenceManager
import com.monospace.battery.data.models.BatteryInfo
import com.monospace.battery.ui.components.drawAodMeter
import java.text.SimpleDateFormat
import java.util.Date
import com.monospace.battery.ui.theme.Font as AppFont

class AlwaysOnDisplayActivity : ComponentActivity() {

    private var batteryLevel by mutableIntStateOf(0)
    private var isCharging by mutableStateOf(false)
    private var chargingSpeed by mutableDoubleStateOf(0.0)
    private var timeRemaining by mutableStateOf("")

    private var clockStyle by mutableIntStateOf(0)
    private var meterStyle by mutableIntStateOf(0)
    private var colorLong by mutableLongStateOf(0xFF00A25B)
    private var showDate by mutableStateOf(true)
    private var is24h by mutableStateOf(true)
    private var fontSizeClock by mutableIntStateOf(80)
    private var fontSizeDate by mutableIntStateOf(14)

    private val batteryUtils by lazy { BatteryUtils(this) }
    private val prefs by lazy { PreferenceManager(this) }

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val info = BatteryInfo(intent)
            batteryLevel = info.level
            isCharging = info.isCharging
            chargingSpeed = batteryUtils.getChargeSpeed(info.voltage, info.isCharging)
            timeRemaining = batteryUtils.getChargeTimeRemaining(info.isCharging)

            loadSettings()
        }
    }

    private fun loadSettings() {
        clockStyle = prefs.getInt(Constants.PREFS_ALERTS, Constants.KEY_AOD_CLOCK_STYLE, 0)
        meterStyle = prefs.getInt(Constants.PREFS_ALERTS, Constants.KEY_AOD_METER_STYLE, 0)
        colorLong = prefs.getLong(Constants.PREFS_ALERTS, Constants.KEY_AOD_COLOR, 0xFF00A25B)
        showDate = prefs.getBoolean(Constants.PREFS_ALERTS, Constants.KEY_AOD_SHOW_DATE, true)
        is24h = prefs.getBoolean(Constants.PREFS_ALERTS, Constants.KEY_AOD_24H_FORMAT, true)
        fontSizeClock = prefs.getInt(Constants.PREFS_ALERTS, Constants.KEY_AOD_FONT_SIZE_CLOCK, 80)
        fontSizeDate = prefs.getInt(Constants.PREFS_ALERTS, Constants.KEY_AOD_FONT_SIZE_DATE, 14)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loadSettings()

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        enableEdgeToEdge()

        // Hide status bar and navigation bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)
            val controller = window.insetsController
            if (controller != null) {
                controller.hide(android.view.WindowInsets.Type.statusBars() or android.view.WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior =
                    android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
                    or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
        }

        setContent {
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
                is24h = is24h,
                fontSizeClock = fontSizeClock,
                fontSizeDate = fontSizeDate
            )
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
        unregisterReceiver(batteryReceiver)
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
    is24h: Boolean,
    fontSizeClock: Int,
    fontSizeDate: Int
) {
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1000)
            currentTime = System.currentTimeMillis()
        }
    }

    val timeFormat =
        SimpleDateFormat(if (is24h) "HH:mm" else "hh:mm a", LocalLocale.current.platformLocale)
    val dateFormat = SimpleDateFormat("EEEE, d MMMM", LocalLocale.current.platformLocale)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = timeFormat.format(Date(currentTime)),
                color = Color.White,
                fontSize = fontSizeClock.sp,
                fontFamily = AppFont.getAodFont(clockStyle),
                style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
            )

            if (showDate) {
                Text(
                    text = dateFormat.format(Date(currentTime)).uppercase(),
                    color = Color.Gray,
                    fontSize = fontSizeDate.sp,
                    fontFamily = AppFont.AzeretMonoLight,
                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false)),
                    modifier = Modifier.offset(y = (-16).dp)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Box(contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(200.dp)) {
                    drawAodMeter(
                        meterStyle,
                        level,
                        if (isCharging) accentColor else Color.White
                    )
                }

                // Percentage visible for all arc styles (0 to 3)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$level%",
                        color = Color.White,
                        fontSize = 44.sp,
                        fontFamily = AppFont.getAodFont(clockStyle)
                    )
                    if (isCharging) {
                        Text(
                            text = "${speed}W",
                            color = accentColor,
                            fontSize = 16.sp,
                            fontFamily = AppFont.getAodFont(clockStyle)
                        )
                    }
                }
            }

            if (isCharging && remaining.isNotEmpty() && remaining != "00:00") {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = remaining,
                    color = Color.Gray,
                    fontSize = 18.sp,
                    fontFamily = AppFont.getAodFont(clockStyle)
                )
            }
        }
    }
}
