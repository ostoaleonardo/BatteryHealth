package com.monospace.battery.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monospace.battery.R
import com.monospace.battery.ui.theme.Font

data class BatteryTip(
    val title: String,
    val description: String,
    val icon: ImageVector
)

@Composable
fun BatteryTipsSection() {
    val tips = listOf(
        BatteryTip(
            title = stringResource(R.string.tips_20_80_rule),
            description = stringResource(R.string.tips_20_80_rule_desc),
            icon = Icons.Default.Info
        ),
        BatteryTip(
            title = stringResource(R.string.tips_heat_kill),
            description = stringResource(R.string.tips_heat_kill_desc),
            icon = Icons.Default.Warning
        ),
        BatteryTip(
            title = stringResource(R.string.tips_original_acc),
            description = stringResource(R.string.tips_original_acc_desc),
            icon = Icons.Default.Lightbulb
        )
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        SectionTitle(stringResource(R.string.tips_title))
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(tips) { tip ->
                TipCard(tip)
            }
        }
    }
}

@Composable
private fun TipCard(tip: BatteryTip) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .height(140.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = tip.icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = tip.title.uppercase(),
                    fontFamily = Font.AzeretMonoLight,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = tip.description,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = Font.AzeretMonoLight,
                lineHeight = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
