package com.monospace.battery.ui.components.history

import android.text.format.DateUtils
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.monospace.battery.R
import com.monospace.battery.core.constants.Constants
import com.monospace.battery.core.utils.BatteryStrings
import com.monospace.battery.data.models.ChargeSession
import com.monospace.battery.ui.components.common.section.BaseItem
import com.monospace.battery.ui.theme.Font
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun ChargeSessionItem(
    session: ChargeSession,
    currentLevel: Int
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

    BaseItem(
        title = stringResource(R.string.history_charge_duration_title, sourceName),
        description = stringResource(
            R.string.history_session_time_format,
            displayDate,
            startTime,
            endTimeFormatted
        ),
        leadingContent = { IconBadge(painterResource(id = sourceIcon)) },
        trailingContent = {
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
    )
}
