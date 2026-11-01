package com.monospace.battery.widgets.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.TypedValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.createBitmap
import androidx.datastore.preferences.core.Preferences
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.LocalState
import androidx.glance.layout.height
import androidx.glance.layout.width
import androidx.glance.unit.ColorProvider
import com.monospace.battery.R
import com.monospace.battery.core.constants.Constants
import com.monospace.battery.core.constants.WidgetConstants
import com.monospace.battery.data.local.PreferenceManager
import com.monospace.battery.widgets.WidgetsUpdater
import kotlin.math.ceil


@Composable
fun WidgetHeadingText(
    text: String,
    fontSize: TextUnit,
    reservedWidthDp: Float = WidgetConstants.TEXT_HORIZONTAL_MARGIN_DP
) {
    val fontIndex = PreferenceManager(LocalContext.current)
        .get(Constants.PREFS_ALERTS, Constants.KEY_HEADING_FONT, 0)
    val fontRes = if (fontIndex == 1) R.font.geist_regular else R.font.n_type82_headline

    WidgetBitmapText(text, fontSize, fontRes, widgetTextColor(), reservedWidthDp)
}

@Composable
fun WidgetLabelText(
    text: String,
    fontSize: TextUnit,
    color: ColorProvider = widgetSecondaryTextColor(),
    reservedWidthDp: Float = WidgetConstants.TEXT_HORIZONTAL_MARGIN_DP
) {
    WidgetBitmapText(text, fontSize, R.font.azeret_mono_light, color, reservedWidthDp)
}

@Composable
private fun WidgetBitmapText(
    text: String,
    fontSize: TextUnit,
    fontRes: Int,
    color: ColorProvider,
    reservedWidthDp: Float
) {
    val context = LocalContext.current
    val maxWidthDp = LocalSize.current.width.value - reservedWidthDp
    val refresh = (LocalState.current as? Preferences)?.get(WidgetsUpdater.REFRESH_KEY)

    val rendered = remember(text, fontSize, fontRes, maxWidthDp, refresh) {
        renderText(context, text, fontSize.value, fontRes, maxWidthDp)
    }

    Image(
        provider = ImageProvider(rendered.bitmap),
        contentDescription = text,
        modifier = GlanceModifier.width(rendered.widthDp.dp).height(rendered.heightDp.dp),
        colorFilter = ColorFilter.tint(color)
    )
}

private class RenderedText(val bitmap: Bitmap, val widthDp: Float, val heightDp: Float)

private fun renderText(
    context: Context,
    text: String,
    fontSizeSp: Float,
    fontRes: Int,
    maxWidthDp: Float
): RenderedText {
    val metrics = context.resources.displayMetrics

    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        typeface = ResourcesCompat.getFont(context, fontRes)
        textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, fontSizeSp, metrics)
    }

    val maxWidthPx = maxWidthDp.coerceAtLeast(1f) * metrics.density
    val measured = paint.measureText(text)
    if (measured > maxWidthPx) paint.textSize *= maxWidthPx / measured

    val width = ceil(paint.measureText(text)).toInt().coerceAtLeast(1)
    val ascent = -paint.fontMetrics.ascent
    val height = ceil(ascent + paint.fontMetrics.descent).toInt().coerceAtLeast(1)

    val bitmap = createBitmap(width, height)
    Canvas(bitmap).drawText(text, 0f, ascent, paint)

    return RenderedText(bitmap, width / metrics.density, height / metrics.density)
}
