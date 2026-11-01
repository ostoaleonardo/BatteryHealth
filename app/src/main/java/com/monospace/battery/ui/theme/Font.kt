package com.monospace.battery.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.font.FontFamily
import com.monospace.battery.R

object Font {
    val NType82 = FontFamily(Font(R.font.n_type82_headline))
    val AzeretMonoLight = FontFamily(Font(R.font.azeret_mono_light))
    val GeistRegular = FontFamily(Font(R.font.geist_regular))

    fun getHeadingFont(index: Int): FontFamily = if (index == 1) GeistRegular else NType82

}

val LocalHeadingFont = staticCompositionLocalOf { com.monospace.battery.ui.theme.Font.NType82 }
