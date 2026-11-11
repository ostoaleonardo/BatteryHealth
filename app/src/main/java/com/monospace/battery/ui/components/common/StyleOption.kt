package com.monospace.battery.ui.components.common

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.monospace.battery.ui.utils.selectionStyle

val StyleOptionSize = 64.dp
val StyleOptionSpacing = 12.dp

@Composable
fun StyleOption(
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
