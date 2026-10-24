package com.hyakuri.kanaflick

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.hyakuri.kanaflick.vocabulary.*

/** Use the real custom-library path; no tutorial or test entry point in production. */
internal fun startSampleRound(rule: AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>) {
    rule.waitUntil(15000) { rule.onAllNodesWithText("开始练习").fetchSemanticsNodes().isNotEmpty() }
    val store = VocabularyStore(rule.activity.noBackupFilesDir)
    store.save(store.load().filterNot { it.id == "device-test" } + Vocabulary("device-test", "设备测试词库", "自动测试夹具", Samples.words, true))
    rule.activityRule.scenario.recreate()
    rule.waitUntil(15000) { rule.onAllNodesWithText("设备测试词库", substring = true).fetchSemanticsNodes().isNotEmpty() }
    rule.onNodeWithText("设备测试词库", substring = true).performScrollTo().performClick()
    rule.onNodeWithText("开始练习").performScrollTo().performClick()
}

internal fun currentTarget(rule: AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>): String =
    rule.onNodeWithTag("target").fetchSemanticsNode().config[androidx.compose.ui.semantics.SemanticsProperties.Text].first().text
