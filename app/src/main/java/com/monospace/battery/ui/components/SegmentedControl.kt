package com.monospace.battery.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import com.monospace.battery.ui.theme.Font

data class SegmentOption(val id: String, val label: Int)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SegmentedControl(
    options: List<SegmentOption>,
    selected: String,
    onSelected: (String) -> Unit
) {
    val selectedIndex = options.indexOfFirst { it.id == selected }.coerceAtLeast(0)

    Row(
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
    ) {
        options.forEachIndexed { index, option ->
            ToggleButton(
                checked = selectedIndex == index,
                onCheckedChange = { onSelected(option.id) },
                modifier = Modifier
                    .semantics { role = Role.RadioButton },
                shapes = when (index) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    options.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                }
            ) {
                Text(
                    text = stringResource(option.label),
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = Font.AzeretMonoLight
                )
            }
        }
    }
}
