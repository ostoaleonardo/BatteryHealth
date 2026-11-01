package com.monospace.battery.ui.components.aod

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monospace.battery.R
import com.monospace.battery.core.constants.Constants
import com.monospace.battery.data.models.BatteryState
import com.monospace.battery.data.models.LocalBatteryState
import com.monospace.battery.data.models.LocalSettingsActions
import com.monospace.battery.data.models.LocalSettingsState
import com.monospace.battery.ui.components.common.section.Section
import com.monospace.battery.ui.theme.Font
import com.monospace.battery.ui.utils.MeterDisplay
import com.monospace.battery.ui.utils.MeterStyle
import com.monospace.battery.ui.utils.selectionStyle

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
                    text = stringResource(
                        R.string.battery_percentage,
                        LocalBatteryState.current.level
                    ),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    fontSize = 9.sp,
                    fontFamily = Font.AzeretMonoLight
                )
            }
        }
    }
}

@Composable
fun AodPreviewCard(
    modifier: Modifier = Modifier,
    level: Int
) {
    val density = LocalDensity.current
    val window = LocalWindowInfo.current.containerSize
    val screenWidthDp = minOf(window.width, window.height) / density.density
    val screenHeightDp = maxOf(window.width, window.height) / density.density
    val shape = RoundedCornerShape(24.dp)

    Box(
        modifier = modifier.padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(screenWidthDp / screenHeightDp)
                .clip(shape)
                .border(2.dp, Color.DarkGray, shape)
                .padding(4.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.Black)
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val scale = maxWidth.value / screenWidthDp
                val previewDensity = Density(density.density * scale, density.fontScale)
                val mockBatteryState = BatteryState(
                    level = level,
                    isCharging = true,
                    chargeSpeed = 25.0,
                    timeRemaining = Constants.DUMMY_TIME_REMAINING
                )

                CompositionLocalProvider(
                    LocalDensity provides previewDensity,
                    LocalBatteryState provides mockBatteryState
                ) {
                    AodScene(
                        currentTime = rememberAodTime(LocalSettingsState.current.aodMeterStyle),
                        isLandscape = false,
                        interactive = false
                    )
                }
            }
        }
    }
}

@Composable
fun AodColorSelector(
    selectedColor: Long,
    onColorSelect: (Long) -> Unit
) {
    val colors = Constants.AodColors
    StyleSelector(
        title = stringResource(R.string.aod_accent_color),
        count = colors.size,
        selectedIndex = colors.indexOf(selectedColor),
        onSelect = { index ->
            val color = colors[index]
            onColorSelect(if (color == selectedColor) 0L else color)
        }
    ) { index, _ ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(colors[index]))
        )
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
    Column {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontFamily = Font.AzeretMonoLight,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )

        BoxWithConstraints {
            val contentWidth = StyleOptionSize * count + StyleOptionSpacing * (count - 1) + 40.dp
            val option: @Composable (Int) -> Unit = { index ->
                StyleOption(
                    selected = selectedIndex == index,
                    onClick = { onSelect(index) }
                ) { content(index, selectedIndex == index) }
            }

            if (contentWidth <= maxWidth) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    repeat(count) { option(it) }
                }
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(StyleOptionSpacing)
                ) {
                    items(count) { option(it) }
                }
            }
        }
    }
}

private val StyleOptionSize = 64.dp
private val StyleOptionSpacing = 12.dp

@Composable
private fun StyleOption(
    selected: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val radius by animateDpAsState(
        targetValue = if (selected) 16.dp else StyleOptionSize / 2,
        animationSpec = tween(durationMillis = 250),
        label = "style_radius"
    )

    Box(
        modifier = Modifier
            .size(StyleOptionSize)
            .selectionStyle(selected, RoundedCornerShape(radius))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun AodSettingsSectionContent() {
    val state = LocalSettingsState.current
    val actions = LocalSettingsActions.current

    Section(stringResource(R.string.aod_look_feel)) {
        // Clock Settings
        switchItem(
            title = stringResource(R.string.aod_show_clock),
            checked = state.aodShowClock,
            onCheckedChange = actions.onAodShowClockChange
        )

        if (state.aodShowClock) {
            sliderItem(
                title = stringResource(R.string.aod_font_size_clock),
                description = stringResource(
                    R.string.battery_percentage,
                    ((state.aodFontSizeClock - Constants.CLOCK_SIZE_MIN.toInt()) * 100 / (Constants.CLOCK_SIZE_MAX - Constants.CLOCK_SIZE_MIN).toInt())
                ),
                value = state.aodFontSizeClock,
                onValueChange = actions.onAodFontSizeClockChange,
                range = Constants.CLOCK_SIZE_MIN..Constants.CLOCK_SIZE_MAX
            )

            switchItem(
                title = stringResource(R.string.aod_24h_format),
                checked = state.aod24hFormat,
                onCheckedChange = actions.onAod24hFormatChange
            )
        }

        // Date Settings
        switchItem(
            title = stringResource(R.string.aod_show_date),
            checked = state.aodShowDate,
            onCheckedChange = actions.onAodShowDateChange
        )

        if (state.aodShowDate) {
            sliderItem(
                title = stringResource(R.string.aod_font_size_date),
                description = stringResource(
                    R.string.battery_percentage,
                    ((state.aodFontSizeDate - Constants.DATE_SIZE_MIN.toInt()) * 100 / (Constants.DATE_SIZE_MAX - Constants.DATE_SIZE_MIN).toInt())
                ),
                value = state.aodFontSizeDate,
                onValueChange = actions.onAodFontSizeDateChange,
                range = Constants.DATE_SIZE_MIN..Constants.DATE_SIZE_MAX
            )
        }

        // Advanced Options
        switchItem(
            title = stringResource(R.string.aod_show_shortcuts),
            description = stringResource(R.string.aod_show_shortcuts_desc),
            checked = state.aodShowShortcuts,
            onCheckedChange = actions.onAodShowShortcutsChange
        )

        sliderItem(
            title = stringResource(R.string.aod_dim_amount),
            description = stringResource(R.string.battery_percentage, state.aodDimAmount),
            value = state.aodDimAmount,
            onValueChange = actions.onAodDimAmountChange,
            range = 0f..Constants.DIM_MAX
        )
    }
}
