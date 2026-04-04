package com.monospace.battery.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.monospace.battery.R

@Composable
fun HealthCard(
    health: Int,
    modifier: Modifier = Modifier
) {
    val healthColor = getHealthColor(health)
    val healthBgColor = getHealthBgColor(health)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = healthBgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(getHealthIcon(health)),
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                colorFilter = ColorFilter.tint(healthColor)
            )
            Text(
                text = stringResource(getHealthStatusRes(health)),
                fontFamily = FontFamily(Font(R.font.n_type82_headline)),
                fontSize = MaterialTheme.typography.displaySmall.fontSize,
                color = healthColor
            )
            Text(
                text = stringResource(R.string.battery_health).uppercase(),
                fontFamily = FontFamily(Font(R.font.azeret_mono_light)),
                fontSize = MaterialTheme.typography.labelSmall.fontSize,
                color = healthColor
            )
        }
    }
}
