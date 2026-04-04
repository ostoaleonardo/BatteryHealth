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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monospace.battery.R
import com.monospace.battery.ui.theme.BatteryTheme

@Composable
fun WidgetsPurchaseContent(
    onBuyClick: () -> Unit,
    onRestoreClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.widget_purchase_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.widget_purchase_description).uppercase(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(vertical = 24.dp),
            textAlign = TextAlign.Center,
        )
        Button(
            onClick = onBuyClick,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 320.dp)
        ) {
            Text(
                text = stringResource(R.string.widget_purchase_unlock_full).uppercase(),
                style = MaterialTheme.typography.labelLarge
            )
        }
        TextButton(
            onClick = onRestoreClick,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 320.dp)
        ) {
            Text(
                text = stringResource(R.string.widget_purchase_restore_purchases).uppercase(),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
fun CompleteWidgetsPurchaseContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.widget_purchase_complete_purchase),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Image(
            painter = painterResource(R.drawable.task),
            contentDescription = null,
            modifier = Modifier
                .padding(vertical = 32.dp)
                .size(64.dp),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
        )
        Text(
            text = stringResource(R.string.widget_purchase_complete_purchase_description).uppercase(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
fun WidgetsPurchaseContentPreview() {
    BatteryTheme {
        Surface {
            WidgetsPurchaseContent(onBuyClick = {}, onRestoreClick = {})
        }
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
fun CompleteWidgetsPurchaseContentPreview() {
    BatteryTheme {
        Surface {
            CompleteWidgetsPurchaseContent()
        }
    }
}