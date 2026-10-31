package com.monospace.battery

import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppNavigationSmokeTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

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
}
