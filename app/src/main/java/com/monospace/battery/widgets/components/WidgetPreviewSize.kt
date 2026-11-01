package com.monospace.battery.widgets.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.LocalSize

val LocalWidgetPreview = compositionLocalOf { false }

@Composable
fun WidgetPreviewSize(widthDp: Int, heightDp: Int, content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalSize provides DpSize(widthDp.dp, heightDp.dp),
        LocalWidgetPreview provides true,
        content = content
    )
}
