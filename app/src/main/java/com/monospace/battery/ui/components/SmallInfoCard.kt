package com.monospace.battery.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.monospace.battery.R

@Composable
fun SmallInfoCard(
    title: String,
    value: String,
    iconRes: Int,
    modifier: Modifier = Modifier,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .clip(MaterialTheme.shapes.extraLarge)
            .clickable { onClick() },
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = 0.3f
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title.uppercase(),
                fontFamily = FontFamily(Font(R.font.azeret_mono_light)),
                fontSize = MaterialTheme.typography.labelSmall.fontSize,
                maxLines = 1
            )
            Text(
                text = value,
                fontFamily = FontFamily(Font(R.font.n_type82_headline)),
                fontSize = MaterialTheme.typography.titleLarge.fontSize,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
            Image(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .size(32.dp),
                colorFilter = ColorFilter.tint(iconTint)
            )
        }
    }
}
