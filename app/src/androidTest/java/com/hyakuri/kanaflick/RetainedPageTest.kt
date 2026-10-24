package com.hyakuri.kanaflick

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test

/** Functional retention checks; these do not measure frame timing. */
class RetainedPageTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Test fun proficiencyRetainsScrolledPageAcrossTabs() {
        rule.waitUntil(15000) { rule.onAllNodesWithText("开始练习").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("tab-1").performClick()
        rule.onNodeWithText("假名输入熟练度").performScrollTo().performClick()
        rule.onNodeWithTag("proficiency-list").performScrollToNode(hasTestTag("reset-proficiency"))
        rule.onNodeWithTag("reset-proficiency").assertIsDisplayed()
        rule.onNodeWithTag("tab-2").performClick()
        rule.onNodeWithTag("proficiency-list").assertDoesNotExist()
        rule.onNodeWithTag("tab-1").performClick()
        rule.onNodeWithTag("reset-proficiency").assertIsDisplayed()
    }

    @Test fun settingsKeepScrolledPositionAndOpenLicense() {
        rule.waitUntil(15000) { rule.onAllNodesWithTag("tab-2").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("tab-2").performClick()
        rule.onNodeWithTag("settings-list").performScrollToNode(hasText("阅读来源与许可"))
        rule.onNodeWithText("阅读来源与许可").assertIsDisplayed()
        rule.onNodeWithTag("tab-0").performClick()
        rule.onNodeWithTag("tab-2").performClick()
        rule.onNodeWithText("阅读来源与许可").assertIsDisplayed().performClick()
        rule.onNodeWithText("关闭").assertIsDisplayed().performClick()
    }
}
