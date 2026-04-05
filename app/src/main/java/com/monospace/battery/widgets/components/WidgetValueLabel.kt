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
    valueFontSize: TextUnit = 32.sp,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally
) {
    Column(
        horizontalAlignment = horizontalAlignment
    ) {
        Text(
            maxLines = 1,
            text = value,
            style = TextStyle(
                fontSize = valueFontSize,
                textAlign = when (horizontalAlignment) {
                    Alignment.Start -> TextAlign.Start
                    Alignment.End -> TextAlign.End
                    else -> TextAlign.Center
                },
                fontWeight = FontWeight.Medium,
                color = GlanceTheme.colors.onSurface
            )
        )
        Text(
            maxLines = 1,
            text = label.uppercase(),
            style = TextStyle(
                fontSize = 10.sp,
                textAlign = when (horizontalAlignment) {
                    Alignment.Start -> TextAlign.Start
                    Alignment.End -> TextAlign.End
                    else -> TextAlign.Center
                },
                fontFamily = FontFamily.Monospace,
                color = GlanceTheme.colors.onSurfaceVariant
            )
        )
    }
}
