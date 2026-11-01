package com.monospace.battery.widgets.components

import androidx.compose.runtime.Composable
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.currentState
import androidx.glance.unit.ColorProvider
import com.monospace.battery.core.constants.Constants
import com.monospace.battery.data.local.PreferenceManager
import com.monospace.battery.widgets.WidgetsUpdater

@Composable
fun widgetSurfaceColor(): ColorProvider {
    val context = LocalContext.current
    currentState<Preferences>()[WidgetsUpdater.REFRESH_KEY]

    val surface = GlanceTheme.colors.surface
    val prefs = PreferenceManager(context)
    val glass = prefs.get(Constants.PREFS_ALERTS, Constants.KEY_WIDGET_GLASS, false)
    val alpha = prefs.get(
        Constants.PREFS_ALERTS,
        Constants.KEY_WIDGET_GLASS_ALPHA,
        Constants.WIDGET_GLASS_ALPHA_DEFAULT
    )

    return if (glass) ColorProvider(surface.getColor(context).copy(alpha = alpha / 100f)) else surface
}
