package com.monospace.battery.widgets.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column

@Composable
fun WidgetValueLabel(
    value: String,
    label: String,
    valueFontSize: TextUnit = 32.sp,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally
) {
    Column(
        horizontalAlignment = horizontalAlignment
    ) {
        WidgetHeadingText(text = value, fontSize = valueFontSize)
        WidgetLabelText(text = label.uppercase(), fontSize = 10.sp)
    }
}
