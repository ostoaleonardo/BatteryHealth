package com.monospace.battery

import android.os.Build
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.monospace.battery.core.constants.Constants
import com.monospace.battery.data.local.PreferenceManager
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppNavigationSmokeTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @After
    fun resetHeadingFont() {
        if (!(Build.FINGERPRINT.startsWith("generic") || Build.PRODUCT.contains("sdk"))) return
        PreferenceManager(composeRule.activity)
            .set(Constants.PREFS_ALERTS, Constants.KEY_HEADING_FONT, 0)
    }

    private fun label(id: Int) = composeRule.activity.getString(id)

    @Test
    fun bottomNavigationOpensEveryTab() {
        listOf(R.string.nav_history, R.string.nav_alerts, R.string.nav_aod, R.string.nav_home).forEach {
            composeRule.onAllNodesWithText(label(it)).onFirst().performClick()
            composeRule.waitForIdle()
            composeRule.onAllNodesWithText(label(it)).onFirst().assertExists()
        }
    }

    @Test
    fun settingsOpensAndGoesBack() {
        composeRule.onNodeWithContentDescription(label(R.string.action_settings)).performClick()
        composeRule.waitForIdle()
        composeRule.onAllNodesWithText(label(R.string.action_settings)).onFirst().assertExists()

        composeRule.onNodeWithContentDescription(label(R.string.action_back)).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(label(R.string.action_settings)).assertExists()
    }

    @Test
    fun headingFontSelectionIsPersisted() {
        assumeTrue(Build.FINGERPRINT.startsWith("generic") || Build.PRODUCT.contains("sdk"))
        val prefs = PreferenceManager(composeRule.activity)
        prefs.set(Constants.PREFS_ALERTS, Constants.KEY_HEADING_FONT, 0)

        composeRule.onNodeWithContentDescription(label(R.string.action_settings)).performClick()
        composeRule.waitForIdle()
        composeRule.onAllNodesWithText(label(R.string.settings_font_2)).onFirst().performClick()
        composeRule.waitForIdle()

        assertEquals(1, prefs.get(Constants.PREFS_ALERTS, Constants.KEY_HEADING_FONT, 0))
    }
}
