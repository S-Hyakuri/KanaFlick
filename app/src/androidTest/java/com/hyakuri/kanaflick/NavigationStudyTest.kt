package com.hyakuri.kanaflick

import androidx.test.platform.app.InstrumentationRegistry
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class NavigationStudyTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    private fun waitLoaded() { rule.waitUntil(15000) { rule.onAllNodesWithText("开始练习").fetchSemanticsNodes().isNotEmpty() } }
    @Test fun browsingSurvivesTabsRecreationAndPracticeExit() {
        waitLoaded()
        rule.onNodeWithTag("tab-1").performClick()
        rule.onNodeWithTag("study-search").assertDoesNotExist()
        rule.onNodeWithTag("study-count").assertTextContains("805 词", substring = true)
        rule.onNodeWithTag("tab-0").performClick(); rule.onNodeWithTag("tab-1").performClick()
        rule.activityRule.scenario.recreate()
        rule.waitUntil(15000) { rule.onAllNodesWithText("练习这些词").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithText("练习这些词").performScrollTo().performClick()
        rule.onNodeWithTag("target").assertExists()
        rule.onNodeWithText("退出练习").performScrollTo().performClick()
        rule.onNodeWithText("确认退出").performClick()
        rule.onNodeWithText("返回首页").performScrollTo().performClick()
        rule.onNodeWithTag("tab-1").assertIsSelected()
        rule.onNodeWithTag("study-search").assertDoesNotExist()
    }
    @Test fun savedRoundSizeDrivesRealRandomRoundAndRestart() {
        waitLoaded(); rule.onNodeWithTag("tab-2").performClick()
        when (InstrumentationRegistry.getArguments().getString("preferenceProbe")) {
            "seed" -> {
                rule.onNodeWithTag("round-20").performClick()
                rule.runOnIdle { rule.activity.getSharedPreferences("practice-options",0).edit().commit() }
                return
            }
            "verify" -> {
                rule.onNodeWithTag("round-20").assertIsSelected()
                rule.onNodeWithTag("round-10").performClick()
                return
            }
        }
        try {
            rule.onNodeWithTag("round-5").performClick()
            rule.activityRule.scenario.recreate()
            rule.onNodeWithTag("round-5").assertIsSelected()
            assertEquals(5,rule.activity.getSharedPreferences("practice-options",0).getInt("round-size",0))
            rule.onNodeWithTag("tab-0").performClick(); waitLoaded()
            rule.onNodeWithText("开始练习").performScrollTo().performClick()
            val targets = mutableListOf<String>()
            repeat(5) {
                val target=rule.onNodeWithTag("target").fetchSemanticsNode().config[SemanticsProperties.Text].first().text
                targets.add(target)
                rule.onNodeWithTag("answer").performScrollTo().performTextReplacement(target)
                rule.mainClock.advanceTimeBy(750); rule.waitForIdle()
            }
            assertEquals(5,targets.toSet().size)
            rule.onNodeWithTag("accuracy").assertTextEquals("100.00%")
            rule.onNodeWithText("再次练习").performScrollTo().performClick()
            rule.onNodeWithTag("target").assertTextEquals(targets.first())
            rule.onNodeWithTag("counts").assertContentDescriptionEquals("正确 0 次，错误 0 次")
        } finally { rule.activity.getSharedPreferences("practice-options",0).edit().putInt("round-size",10).commit() }
    }
    @Test fun offlineAttributionAndLicenseAreReadable() {
        waitLoaded()
        rule.onNodeWithTag("tab-2").performClick()
        rule.onNodeWithText("查看来源与处理说明").performScrollTo().performClick()
        rule.waitUntil(15000) { rule.onAllNodesWithText("原作者：egg rolls", substring=true).fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithText("关闭").performClick()
        rule.onNodeWithText("查看许可证全文").performScrollTo().performClick()
        rule.waitUntil(15000) { rule.onAllNodesWithText("Attribution-NonCommercial 4.0 International", substring=true).fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithText("关闭").performClick()
    }
}

