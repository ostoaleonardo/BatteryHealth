package com.monospace.battery.widgets.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceTheme
import androidx.glance.layout.Column
import androidx.glance.text.FontFamily
import androidx.glance.text.Text
import androidx.glance.text.TextStyle

@Composable
fun WidgetInfoRow(label: String, value: String = "") {
    Column {
        Text(
            text = label.uppercase(),
            style = TextStyle(
                fontSize = 7.sp,
                fontFamily = FontFamily.Monospace,
                color = GlanceTheme.colors.onSurfaceVariant
            )
        )

        if (value.isNotEmpty()) {
            WidgetHeadingText(
                text = value,
                fontSize = 14.sp,
                reservedWidthDp = ICON_RESERVED_WIDTH_DP
            )
        }
    }
}

private const val ICON_RESERVED_WIDTH_DP = 64f
