package com.monospace.battery.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.monospace.battery.R
import com.monospace.battery.ui.theme.Font

enum class SettingsItemPosition {
    TOP, MIDDLE, BOTTOM, SINGLE
}

@Composable
inline fun SettingsSection(
    title: String? = null,
    content: SettingsSectionScope.() -> Unit
) {
    val scope = SettingsSectionScope()
    scope.content()

    val items = scope.getItems()
    if (items.isEmpty()) return

    Column {
        if (title != null) {
            SectionTitle(title)
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items.forEachIndexed { index, item ->
                val position = when {
                    items.size == 1 -> SettingsItemPosition.SINGLE
                    index == 0 -> SettingsItemPosition.TOP
                    index == items.size - 1 -> SettingsItemPosition.BOTTOM
                    else -> SettingsItemPosition.MIDDLE
                }

                item(position)
            }
        }
    }
}

class SettingsSectionScope {
    private val items = mutableListOf<@Composable (SettingsItemPosition) -> Unit>()

    fun item(
        title: String,
        description: String? = null,
        enabled: Boolean = true,
        iconRes: Int = R.drawable.open_in_new,
        onClick: () -> Unit
    ) {
        items.add { position ->
            SettingsItem(title, description, position, enabled, iconRes, onClick)
        }
    }

    fun switchItem(
        title: String,
        description: String? = null,
        checked: Boolean,
        enabled: Boolean = true,
        onCheckedChange: (Boolean) -> Unit
    ) {
        items.add { position ->
            SettingsSwitchItem(title, description, checked, position, enabled, onCheckedChange)
        }
    }

    fun sliderItem(
        title: String? = null,
        description: String,
        value: Int,
        onValueChange: (Int) -> Unit,
        enabled: Boolean = true,
        range: ClosedFloatingPointRange<Float> = 0f..100f
    ) {
        items.add { position ->
            SettingsSliderItem(
                title = title,
                description = description,
                value = value,
                onValueChange = onValueChange,
                position = position,
                enabled = enabled,
                range = range
            )
        }
    }

    fun getItems() = items
}

@Composable
fun SettingsBaseItem(
    title: String? = null,
    description: String? = null,
    position: SettingsItemPosition = SettingsItemPosition.SINGLE,
    horizontal: Boolean = true,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit = {}
) {
    val shape = when (position) {
        SettingsItemPosition.TOP -> RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        SettingsItemPosition.BOTTOM -> RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
        SettingsItemPosition.MIDDLE -> RoundedCornerShape(0.dp)
        SettingsItemPosition.SINGLE -> RoundedCornerShape(16.dp)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        onClick = { if (enabled) onClick?.invoke() },
        enabled = onClick != null && enabled
    ) {
        val containerModifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .alpha(if (enabled) 1f else 0.4f)

        if (horizontal) {
            Row(
                modifier = containerModifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SettingsItemText(title, description, Modifier.weight(1f))
                Spacer(modifier = Modifier.width(16.dp))
                content()
            }
        } else {
            Column(modifier = containerModifier) {
                SettingsItemText(title, description)
                content()
            }
        }
    }
}

@Composable
private fun SettingsItemText(
    title: String?,
    description: String?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        if (title != null) {
            Text(
                text = title.uppercase(),
                fontFamily = Font.AzeretMonoLight,
                style = MaterialTheme.typography.titleSmall
            )
        }
        if (description != null) {
            Text(
                text = description,
                fontFamily = Font.AzeretMonoLight,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.alpha(0.6f)
            )
        }
    }
}

@Composable
fun SettingsItem(
    title: String,
    description: String? = null,
    position: SettingsItemPosition = SettingsItemPosition.SINGLE,
    enabled: Boolean = true,
    iconRes: Int = R.drawable.open_in_new,
    onClick: () -> Unit
) {
    SettingsBaseItem(
        title = title,
        description = description,
        position = position,
        enabled = enabled,
        onClick = onClick
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun SettingsSwitchItem(
    title: String,
    description: String? = null,
    checked: Boolean,
    position: SettingsItemPosition = SettingsItemPosition.SINGLE,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    SettingsBaseItem(
        title = title,
        description = description,
        position = position,
        enabled = enabled,
        onClick = { onCheckedChange(!checked) }
    ) {
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}

@Composable
fun SettingsSliderItem(
    title: String? = null,
    description: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    position: SettingsItemPosition = SettingsItemPosition.SINGLE,
    enabled: Boolean = true,
    range: ClosedFloatingPointRange<Float> = 0f..100f
) {
    SettingsBaseItem(
        title = title,
        description = description,
        position = position,
        enabled = enabled,
        horizontal = false
    ) {
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = range,
            modifier = Modifier.padding(top = 8.dp),
            enabled = enabled
        )
    }
}
