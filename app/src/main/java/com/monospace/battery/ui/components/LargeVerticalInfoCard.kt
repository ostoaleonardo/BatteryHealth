package com.monospace.battery.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.monospace.battery.R

@Composable
fun LargeVerticalInfoCard(
    title: String,
    value: String,
    iconRes: Int,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 24.dp, horizontal = 16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title.uppercase(),
                fontFamily = FontFamily(Font(R.font.azeret_mono_light)),
                fontSize = MaterialTheme.typography.labelSmall.fontSize,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontFamily = FontFamily(Font(R.font.n_type82_headline)),
                fontSize = MaterialTheme.typography.displaySmall.fontSize,
                textAlign = TextAlign.Center
            )
            Image(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier
                    .padding(top = 32.dp)
                    .size(72.dp),
                colorFilter = ColorFilter.tint(iconTint)
            )
        }
    }
}

@Composable
fun LargeHorizontalInfoCard(
    title: String,
    value: String,
    iconRes: Int,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 24.dp, horizontal = 16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .size(64.dp),
                colorFilter = ColorFilter.tint(iconTint)
            )
            Text(
                text = value,
                fontFamily = FontFamily(Font(R.font.n_type82_headline)),
                fontSize = MaterialTheme.typography.displaySmall.fontSize,
                textAlign = TextAlign.Center
            )
            Text(
                text = title.uppercase(),
                fontFamily = FontFamily(Font(R.font.azeret_mono_light)),
                fontSize = MaterialTheme.typography.labelSmall.fontSize,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
