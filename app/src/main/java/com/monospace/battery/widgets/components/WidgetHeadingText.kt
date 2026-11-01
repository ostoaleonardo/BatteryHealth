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
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.layout.height
import androidx.glance.layout.width
import com.monospace.battery.R
import com.monospace.battery.core.constants.Constants
import com.monospace.battery.data.local.PreferenceManager
import kotlin.math.ceil
import androidx.core.graphics.createBitmap

private const val HORIZONTAL_MARGIN_DP = 32f

@Composable
fun WidgetHeadingText(
    text: String,
    fontSize: TextUnit,
    reservedWidthDp: Float = HORIZONTAL_MARGIN_DP
) {
    val context = LocalContext.current
    val maxWidthDp = LocalSize.current.width.value - reservedWidthDp
    val fontIndex = PreferenceManager(context)
        .get(Constants.PREFS_ALERTS, Constants.KEY_HEADING_FONT, 0)

    val rendered = remember(text, fontSize, maxWidthDp, fontIndex) {
        renderHeading(context, text, fontSize.value, maxWidthDp, fontIndex)
    }

    Image(
        provider = ImageProvider(rendered.bitmap),
        contentDescription = text,
        modifier = GlanceModifier.width(rendered.widthDp.dp).height(rendered.heightDp.dp),
        colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurface)
    )
}

private class RenderedHeading(val bitmap: Bitmap, val widthDp: Float, val heightDp: Float)

private fun renderHeading(
    context: Context,
    text: String,
    fontSizeSp: Float,
    maxWidthDp: Float,
    fontIndex: Int
): RenderedHeading {
    val metrics = context.resources.displayMetrics
    val fontRes = if (fontIndex == 1) R.font.geist_medium else R.font.n_type82_headline

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

    return RenderedHeading(bitmap, width / metrics.density, height / metrics.density)
}
