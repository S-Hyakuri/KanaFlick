package com.hyakuri.kanaflick

import android.os.SystemClock
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class PracticeExperienceTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    private fun seconds(): Long {
        rule.mainClock.advanceTimeBy(300); rule.waitForIdle()
        val text = rule.onNodeWithTag("timer").fetchSemanticsNode().config[SemanticsProperties.ContentDescription].single().substringAfterLast(" ")
        val parts = text.split(":")
        return parts[0].toLong() * 60 + parts[1].toLong()
    }
    private fun imeVisible(): Boolean {
        var visible = false
        rule.runOnUiThread { visible = ViewCompat.getRootWindowInsets(rule.activity.window.decorView)?.isVisible(WindowInsetsCompat.Type.ime()) == true }
        return visible
    }
    @Test fun autofocusShowsKeyboardOnceAndTimerSurvivesLifecycle() {
        startSampleRound(rule)
        rule.onNodeWithTag("answer").assertIsFocused()
        rule.waitUntil(5000) { imeVisible() }
        rule.runOnUiThread { WindowInsetsControllerCompat(rule.activity.window, rule.activity.window.decorView).hide(WindowInsetsCompat.Type.ime()) }
        rule.waitUntil(5000) { !imeVisible() }
        SystemClock.sleep(1300)
        val before = seconds()
        assertTrue(before >= 1)
        assertFalse(imeVisible())
        rule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        SystemClock.sleep(1600)
        rule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        val after = seconds()
        assertTrue("Background must be excluded: $before -> $after", after - before in 0..1)
        rule.activityRule.scenario.recreate()
        assertTrue(seconds() >= after)
        assertTrue(seconds() - after <= 1)
        rule.onNodeWithTag("counts").assertContentDescriptionEquals("正确 0 次，错误 0 次")
        // Tick-driven recomposition must not reopen a keyboard the user dismissed.
        SystemClock.sleep(1300)
        assertFalse(imeVisible())
        repeat(3) {
            rule.onNodeWithTag("answer").performTextReplacement(currentTarget(rule))
            rule.mainClock.advanceTimeBy(750); rule.waitForIdle()
        }
        val result = rule.onNodeWithText("本局用时", substring = true).fetchSemanticsNode().config[SemanticsProperties.Text]
        SystemClock.sleep(1300)
        assertEquals(result, rule.onNodeWithText("本局用时", substring = true).fetchSemanticsNode().config[SemanticsProperties.Text])
        rule.onNodeWithText("再次练习").performScrollTo().performClick()
        assertTrue(seconds() <= 1)
        rule.onNodeWithTag("answer").assertIsFocused()
    }
    @Test fun optionsExposeAuthorAndHelpWithoutTutorial() {
        rule.waitUntil(15000) { rule.onAllNodesWithText("开始练习").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("tab-2").performClick()
        rule.onNodeWithText("体验 3 词教程").assertDoesNotExist()
        rule.onNodeWithText("关于 KanaFlick").performScrollTo().performClick()
        rule.onNodeWithText("Chongzheng Li · @S-Hyakuri").assertExists()
        rule.onNodeWithText("关闭").performClick()
        rule.onNodeWithText("使用帮助").performScrollTo().performClick()
        rule.onNodeWithText("自动判题").performScrollTo().assertIsDisplayed()
        rule.onAllNodesWithText("每次文本变化立即判断", substring = true).assertCountEquals(1)
    }
}

