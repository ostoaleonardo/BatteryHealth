package com.monospace.battery.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.monospace.battery.R

object Font {
    val NType82 = FontFamily(Font(R.font.n_type82_headline))
    val AzeretMonoLight = FontFamily(Font(R.font.azeret_mono_light))
    val BebasNeueRegular = FontFamily(Font(R.font.bebas_neue_regular))
    val GeoRegular = FontFamily(Font(R.font.geo_regular))
    val BitcountGridRegular = FontFamily(Font(R.font.bitcount_grid_double))
    val GoogleSansCodeRegular = FontFamily(Font(R.font.google_sans_code_regular))

    fun getFont(index: Int): FontFamily {
        return when (index) {
            0 -> NType82
            1 -> BebasNeueRegular
            2 -> GeoRegular
            3 -> BitcountGridRegular
            4 -> GoogleSansCodeRegular
            else -> FontFamily.Default
        }
    }
}
