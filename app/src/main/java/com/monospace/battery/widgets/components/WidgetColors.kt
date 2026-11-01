package com.monospace.battery.widgets.components

import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.color.ColorProvider as dayNightColorProvider
import androidx.glance.unit.ColorProvider
import com.monospace.battery.core.constants.WidgetConstants

@Composable
fun widgetColor(
    fallback: ColorProvider,
    light: (ColorScheme) -> Color,
    dark: (ColorScheme) -> Color
): ColorProvider {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return fallback

    val context = LocalContext.current
    return dayNightColorProvider(
        day = light(dynamicLightColorScheme(context)),
        night = dark(dynamicDarkColorScheme(context))
    )
}

@Composable
fun widgetAccentColor(): ColorProvider =
    widgetColor(GlanceTheme.colors.primary, { it.primary }, { it.primary })

@Composable
fun widgetTextColor(): ColorProvider =
    widgetColor(GlanceTheme.colors.onSurface, { WidgetConstants.TEXT_PRIMARY_LIGHT }, { it.onSurface })

@Composable
fun widgetSecondaryTextColor(): ColorProvider =
    widgetColor(
        GlanceTheme.colors.onSurfaceVariant,
        { WidgetConstants.TEXT_SECONDARY_LIGHT },
        { it.onSurfaceVariant }
    )
