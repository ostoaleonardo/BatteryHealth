package com.monospace.battery.ui.components.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monospace.battery.R
import com.monospace.battery.core.utils.BatteryStrings
import com.monospace.battery.data.models.ChargerStats
import com.monospace.battery.data.models.LocalSettingsActions
import com.monospace.battery.data.models.LocalSettingsState
import com.monospace.battery.ui.components.common.ClickableTooltip
import com.monospace.battery.ui.components.common.section.BaseItem
import com.monospace.battery.ui.components.common.section.Section
import com.monospace.battery.ui.theme.Font

@Composable
fun ChargerAnalysisSection(
    stats: List<ChargerStats>
) {
    val isPremium = LocalSettingsState.current.isWidgetsPurchased
    val onUpgradeClick = LocalSettingsActions.current.onUnlockClick
    val displayedStats = if (isPremium) stats else stats.take(1)

    Section(stringResource(R.string.charger_analysis_title)) {
        if (stats.isEmpty()) {
            customItem { BaseItem(description = stringResource(R.string.charger_analysis_no_data)) }
        } else {
            displayedStats.forEach { stat ->
                customItem { ChargerStatItem(stat = stat, isLocked = !isPremium) }
            }

            if (!isPremium) {
                customItem {
                    BaseItem(
                        title = stringResource(R.string.history_unlock_full),
                        description = stringResource(R.string.charger_analysis_upsell_desc),
                        onClick = onUpgradeClick,
                        trailingContent = { Icon(imageVector = Icons.Default.Lock, contentDescription = null) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ChargerStatItem(
    stat: ChargerStats,
    isLocked: Boolean
) {
    val locale = LocalLocale.current.platformLocale
    val batteryStrings = BatteryStrings()
    val sourceName = stringResource(batteryStrings.getChargingSource(stat.source))
    val sourceIcon = batteryStrings.getChargingSourceIcon(stat.source)

    BaseItem(
        title = sourceName,
        description = "${stat.sessionCount} ${stringResource(R.string.charger_analysis_sessions)}",
        horizontal = isLocked,
        leadingContent = { IconBadge(painterResource(id = sourceIcon)) },
        trailingContent = {
            Text(
                text = java.lang.String.format(locale, "%.2f %%/m", stat.averageRate),
                fontFamily = Font.AzeretMonoLight,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    ) {
        if (!isLocked) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    label = stringResource(R.string.charger_analysis_stability),
                    value = java.lang.String.format(locale, "%.0f%%", stat.stability * 100),
                    tooltip = stringResource(R.string.charger_analysis_stability_desc)
                )
                StatItem(
                    label = stringResource(R.string.charger_analysis_avg_temp),
                    value = java.lang.String.format(locale, "%.1f°C", stat.averageTemp),
                    tooltip = stringResource(R.string.charger_analysis_avg_temp_desc)
                )
                StatItem(
                    label = stringResource(R.string.charger_analysis_avg_speed),
                    value = getSpeedLabel(stat.averageRate),
                    tooltip = stringResource(R.string.charger_analysis_avg_speed_desc)
                )
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, tooltip: String) {
    ClickableTooltip(tooltipText = tooltip) {
        Column {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 8.sp,
                fontFamily = Font.AzeretMonoLight
            )
            Text(
                text = value,
                fontFamily = Font.AzeretMonoLight,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun getSpeedLabel(rate: Float): String {
    return when {
        rate > 1.2f -> stringResource(R.string.charger_quality_fast)
        rate > 0.6f -> stringResource(R.string.charger_quality_normal)
        else -> stringResource(R.string.charger_quality_slow)
    }
}
