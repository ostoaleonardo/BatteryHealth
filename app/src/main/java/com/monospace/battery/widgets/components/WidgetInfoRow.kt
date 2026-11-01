package com.monospace.battery.widgets.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import androidx.glance.layout.Column

@Composable
fun WidgetInfoRow(label: String, value: String = "") {
    Column {
        WidgetLabelText(
            text = label.uppercase(),
            fontSize = 7.sp,
            reservedWidthDp = ICON_RESERVED_WIDTH_DP
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
