package com.monospace.battery.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.monospace.battery.ui.theme.Font

@Composable
fun SectionTitle(
    title: String,
    top: Dp = 32.dp
) {
    Text(
        text = title.uppercase(),
        fontFamily = Font.AzeretMonoLight,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .padding(top = top, bottom = 8.dp)
    )
}
