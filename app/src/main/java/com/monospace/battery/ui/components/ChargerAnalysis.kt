package com.monospace.battery.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monospace.battery.R
import com.monospace.battery.core.utils.BatteryStrings
import com.monospace.battery.data.models.ChargerStats
import com.monospace.battery.ui.theme.Font

@Composable
fun ChargerAnalysisSection(
    stats: List<ChargerStats>,
    isPremium: Boolean,
    onUpgradeClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionTitle(stringResource(R.string.charger_analysis_title))

        if (stats.isEmpty()) {
            Text(
                text = stringResource(R.string.charger_analysis_no_data),
                modifier = Modifier.padding(24.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = Font.AzeretMonoLight
            )
        } else {
            val displayedStats = if (isPremium) stats else stats.take(1)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column {
                    displayedStats.forEachIndexed { index, stat ->
                        ChargerStatItem(
                            stat = stat,
                            isLocked = !isPremium,
                            showDivider = index < displayedStats.size - 1
                        )
                    }

                    if (!isPremium) {
                        UpsellCard(
                            title = stringResource(R.string.history_unlock_full),
                            description = stringResource(R.string.charger_analysis_upsell_desc),
                            onClick = onUpgradeClick,
                            shape = RoundedCornerShape(
                                bottomStart = 28.dp,
                                bottomEnd = 28.dp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChargerStatItem(
    stat: ChargerStats,
    isLocked: Boolean,
    showDivider: Boolean
) {
    val locale = LocalLocale.current.platformLocale
    val batteryStrings = BatteryStrings()
    val sourceName = stringResource(batteryStrings.getChargingSource(stat.source))
    val sourceIcon = batteryStrings.getChargingSourceIcon(stat.source)

    Column {
        // Header Row with subtle background
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = sourceIcon),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = sourceName.uppercase(),
                    fontFamily = Font.AzeretMonoLight,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${stat.sessionCount} ${stringResource(R.string.charger_analysis_sessions)}",
                    fontFamily = Font.AzeretMonoLight,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = java.lang.String.format(locale, "%.2f %%/m", stat.averageRate),
                fontFamily = Font.AzeretMonoLight,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Stats Row (only if not locked)
        if (!isLocked) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
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

        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
            )
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
