package com.monospace.battery.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monospace.battery.R
import com.monospace.battery.ui.theme.Font

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SotCard(
    sot: String,
    batteryUsed: Int,
    drainRate: Float,
    estimatedFullSot: String
) {
    val locale = LocalLocale.current.platformLocale
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Main SOT Header
            ClickableTooltip(
                tooltipText = stringResource(R.string.sot_description)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.history_sot_title),
                            fontFamily = Font.AzeretMonoLight,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = sot,
                            fontFamily = Font.NType82,
                            style = MaterialTheme.typography.displayMedium,
                            letterSpacing = (-1).sp
                        )
                    }
                    Icon(
                        painter = painterResource(R.drawable.schedule),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Secondary Stats (Proportional 1/3 each)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatItem(
                    label = stringResource(R.string.sot_label_used),
                    value = "$batteryUsed%",
                    description = stringResource(R.string.sot_label_used_description)
                )
                StatItem(
                    label = stringResource(R.string.sot_label_drain),
                    value = String.format(locale, "%.1f%%", drainRate),
                    description = stringResource(R.string.sot_label_drain_description)
                )
                StatItem(
                    label = stringResource(R.string.sot_label_est_full),
                    value = estimatedFullSot,
                    description = stringResource(R.string.sot_label_est_full_description)
                )
            }
        }
    }
}

@Composable
private fun RowScope.StatItem(
    label: String,
    value: String,
    description: String
) {
    ClickableTooltip(
        tooltipText = description,
        modifier = Modifier.weight(1f)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = label,
                fontFamily = Font.AzeretMonoLight,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                fontSize = 9.sp
            )
            Text(
                text = value,
                fontFamily = Font.AzeretMonoLight,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
