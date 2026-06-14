package com.monospace.battery.ui.components.history

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monospace.battery.R
import com.monospace.battery.core.constants.Constants
import com.monospace.battery.core.utils.BatteryStrings
import com.monospace.battery.data.models.ChargeSession
import com.monospace.battery.ui.theme.Font
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun ChargeSessionItem(
    session: ChargeSession,
    currentLevel: Int,
    showDivider: Boolean
) {
    val locale = LocalLocale.current.platformLocale
    val batteryStrings = BatteryStrings()
    val sourceName = stringResource(batteryStrings.getChargingSource(session.chargeSource))
    val sourceIcon = batteryStrings.getChargingSourceIcon(session.chargeSource)

    val timeFormat = SimpleDateFormat(Constants.TIME_PATTERN_24H, locale)
    val dateFormat = SimpleDateFormat(Constants.DATE_PATTERN_SHORT, locale)

    val isToday = DateUtils.isToday(session.startTime)
    val displayDate = if (isToday) Constants.EMPTY_STRING
    else stringResource(
        R.string.history_session_date_format,
        dateFormat.format(Date(session.startTime))
    )

    val startTime = timeFormat.format(Date(session.startTime))
    val endTimeFormatted = session.endTime?.let { timeFormat.format(Date(it)) }
        ?: stringResource(R.string.history_currently_charging)

    val endLevel = session.endLevel ?: currentLevel
    val gain = (endLevel - session.startLevel).coerceAtLeast(0)

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Source Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = sourceIcon),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Session Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.history_charge_duration_title, sourceName),
                    fontFamily = Font.AzeretMonoLight,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(
                        R.string.history_session_time_format,
                        displayDate,
                        startTime,
                        endTimeFormatted
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = Font.AzeretMonoLight,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Level Gain
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stringResource(
                        R.string.battery_gain_format,
                        stringResource(R.string.battery_percentage, gain)
                    ),
                    fontFamily = Font.AzeretMonoLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(
                        R.string.battery_range_format,
                        stringResource(R.string.battery_percentage, session.startLevel),
                        stringResource(R.string.battery_percentage, endLevel)
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = Font.AzeretMonoLight,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
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
