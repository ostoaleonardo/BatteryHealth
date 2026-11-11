package com.monospace.battery.ui.components.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monospace.battery.R
import com.monospace.battery.core.constants.WidgetConstants
import com.monospace.battery.data.models.LocalSettingsActions
import com.monospace.battery.data.models.LocalSettingsState
import com.monospace.battery.ui.components.common.StyleOption
import com.monospace.battery.ui.components.common.section.BaseItem
import com.monospace.battery.ui.components.common.section.Section
import com.monospace.battery.ui.theme.Font

@Composable
fun AppearanceSection() {
    val state = LocalSettingsState.current
    val actions = LocalSettingsActions.current
    val labels = listOf(R.string.settings_font_1, R.string.settings_font_2)

    Section(stringResource(R.string.settings_appearance)) {
        customItem {
            BaseItem(
                title = stringResource(R.string.settings_heading_font),
                description = stringResource(R.string.settings_heading_font_description),
                horizontal = false
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    labels.forEachIndexed { index, label ->
                        val selected = state.headingFont == index

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StyleOption(
                                selected = selected,
                                onClick = { actions.onHeadingFontChange(index) }
                            ) {
                                Text(
                                    text = "Aa",
                                    fontSize = 28.sp,
                                    fontFamily = Font.getHeadingFont(index),
                                    color = if (selected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }

                            Text(
                                text = stringResource(label),
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = Font.AzeretMonoLight
                            )
                        }
                    }
                }
            }
        }

        switchItem(
            title = stringResource(R.string.settings_widget_glass),
            description = stringResource(R.string.settings_widget_glass_description),
            checked = state.widgetGlass,
            enabled = state.isWidgetsPurchased,
            onCheckedChange = actions.onWidgetGlassChange
        )

        if (state.widgetGlass) {
            sliderItem(
                description = stringResource(R.string.battery_percentage, state.widgetGlassAlpha),
                value = state.widgetGlassAlpha,
                onValueChange = actions.onWidgetGlassAlphaChange,
                enabled = state.isWidgetsPurchased,
                range = WidgetConstants.GLASS_ALPHA_MIN..WidgetConstants.GLASS_ALPHA_MAX
            )
        }
    }
}
