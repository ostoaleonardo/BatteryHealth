package com.monospace.battery.widgets.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceTheme
import androidx.glance.layout.Column
import androidx.glance.text.FontFamily
import androidx.glance.text.FontWeight
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
            Text(
                text = value,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = GlanceTheme.colors.onSurface
                )
            )
        }
    }
}
