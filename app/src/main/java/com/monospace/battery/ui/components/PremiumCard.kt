package com.monospace.battery.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.monospace.battery.R

@Composable
fun PremiumCard(
    onUpgradeClick: () -> Unit
) {
    AlertCard(
        title = stringResource(R.string.premium_title),
        description = stringResource(R.string.premium_description),
        buttonText = stringResource(R.string.premium_button),
        onClick = onUpgradeClick,
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.crown),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    )
}
