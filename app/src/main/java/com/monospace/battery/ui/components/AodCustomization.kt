package com.monospace.battery.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monospace.battery.R
import java.text.SimpleDateFormat
import java.util.Date
import com.monospace.battery.ui.theme.Font as AppFont

@Composable
fun AodStyleSelectors(
    level: Int,
    clockStyle: Int,
    meterStyle: Int,
    selectedColor: Color,
    is24h: Boolean,
    onClockStyleChange: (Int) -> Unit,
    onMeterStyleChange: (Int) -> Unit,
    onColorChange: (Long) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // 1. Clock Style (Squares)
        StyleSelector(
            title = stringResource(R.string.aod_clock_style),
            count = 6,
            selectedIndex = clockStyle,
            onSelect = onClockStyleChange
        ) { index ->
            Text(
                text = if (is24h) "14:30" else "02:30",
                color = Color.White,
                fontSize = 18.sp,
                fontFamily = AppFont.getAodFont(index)
            )
        }

        // 2. Speedometer Style (Squares)
        StyleSelector(
            title = stringResource(R.string.aod_meter_style),
            count = 3,
            selectedIndex = meterStyle,
            onSelect = onMeterStyleChange
        ) { index ->
            Box(contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(36.dp)) {
                    val strokeWidth = 2.dp.toPx()
                    val sweep = if (index == 2) 360f else 260f
                    val start = if (index == 2) 0f else 140f
                    
                    drawArc(
                        color = Color.Gray.copy(alpha = 0.2f),
                        startAngle = start,
                        sweepAngle = sweep,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    
                    drawArc(
                        color = selectedColor,
                        startAngle = start,
                        sweepAngle = (level / 100f) * sweep,
                        useCenter = false,
                        style = if (index == 1) Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                                else Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
                Text(
                    text = "$level", 
                    color = Color.White, 
                    fontSize = 9.sp,
                    fontFamily = AppFont.AzeretMonoLight
                )
            }
        }

        // 3. Color Style (Circles)
        ColorSelector(selectedColor = selectedColor, onColorSelect = onColorChange)
    }
}

@Composable
fun AodPreviewCard(
    level: Int,
    color: Color,
    clockStyle: Int,
    meterStyle: Int,
    showDate: Boolean,
    is24h: Boolean,
    fontSizeClock: Int = 24,
    fontSizeDate: Int = 6
) {
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1000)
            currentTime = System.currentTimeMillis()
        }
    }

    val locale = LocalLocale.current.platformLocale
    val timeFormat = SimpleDateFormat(if (is24h) "HH:mm" else "hh:mm a", locale)
    val dateFormat = SimpleDateFormat("EEE, d MMM", locale)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Smartphone Frame 9:19.5 proportional
        Box(
            modifier = Modifier
                .width(130.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF1A1A1A))
                .border(2.dp, Color.DarkGray, RoundedCornerShape(24.dp))
                .padding(4.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
                        modifier = Modifier.offset(y = (-8).dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Box(contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.size(50.dp)) {
                        val strokeWidth = 3.dp.toPx()
                        val sweep = if (meterStyle == 2) 360f else 260f
                        val start = if (meterStyle == 2) 0f else 140f
                        
                        drawArc(
                            color = Color.DarkGray.copy(alpha = 0.3f),
                            startAngle = start,
                            sweepAngle = sweep,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = color,
                            startAngle = start,
                            sweepAngle = (level / 100f) * sweep,
                            useCenter = false,
                            style = if (meterStyle == 1) Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                                    else Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                    Text(
                        text = "$level%",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontFamily = AppFont.getAodFont(clockStyle)
                    )
                }
            }
        }
    }
}

@Composable
fun ColorSelector(
    selectedColor: Color,
    onColorSelect: (Long) -> Unit
) {
    // 0L represents Dynamic Color
    val colors = listOf(
        0L, 0xFF00A25B, 0xFF2196F3, 0xFFE91E63, 
        0xFFFF9800, 0xFF9C27B0, 0xFF00BCD4, 0xFFFFEB3B, 0xFFFFFFFF
    )

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
            itemsIndexed(colors) { _, colorValue ->
                val isDynamic = colorValue == 0L
                val color = if (isDynamic) MaterialTheme.colorScheme.primary else Color(colorValue)
                
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(if (isDynamic) Color.Transparent else color)
                        .border(
                            width = if (selectedColor == color) 2.dp else 1.dp,
                            color = if (selectedColor == color) color else Color.White.copy(alpha = 0.1f),
                            shape = CircleShape
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
    content: @Composable (Int) -> Unit
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
            items(count) { index ->
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f))
                        .border(
                            width = if (selectedIndex == index) 2.dp else 1.dp,
                            color = if (selectedIndex == index) MaterialTheme.colorScheme.primary 
                                    else Color.White.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { onSelect(index) },
                    contentAlignment = Alignment.Center
                ) {
                    content(index)
                }
            }
        }
    }
}
