package com.monospace.battery.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.monospace.battery.R
import com.monospace.battery.ui.theme.BatteryTheme

@Composable
fun SettingsBaseItem(
    title: String,
    description: String? = null,
    horizontal: Boolean = true,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit = {}
) {
    val modifier = Modifier
        .fillMaxWidth()
        .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
        .padding(horizontal = 24.dp, vertical = 16.dp)

    if (horizontal) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SettingsItemText(title, description, Modifier.weight(1f))
            Spacer(modifier = Modifier.width(16.dp))
            content()
        }
    } else {
        Column(modifier = modifier) {
            SettingsItemText(title, description)
            content()
        }
    }
}

@Composable
private fun SettingsItemText(
    title: String,
    description: String?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title.uppercase(),
            fontFamily = FontFamily(Font(R.font.azeret_mono_light)),
            style = MaterialTheme.typography.titleSmall
        )
        if (description != null) {
            Text(
                text = description,
                fontFamily = FontFamily(Font(R.font.azeret_mono_light)),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.alpha(0.6f)
            )
        }
    }
}

@Composable
fun SettingsItem(
    title: String,
    description: String? = null,
    onClick: () -> Unit
) {
    SettingsBaseItem(
        title = title,
        description = description,
        onClick = onClick
    ) {
        Icon(
            painter = painterResource(id = R.drawable.open_in_new),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun SettingsSwitchItem(
    title: String,
    description: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    SettingsBaseItem(
        title = title,
        description = description,
        onClick = { onCheckedChange(!checked) }
    ) {
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun SettingsSliderItem(
    title: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    range: ClosedFloatingPointRange<Float> = 0f..100f
) {
    SettingsBaseItem(
        title = title,
        description = stringResource(R.string.settings_healthy_charge_level, value),
        horizontal = false
    ) {
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = range,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title.uppercase(),
        fontFamily = FontFamily(Font(R.font.azeret_mono_light)),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp, bottom = 8.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun SettingsItemPreview() {
    BatteryTheme {
        SettingsItem(
            title = "Unlock Full Version",
            description = "Get access to all widgets",
            onClick = {}
        )
    }
}
