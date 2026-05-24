package com.monospace.battery.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.monospace.battery.R

object Font {
    val AzeretMonoLight = FontFamily(Font(R.font.azeret_mono_light))
    val NType82 = FontFamily(Font(R.font.n_type82_headline))
    val AntonRegular = FontFamily(Font(R.font.anton_regular))
    val ByteSizedRegular = FontFamily(Font(R.font.bytesized_regular))
    val GoogleSansCodeRegular = FontFamily(Font(R.font.google_sans_code_regular))

    fun getAodFont(index: Int): FontFamily {
        return when (index) {
            0 -> NType82
            1 -> AzeretMonoLight
            2 -> AntonRegular
            3 -> ByteSizedRegular
            4 -> GoogleSansCodeRegular
            else -> FontFamily.Default
        }
    }
}
