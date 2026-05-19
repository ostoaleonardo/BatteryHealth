package com.monospace.battery.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(end = 16.dp)
        ) {
            SectionTitle(stringResource(R.string.charger_analysis_title))
            Spacer(modifier = Modifier.weight(1f))

            var showInfo by remember { mutableStateOf(false) }
            IconButton(onClick = { showInfo = true }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            if (showInfo) {
                AlertDialog(
                    onDismissRequest = { showInfo = false },
                    confirmButton = {
                        TextButton(onClick = { showInfo = false }) {
                            Text("OK", fontFamily = Font.AzeretMonoLight)
                        }
                    },
                    title = {
                        Text(
                            text = stringResource(R.string.charger_analysis_what_measured_title),
                            fontFamily = Font.AzeretMonoLight,
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    text = {
                        Text(
                            text = stringResource(R.string.charger_analysis_what_measured_desc),
                            fontFamily = Font.AzeretMonoLight,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    shape = RoundedCornerShape(28.dp)
                )
            }
        }

        if (!isPremium) {
            AlertCard(
                title = stringResource(R.string.premium_title),
                description = stringResource(R.string.history_unlock_full_desc),
                buttonText = stringResource(R.string.premium_button),
                onClick = onUpgradeClick,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            )
        } else if (stats.isEmpty()) {
            Card(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                        alpha = 0.3f
                    )
                )
            ) {
                Text(
                    text = stringResource(R.string.history_collecting_data),
                    modifier = Modifier.padding(24.dp),
                    fontFamily = Font.AzeretMonoLight,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            ChargerStatsList(stats)
        }
    }
}

@Composable
private fun ChargerStatsList(stats: List<ChargerStats>) {
    val locale = LocalLocale.current.platformLocale
    val batteryStrings = BatteryStrings()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        stats.forEach { stat ->
            val sourceName = stringResource(batteryStrings.getChargingSource(stat.source))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
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

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatItem(
                            label = stringResource(R.string.charger_analysis_stability),
                            value = java.lang.String.format(locale, "%.0f%%", stat.stability * 100)
                        )
                        StatItem(
                            label = stringResource(R.string.charger_analysis_avg_temp),
                            value = java.lang.String.format(locale, "%.1f°C", stat.averageTemp)
                        )
                        StatItem(
                            label = stringResource(R.string.charger_analysis_avg_speed),
                            value = getSpeedLabel(stat.averageRate)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
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

@Composable
private fun getSpeedLabel(rate: Float): String {
    return when {
        rate > 1.2f -> stringResource(R.string.charger_quality_fast)
        rate > 0.6f -> stringResource(R.string.charger_quality_normal)
        else -> stringResource(R.string.charger_quality_slow)
    }
}
