package com.monospace.battery.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.monospace.battery.R
import com.monospace.battery.ui.theme.BatteryTheme

@Composable
fun BatteryDialog(
    onDismissRequest: () -> Unit,
    title: String,
    value: String,
    description: String,
    iconRes: Int,
    iconTint: Color
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Image(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    colorFilter = ColorFilter.tint(iconTint)
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = title.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = FontFamily(Font(R.font.azeret_mono_light))
                    )
                    Text(
                        text = value,
                        color = iconTint,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.headlineMedium,
                        fontFamily = FontFamily(Font(R.font.n_type82_headline)),
                    )
                }

                Text(
                    text = description,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = FontFamily(Font(R.font.azeret_mono_light))
                )

                Button(
                    onClick = onDismissRequest,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = iconTint.copy(alpha = 0.1f),
                        contentColor = iconTint
                    )
                ) {
                    Text(
                        text = stringResource(android.R.string.ok),
                        fontFamily = FontFamily(Font(R.font.azeret_mono_light))
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Composable
fun BatteryDialogLightPreview() {
    BatteryTheme {
        BatteryDialog(
            onDismissRequest = {},
            title = "Battery Health",
            value = "Good",
            description = "The battery is in optimal condition and functioning correctly within its normal parameters.",
            iconRes = R.drawable.bolt,
            iconTint = MaterialTheme.colorScheme.primary
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
fun BatteryDialogDarkPreview() {
    BatteryTheme {
        BatteryDialog(
            onDismissRequest = {},
            title = "Temperature",
            value = "32.0°C",
            description = "The battery temperature is normal. Excessive heat can reduce the lifespan of chemical components.",
            iconRes = R.drawable.thermometer,
            iconTint = Color(0xFF4CAF50)
        )
    }
}
