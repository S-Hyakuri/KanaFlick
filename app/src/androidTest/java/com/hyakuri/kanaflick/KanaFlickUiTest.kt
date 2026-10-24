package com.hyakuri.kanaflick
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test
class KanaFlickUiTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    private fun start() { startSampleRound(rule) }
    private fun answer(text: String) { rule.onNodeWithTag("answer").performScrollTo().performTextReplacement(text); rule.mainClock.advanceTimeBy(750); rule.waitForIdle() }
    @Test fun roundAndWrongAnswer() {
        start(); val first = currentTarget(rule); answer("誤答")
        rule.onNodeWithTag("target").assertTextEquals(first)
        rule.onNodeWithTag("answer").assertTextContains("誤答")
        rule.onNodeWithTag("submit").assertDoesNotExist()
        repeat(3) { answer(currentTarget(rule)) }
        rule.onNodeWithTag("accuracy").assertTextEquals("75.00%")
        rule.onNodeWithText("再次练习").performScrollTo().performClick()
        rule.onNodeWithTag("counts").assertContentDescriptionEquals("正确 0 次，错误 0 次")
    }
    @Test fun recreationKeepsEditedInputAndCounts() {
        start(); answer(currentTarget(rule)); val next = currentTarget(rule)
        rule.onNodeWithTag("answer").performTextReplacement("編集中"); rule.mainClock.advanceTimeBy(750); rule.waitForIdle()
        rule.activityRule.scenario.recreate()
        rule.onNodeWithTag("target").assertTextEquals(next)
        rule.onNodeWithTag("answer").assertTextContains("編集中")
        rule.onNodeWithTag("counts").assertContentDescriptionEquals("正确 1 次，错误 1 次")
        answer(currentTarget(rule))
    }
    @Test fun automaticAnswerAndImeDoneDoNotDoubleScore() {
        start()
        rule.onNodeWithTag("answer").performTextReplacement(currentTarget(rule))
        rule.mainClock.advanceTimeBy(750); rule.waitForIdle()
        val next = currentTarget(rule)
        rule.onNodeWithTag("answer").performImeAction()
        rule.mainClock.advanceTimeBy(750); rule.waitForIdle()
        rule.onNodeWithTag("target").assertTextEquals(next)
        rule.onNodeWithTag("counts").assertContentDescriptionEquals("正确 1 次，错误 0 次")
        rule.onNodeWithTag("submit").assertDoesNotExist()
    }    @Test fun exitClears() {
        start(); val first = currentTarget(rule); rule.onNodeWithText("退出练习").performScrollTo().performClick()
        rule.onNodeWithText("继续练习").performClick()
        rule.onNodeWithTag("target").assertTextEquals(first)
        rule.onNodeWithText("退出练习").performScrollTo().performClick()
        rule.onNodeWithText("确认退出").performClick()
        rule.onNodeWithTag("accuracy").assertTextEquals("0.00%")
        rule.onNodeWithText("返回首页").performClick(); start()
        rule.onNodeWithTag("counts").assertContentDescriptionEquals("正确 0 次，错误 0 次")
    }
}



