package com.hyakuri.kanaflick

import android.view.inputmethod.EditorInfo
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.hyakuri.kanaflick.reading.ReadingLibrary
import org.junit.Rule
import org.junit.Test

class SentenceUiTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    private fun sentenceHome() {
        rule.waitUntil(15000) { rule.onAllNodesWithText("句子练习").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithText("句子练习").performScrollTo().performClick()
        rule.waitUntil(15000) { rule.onAllNodesWithTag("start-sentence").fetchSemanticsNodes().isNotEmpty() }
    }
    private fun start() {
        sentenceHome()
        rule.onNodeWithTag("start-sentence").performScrollTo().performClick()
    }
    @Test fun articleCompletesWithErrorsSkipAndRecreation() {
        start()
        val article = ReadingLibrary.load { rule.activity.assets.open(it) }.first { it.id == "amabie" }
        rule.onNodeWithTag("answer").performScrollTo().performTextReplacement("まちがい")
        rule.mainClock.advanceTimeBy(300)
        rule.onNodeWithTag("feedback").assertTextContains("第 1 字", substring = true)
        rule.onNodeWithTag("answer").assertTextContains("まちがい")
        rule.onNodeWithTag("answer").performTextReplacement(article.sentences[0].kana)
        rule.waitForIdle()
        rule.onNodeWithTag("progress").assertTextEquals("1 / ${article.sentences.size}")
        rule.activityRule.scenario.recreate()
        rule.onNodeWithTag("progress").assertTextEquals("1 / ${article.sentences.size}")
        rule.onNodeWithTag("skip").performScrollTo().performClick()
        for (sentence in article.sentences.drop(2)) {
            rule.onNodeWithTag("answer").performScrollTo().performTextReplacement(sentence.kana)
            rule.waitForIdle()
        }
        rule.onNodeWithText("完成 ${article.sentences.size - 1} 句 · 跳过 1 句 · 错误 1 次").assertExists()
        rule.onNodeWithText("再次练习").performScrollTo().performClick()
        rule.onNodeWithTag("progress").assertTextEquals("0 / ${article.sentences.size}")
    }
    @Test fun modeConfigAndReadingDetailSurviveRecreation() {
        sentenceHome()
        rule.onNodeWithTag("reading-N3").performScrollTo().performClick()
        rule.onNodeWithText("单词练习").performScrollTo().performClick()
        rule.onNodeWithText("句子练习").performScrollTo().performClick()
        rule.onNodeWithTag("reading-N3").assertIsSelected()
        rule.onNodeWithText("查看所有阅读").performScrollTo().performClick()
        rule.onNodeWithText("窓の外の人").performClick()
        rule.onNodeWithTag("reading-original").assertExists()
        rule.activityRule.scenario.recreate()
        rule.waitUntil(15000) { rule.onAllNodesWithTag("reading-original").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithText("展开假名").performScrollTo().performClick()
        rule.onNodeWithText("练习这篇").performScrollTo().performClick()
        rule.onNodeWithTag("progress").assertTextEquals("0 / 26")
    }
    @Test fun composingPrefixAndRestoredTextDoNotScoreAndPrefixCanGoBack() {
        start()
        rule.onNodeWithTag("answer").performScrollTo().performClick()
        rule.runOnIdle {
            val view = requireNotNull(rule.activity.window.decorView.findFocus())
            val connection = requireNotNull(view.onCreateInputConnection(EditorInfo()))
            connection.setComposingText("これは", 1)
        }
        rule.mainClock.advanceTimeBy(400)
        rule.onNodeWithTag("counts").assertContentDescriptionEquals("正确 0 次，错误 0 次")
        rule.activityRule.scenario.recreate()
        rule.mainClock.advanceTimeBy(400)
        rule.onNodeWithTag("counts").assertContentDescriptionEquals("正确 0 次，错误 0 次")
        rule.onNodeWithTag("answer").performScrollTo().performTextReplacement("これは")
        rule.onNodeWithTag("sentence-prompt").assert(SemanticsMatcher.expectValue(androidx.compose.ui.semantics.SemanticsProperties.StateDescription, "已匹配 3 字"))
        rule.onNodeWithTag("answer").performTextReplacement("こ")
        rule.onNodeWithTag("sentence-prompt").assert(SemanticsMatcher.expectValue(androidx.compose.ui.semantics.SemanticsProperties.StateDescription, "已匹配 1 字"))
    }
}
