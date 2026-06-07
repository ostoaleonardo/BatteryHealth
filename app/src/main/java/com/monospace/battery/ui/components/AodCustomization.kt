package com.monospace.battery.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monospace.battery.R
import com.monospace.battery.core.constants.Constants
import com.monospace.battery.data.models.LocalBatteryState
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import com.monospace.battery.ui.theme.Font as AppFont

@Composable
fun AodStyleSelectors(
    clockStyle: Int,
    meterStyle: Int,
    selectedColor: Color,
    is24h: Boolean,
    onClockStyleChange: (Int) -> Unit,
    onMeterStyleChange: (Int) -> Unit,
    onColorChange: (Long) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // 1. Clock Style Selector
        AodClockStyleSelector(
            selectedIndex = clockStyle,
            is24h = is24h,
            onSelect = onClockStyleChange
        )

        // 2. Meter Style Selector
        AodMeterStyleSelector(
            selectedIndex = meterStyle,
            selectedColor = selectedColor,
            onSelect = onMeterStyleChange
        )

        // 3. Color Selector
        AodColorSelector(
            selectedColor = selectedColor,
            onColorSelect = onColorChange
        )
    }
}

@Composable
fun AodClockStyleSelector(
    selectedIndex: Int,
    is24h: Boolean,
    onSelect: (Int) -> Unit
) {
    StyleSelector(
        title = stringResource(R.string.aod_clock_style),
        count = 6,
        selectedIndex = selectedIndex,
        onSelect = onSelect
    ) { index, isSelected ->
        Text(
            text = if (is24h) "14:30" else "02:30",
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            fontSize = 18.sp,
            fontFamily = AppFont.getAodFont(index)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AodMeterStyleSelector(
    selectedIndex: Int,
    selectedColor: Color,
    onSelect: (Int) -> Unit
) {
    StyleSelector(
        title = stringResource(R.string.aod_meter_style),
        count = MeterStyle.entries.size,
        selectedIndex = selectedIndex,
        onSelect = onSelect
    ) { index, isSelected ->
        val unselectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        val color = if (isSelected) selectedColor else unselectedColor

        Box(contentAlignment = Alignment.Center) {
            MeterDisplay(
                styleIndex = index,
                color = color,
                modifier = Modifier.size(36.dp),
                strokeWidth = 3.dp,
                size = 36.dp
            )

            // Percentage visible for all except water glass (index 6)
            if (index != MeterStyle.WATER_GLASS.index) {
                Text(
                    text = "${LocalBatteryState.current.level}%",
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    fontSize = 9.sp,
                    fontFamily = AppFont.AzeretMonoLight
                )
            }
        }
    }
}

@Composable
fun AodPreviewCard(
    level: Int,
    color: Color,
    clockStyle: Int,
    meterStyle: Int,
    showDate: Boolean,
    showClock: Boolean = true,
    showShortcuts: Boolean = false,
    is24h: Boolean,
    fontSizeClock: Int = 24,
    fontSizeDate: Int = 6
) {
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(meterStyle) {
        val delayTime = if (meterStyle == MeterStyle.WATER_GLASS.index) 50L else 1000L
        while (true) {
            currentTime = System.currentTimeMillis()
            delay(delayTime)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(130.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(24.dp))
                .border(2.dp, Color.DarkGray, RoundedCornerShape(24.dp))
                .padding(4.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            // 1. Water Background (if style 6)
            if (meterStyle == MeterStyle.WATER_GLASS.index) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawAodMeter(MeterStyle.WATER_GLASS, level, color, currentTime)
                }
            }

            // 2. Content
            AodPreviewContent(
                showClock = showClock,
                showDate = showDate,
                is24h = is24h,
                fontSizeClock = fontSizeClock,
                fontSizeDate = fontSizeDate,
                clockStyle = clockStyle,
                meterStyle = meterStyle,
                color = color
            )

            // 4. Shortcuts Preview
            if (showShortcuts) {
                AodPreviewShortcuts()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AodPreviewContent(
    showClock: Boolean,
    showDate: Boolean,
    is24h: Boolean,
    fontSizeClock: Int,
    fontSizeDate: Int,
    clockStyle: Int,
    meterStyle: Int,
    color: Color
) {
    val locale = LocalLocale.current.platformLocale
    val currentTime = System.currentTimeMillis() // Static for preview
    val timeFormat = SimpleDateFormat(if (is24h) "HH:mm" else "hh:mm", locale)
    val dateFormat = SimpleDateFormat("EEE, d MMM", locale)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (showClock) {
            Text(
                text = timeFormat.format(Date(currentTime)),
                color = Color.White,
                fontSize = fontSizeClock.sp,
                fontFamily = AppFont.getAodFont(clockStyle),
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
                fontFamily = AppFont.AzeretMonoLight,
                style = TextStyle(
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                )
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 1. Meter (Percentage only inside)
        if (meterStyle != MeterStyle.WATER_GLASS.index) {
            Box(contentAlignment = Alignment.Center) {
                MeterDisplay(
                    styleIndex = meterStyle,
                    color = color,
                    modifier = Modifier.size(50.dp),
                    strokeWidth = 4.dp,
                    size = 50.dp
                )

                Text(
                    text = "${LocalBatteryState.current.level}%",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontFamily = AppFont.getAodFont(clockStyle)
                )
            }
        } else {
            Text(
                text = "${LocalBatteryState.current.level}%",
                color = Color.White,
                fontSize = 14.sp,
                fontFamily = AppFont.getAodFont(clockStyle)
            )
        }

        // 2. Metrics (Always shown in preview for context)
        Spacer(modifier = Modifier.height(16.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "25W",
                    color = color,
                    fontSize = 6.sp,
                    fontFamily = AppFont.getAodFont(clockStyle),
                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                )
                Text(
                    text = stringResource(R.string.aod_charging_speed_label).uppercase(),
                    color = Color.Gray,
                    fontSize = 3.sp,
                    fontFamily = AppFont.AzeretMonoLight,
                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "01:20",
                    color = Color.White,
                    fontSize = 6.sp,
                    fontFamily = AppFont.getAodFont(clockStyle),
                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                )
                Text(
                    text = stringResource(R.string.aod_time_remaining_label).uppercase(),
                    color = Color.Gray,
                    fontSize = 3.sp,
                    fontFamily = AppFont.AzeretMonoLight,
                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                )
            }
        }
    }
}

@Composable
fun AodPreviewShortcuts() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 8.dp)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
            )
        }
    }
}

@Composable
fun AodColorSelector(
    selectedColor: Color,
    onColorSelect: (Long) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Text(
            text = stringResource(R.string.aod_accent_color).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontFamily = AppFont.AzeretMonoLight,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(Constants.AodColors) { _, colorValue ->
                val isDynamic = colorValue == 0L
                val color = if (isDynamic) MaterialTheme.colorScheme.primary else Color(colorValue)
                val isSelected = selectedColor == color

                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .selectionStyle(
                            isSelected = isSelected,
                            shape = CircleShape,
                            showUnselectedBorder = isDynamic,
                            backgroundColor = if (isDynamic) Color.Transparent else color
                        )
                        .clickable { onColorSelect(colorValue) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isDynamic) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StyleSelector(
    title: String,
    count: Int,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    content: @Composable (Int, Boolean) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontFamily = AppFont.AzeretMonoLight,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val shape = RoundedCornerShape(24.dp)

            items(count) { index ->
                val isSelected = selectedIndex == index

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .selectionStyle(isSelected, shape)
                        .clickable { onSelect(index) },
                    contentAlignment = Alignment.Center
                ) {
                    content(index, isSelected)
                }
            }
        }
    }
}
