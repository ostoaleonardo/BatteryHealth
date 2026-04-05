package com.monospace.battery.widgets.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceTheme
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.text.FontFamily
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle

@Composable
fun WidgetValueLabel(
    value: String,
    label: String,
    valueFontSize: TextUnit = 32.sp
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = TextStyle(
                fontSize = valueFontSize,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                color = GlanceTheme.colors.onSurface
            )
        )
        Text(
            text = label.uppercase(),
            style = TextStyle(
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                fontFamily = FontFamily.Monospace,
                color = GlanceTheme.colors.onSurfaceVariant
            )
        )
    }
}
