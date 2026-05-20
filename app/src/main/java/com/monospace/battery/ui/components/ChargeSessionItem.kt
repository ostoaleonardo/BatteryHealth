package com.monospace.battery.ui.components

import android.os.BatteryManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.monospace.battery.R
import com.monospace.battery.core.utils.BatteryStrings
import com.monospace.battery.data.models.ChargeSession
import com.monospace.battery.ui.theme.BatteryTheme
import com.monospace.battery.ui.theme.Font
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun ChargeSessionItem(
    session: ChargeSession,
    showDivider: Boolean,
    currentLevel: Int? = null
) {
    val dateFormat = SimpleDateFormat("dd MMM, HH:mm", LocalLocale.current.platformLocale)
    val startTime = dateFormat.format(Date(session.startTime))

    val isCurrentlyCharging = session.endTime == null
    val batteryStrings = BatteryStrings()
    val sourceRes = batteryStrings.getChargingSource(session.chargeSource)
    val sourceIcon = batteryStrings.getChargingSourceIcon(session.chargeSource)
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

    val gained = if (isCurrentlyCharging && currentLevel != null) {
        currentLevel - session.startLevel
    } else {
        (session.endLevel ?: session.startLevel) - session.startLevel
    }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
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

            SessionInfo(
                title = titleText,
                subtitle = "$startTime • $sourceStr"
            )

            GainedBadge(gained, isCurrentlyCharging)
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

@Composable
private fun RowScope.SessionInfo(
    title: String,
    subtitle: String
) {
    Column(modifier = Modifier.weight(1f)) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.titleSmall,
            fontFamily = Font.AzeretMonoLight
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = Font.AzeretMonoLight
        )
    }
}

@Composable
private fun GainedBadge(
    gained: Int,
    isCurrentlyCharging: Boolean
) {
    val isPositive = gained >= 0
    val color =
        if (isPositive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error

    val icon = when {
        isCurrentlyCharging -> R.drawable.bolt_fill
        isPositive -> R.drawable.arrow_drop_up
        else -> R.drawable.arrow_drop_down
    }

    val text = if (isPositive) "+$gained%" else "$gained%"

    val iconSize = if (isCurrentlyCharging) 14.dp else 24.dp

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = color,
            fontFamily = Font.AzeretMonoLight
        )
        Box(
            modifier = Modifier.size(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChargeSessionItemPreview() {
    BatteryTheme {
        ChargeSessionItem(
            session = ChargeSession(
                startTime = System.currentTimeMillis() - 3600000,
                endTime = System.currentTimeMillis(),
                startLevel = 45,
                endLevel = 80,
                chargeSource = BatteryManager.BATTERY_PLUGGED_AC
            ),
            showDivider = true
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CurrentlyChargingSessionItemPreview() {
    BatteryTheme {
        ChargeSessionItem(
            session = ChargeSession(
                startTime = System.currentTimeMillis() - 1800000,
                endTime = null,
                startLevel = 20,
                endLevel = null,
                chargeSource = BatteryManager.BATTERY_PLUGGED_USB
            ),
            showDivider = false
        )
    }
}
