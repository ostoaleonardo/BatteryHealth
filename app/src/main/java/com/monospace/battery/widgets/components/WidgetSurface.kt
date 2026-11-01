package com.monospace.battery.widgets.components

import androidx.compose.runtime.Composable
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.LocalState
import androidx.glance.unit.ColorProvider
import com.monospace.battery.core.constants.Constants
import com.monospace.battery.core.constants.WidgetConstants
import com.monospace.battery.data.local.PreferenceManager
import com.monospace.battery.widgets.WidgetsUpdater

@Composable
fun widgetSurfaceColor(): ColorProvider {
    val context = LocalContext.current
    (LocalState.current as? Preferences)?.get(WidgetsUpdater.REFRESH_KEY)

    val surface = GlanceTheme.colors.surface
    val prefs = PreferenceManager(context)
    val glass = prefs.get(Constants.PREFS_ALERTS, Constants.KEY_WIDGET_GLASS, false)
    val alpha = prefs.get(
        Constants.PREFS_ALERTS,
        Constants.KEY_WIDGET_GLASS_ALPHA,
        WidgetConstants.GLASS_ALPHA_DEFAULT
    )

    if (!glass) return surface

    val glassAlpha = alpha / 100f
    return widgetColor(
        surface,
        { it.surface.copy(alpha = glassAlpha) },
        { it.surface.copy(alpha = glassAlpha) }
    )
}
