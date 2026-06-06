package com.monospace.battery.ui.components.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource

data class DialogData(
    val title: String,
    val value: String,
    val description: String,
    val iconRes: Int,
    val iconTint: Color
)

val LocalIconTint = staticCompositionLocalOf<Color> { error("No icon tint provided") }
val LocalHealthColor = staticCompositionLocalOf<Color> { error("No health color provided") }
val LocalOnShowDialog = staticCompositionLocalOf<(DialogData) -> Unit> { {} }

@Composable
fun rememberBatteryDialogData(
    titleRes: Int,
    value: Any,
    descriptionRes: Int,
    iconRes: Int,
    iconTint: Color = LocalIconTint.current
): DialogData {
    val title = stringResource(titleRes)
    val description = stringResource(descriptionRes)

    val valueText = when (value) {
        is Int -> stringResource(value)
        else -> value.toString()
    }

    return remember(title, valueText, description, iconRes, iconTint) {
        DialogData(title, valueText, description, iconRes, iconTint)
    }
}
