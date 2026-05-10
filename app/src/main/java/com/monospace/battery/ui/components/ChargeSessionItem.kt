package com.monospace.battery.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.monospace.battery.R
import com.monospace.battery.core.utils.BatteryStrings
import com.monospace.battery.data.models.ChargeSession
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun ChargeSessionItem(session: ChargeSession, showDivider: Boolean) {
    val dateFormat = SimpleDateFormat("dd MMM, HH:mm", LocalLocale.current.platformLocale)
    val startTime = dateFormat.format(Date(session.startTime))

    val isCurrentlyCharging = session.endTime == null
    val sourceRes = BatteryStrings().getChargingSource(session.chargeSource)
    val sourceStr = stringResource(sourceRes)

    val durationStr = if (session.endTime != null) {
        val diff = session.endTime - session.startTime
        val mins = (diff / 60_000) % 60
        val hrs = diff / 3_600_000
        if (hrs > 0) "${hrs}h ${mins}m" else "${mins}m"
    } else ""

    val titleText = if (isCurrentlyCharging) {
        stringResource(R.string.history_currently_charging)
    } else {
        stringResource(R.string.history_charge_duration_title, durationStr)
    }

    val gained = (session.endLevel ?: session.startLevel) - session.startLevel

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titleText,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "$startTime • $sourceStr",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                val displayGained = if (gained >= 0) "+$gained%" else "$gained%"
                Text(
                    text = displayGained,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (gained >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    fontFamily = FontFamily(Font(R.font.azeret_mono_light))
                )
                Icon(
                    painter = painterResource(
                        if (gained >= 0) R.drawable.arrow_drop_up else R.drawable.arrow_drop_down
                    ),
                    contentDescription = null,
                    tint = if (gained >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
            )
        }
    }
}
