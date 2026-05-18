package com.monospace.battery.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monospace.battery.R
import com.monospace.battery.core.utils.BatteryStrings
import com.monospace.battery.data.models.ChargeSession
import com.monospace.battery.ui.theme.Font

@Composable
fun ChargerAnalysisSection(
    sessions: List<ChargeSession>,
    isPremium: Boolean,
    onUpgradeClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionTitle(stringResource(R.string.charger_analysis_title))
        
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
        } else if (sessions.filter { it.endTime != null }.size < 2) {
            Card(
                modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Text(
                    text = stringResource(R.string.history_collecting_data),
                    modifier = Modifier.padding(24.dp),
                    fontFamily = Font.AzeretMonoLight,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            ChargerStatsList(sessions)
        }
    }
}

@Composable
private fun ChargerStatsList(sessions: List<ChargeSession>) {
    val locale = LocalLocale.current.platformLocale
    val grouped = sessions.filter { it.endTime != null && it.endLevel != null }
        .groupBy { it.chargeSource }
        
    val batteryStrings = BatteryStrings()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        grouped.forEach { (source, sessionList) ->
            val sourceRes = batteryStrings.getChargingSource(source)
            val sourceName = stringResource(sourceRes)
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = sourceName.uppercase(),
                            fontFamily = Font.AzeretMonoLight,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${sessionList.size} ${stringResource(R.string.charger_analysis_sessions)}",
                            fontFamily = Font.AzeretMonoLight,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    var totalGain = 0
                    var totalTimeMinutes = 0L
                    sessionList.forEach {
                        totalGain += (it.endLevel!! - it.startLevel)
                        totalTimeMinutes += ((it.endTime!! - it.startTime) / 60000).coerceAtLeast(1L)
                    }
                    
                    val rate = if (totalTimeMinutes > 0) totalGain.toFloat() / totalTimeMinutes.toFloat() else 0f
                    
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = stringResource(R.string.charger_analysis_avg_speed),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 8.sp
                        )
                        Text(
                            text = java.lang.String.format(locale, "%.2f %%/m", rate),
                            fontFamily = Font.AzeretMonoLight,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
