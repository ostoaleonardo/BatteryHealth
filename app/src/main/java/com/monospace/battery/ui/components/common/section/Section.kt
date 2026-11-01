package com.monospace.battery.ui.components.common.section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.monospace.battery.R
import com.monospace.battery.ui.theme.Font

private class ItemPosition(val index: Int = 0, val count: Int = 1)

private val LocalItemPosition = compositionLocalOf { ItemPosition() }

@Composable
inline fun Section(
    title: String? = null,
    noinline trailingContent: (@Composable () -> Unit)? = null,
    content: SectionScope.() -> Unit
) {
    SectionList(title, trailingContent, SectionScope().apply(content).getItems())
}

@Composable
fun SectionList(
    title: String?,
    trailingContent: (@Composable () -> Unit)?,
    items: List<@Composable () -> Unit>
) {
    if (items.isEmpty()) return

    Column {
        if (title != null) {
            SectionTitle(
                title = title,
                trailingContent = trailingContent
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
        ) {
            items.forEachIndexed { index, item ->
                CompositionLocalProvider(
                    LocalItemPosition provides ItemPosition(
                        index,
                        items.size
                    )
                ) {
                    item()
                }
            }
        }
    }
}

class SectionScope {
    private val items = mutableListOf<@Composable () -> Unit>()

    fun item(
        title: String,
        description: String? = null,
        enabled: Boolean = true,
        iconRes: Int = R.drawable.open_in_new,
        onClick: () -> Unit
    ) {
        items.add {
            BaseItem(title, description, enabled = enabled, onClick = onClick) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }

    fun switchItem(
        title: String,
        description: String? = null,
        checked: Boolean,
        enabled: Boolean = true,
        onCheckedChange: (Boolean) -> Unit
    ) {
        items.add { SwitchItem(title, description, checked, enabled, onCheckedChange) }
    }

    fun sliderItem(
        title: String? = null,
        description: String,
        value: Int,
        onValueChange: (Int) -> Unit,
        enabled: Boolean = true,
        range: ClosedFloatingPointRange<Float> = 0f..100f
    ) {
        items.add {
            BaseItem(title, description, horizontal = false, enabled = enabled) {
                ItemSlider(value, onValueChange, enabled, range)
            }
        }
    }

    fun customItem(content: @Composable () -> Unit) {
        items.add(content)
    }

    fun getItems() = items
}

@Composable
private fun itemColors(): ListItemColors {
    val container = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)

    return ListItemDefaults.segmentedColors(
        containerColor = container,
        selectedContainerColor = container
    )
}

@Composable
private fun ItemTitle(title: String) {
    Text(
        text = title.uppercase(),
        fontFamily = Font.AzeretMonoLight,
        style = MaterialTheme.typography.titleSmall
    )
}

@Composable
private fun ItemDescription(description: String) {
    Text(
        text = description,
        fontFamily = Font.AzeretMonoLight,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.alpha(0.6f)
    )
}

private val itemModifier = Modifier
    .fillMaxWidth()
    .padding(horizontal = 16.dp)

private val itemPadding = PaddingValues(16.dp)

@Composable
fun BaseItem(
    title: String? = null,
    description: String? = null,
    horizontal: Boolean = true,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit = {}
) {
    val position = LocalItemPosition.current
    val shapes = ListItemDefaults.segmentedShapes(position.index, position.count)
    val label: @Composable () -> Unit = { if (title != null) ItemTitle(title) }

    val supporting: (@Composable () -> Unit)? = if (horizontal) {
        description?.let { { ItemDescription(it) } }
    } else {
        {
            Column {
                if (description != null) ItemDescription(description)
                content()
            }
        }
    }
    val trailing: (@Composable () -> Unit)? = trailingContent ?: if (horizontal) content else null
    val alignment = if (horizontal) Alignment.CenterVertically else Alignment.Top

    if (onClick != null) {
        SegmentedListItem(
            onClick = onClick,
            shapes = shapes,
            modifier = itemModifier,
            enabled = enabled,
            leadingContent = leadingContent,
            trailingContent = trailing,
            supportingContent = supporting,
            colors = itemColors(),
            verticalAlignment = alignment,
            contentPadding = itemPadding,
            content = label
        )
    } else {
        SegmentedListItem(
            shapes = shapes,
            modifier = itemModifier,
            enabled = enabled,
            leadingContent = leadingContent,
            trailingContent = trailing,
            supportingContent = supporting,
            colors = itemColors(),
            verticalAlignment = alignment,
            contentPadding = itemPadding,
            content = label
        )
    }
}

@Composable
private fun SwitchItem(
    title: String,
    description: String?,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val position = LocalItemPosition.current

    SegmentedListItem(
        checked = checked,
        onCheckedChange = onCheckedChange,
        shapes = ListItemDefaults.segmentedShapes(position.index, position.count),
        modifier = itemModifier,
        enabled = enabled,
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = null,
                enabled = enabled,
                thumbContent = if (checked) {
                    {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize)
                        )
                    }
                } else null
            )
        },
        supportingContent = description?.let { { ItemDescription(it) } },
        colors = itemColors(),
        verticalAlignment = Alignment.CenterVertically,
        contentPadding = itemPadding,
        content = { ItemTitle(title) }
    )
}

@Composable
private fun ItemSlider(
    value: Int,
    onValueChange: (Int) -> Unit,
    enabled: Boolean,
    range: ClosedFloatingPointRange<Float>
) {
    val sliderState = rememberSliderState(value.toFloat(), 0, range)

    LaunchedEffect(value) {
        if (sliderState.value != value.toFloat()) sliderState.value = value.toFloat()
    }

    Slider(
        state = sliderState,
        onValueChange = { onValueChange(it.toInt()) },
        modifier = Modifier.padding(top = 8.dp),
        enabled = enabled
    )
}
