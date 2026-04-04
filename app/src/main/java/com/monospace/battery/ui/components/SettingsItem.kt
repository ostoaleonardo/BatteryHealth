package com.monospace.battery.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.monospace.battery.R
import com.monospace.battery.ui.theme.BatteryTheme

@Composable
fun SettingsItem(
    title: String,
    description: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
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
        Icon(
            painter = painterResource(id = R.drawable.open_in_new),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
    }
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
