package com.monospace.battery.ui.components.history

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.monospace.battery.R
import com.monospace.battery.core.constants.Constants
import com.monospace.battery.data.models.BatteryTip
import com.monospace.battery.ui.components.common.section.BaseItem
import com.monospace.battery.ui.components.common.section.Section

@Composable
fun BatteryTipsSection(
    tip: BatteryTip,
    onNextTip: () -> Unit
) {
    Section(
        title = stringResource(R.string.tips_title),
        trailingContent = {
            IconButton(
                onClick = onNextTip,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    modifier = Modifier.size(16.dp),
                    imageVector = Icons.Default.Refresh,
                    contentDescription = stringResource(R.string.action_refresh),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    ) {
        customItem {
            AnimatedContent(
                targetState = tip,
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
                label = Constants.TIP_ANIMATION_LABEL
            ) { currentTip ->
                TipCard(currentTip)
            }
        }
    }
}

@Composable
private fun TipCard(tip: BatteryTip) {
    BaseItem(
        title = stringResource(tip.titleRes),
        description = stringResource(tip.descriptionRes),
        leadingContent = { IconBadge(rememberVectorPainter(tip.iconVector)) }
    )
}
