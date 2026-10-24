package com.hyakuri.kanaflick

import android.text.InputType
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.hyakuri.kanaflick.vocabulary.Samples
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class InputConnectionTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    @Test fun composingKanaScoresImmediatelyAndLateCandidateIsRejected() {
        startSampleRound(rule)
        val first = currentTarget(rule)
        val reading = Samples.words.first { it.text == first }.reading
        rule.onNodeWithTag("answer").performScrollTo().performClick()
        lateinit var connection: InputConnection
        rule.runOnIdle {
            val attributes = EditorInfo()
            val view = requireNotNull(rule.activity.window.decorView.findFocus())
            connection = requireNotNull(view.onCreateInputConnection(attributes))
            assertTrue(attributes.inputType and InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS != 0)
        }
        for (length in 1 until reading.length) {
            rule.runOnIdle { connection.setComposingText(reading.take(length), 1) }
            rule.onNodeWithTag("counts").assertContentDescriptionEquals("正确 0 次，错误 0 次")
        }
        rule.runOnIdle { connection.setComposingText(reading, 1) }
        rule.waitForIdle()
        rule.onNodeWithTag("counts").assertContentDescriptionEquals("正确 1 次，错误 0 次")
        val next = currentTarget(rule)
        rule.runOnIdle { connection.commitText(first, 1); connection.finishComposingText() }
        rule.waitForIdle()
        rule.onNodeWithTag("target").assertTextEquals(next)
        rule.onNodeWithTag("counts").assertContentDescriptionEquals("正确 1 次，错误 0 次")
        rule.onNodeWithTag("answer").performImeAction()
        rule.activityRule.scenario.recreate()
        rule.onNodeWithTag("target").assertTextEquals(next)
    }
    @Test fun recreationDoesNotRescoreExistingComposingError() {
        startSampleRound(rule)
        val first = currentTarget(rule)
        rule.onNodeWithTag("answer").performScrollTo().performClick()
        rule.runOnIdle {
            val view = requireNotNull(rule.activity.window.decorView.findFocus())
            val connection = requireNotNull(view.onCreateInputConnection(EditorInfo()))
            connection.setComposingText("誤答", 1)
        }
        rule.onNodeWithTag("counts").assertContentDescriptionEquals("正确 0 次，错误 1 次")
        rule.activityRule.scenario.recreate()
        rule.waitForIdle()
        rule.onNodeWithTag("answer").assertTextContains("誤答")
        rule.onNodeWithTag("counts").assertContentDescriptionEquals("正确 0 次，错误 1 次")
        rule.onNodeWithTag("answer").performTextReplacement(first)
        rule.waitForIdle()
        rule.onNodeWithTag("counts").assertContentDescriptionEquals("正确 1 次，错误 1 次")
    }
}
