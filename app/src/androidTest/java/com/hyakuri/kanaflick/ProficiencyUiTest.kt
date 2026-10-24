package com.hyakuri.kanaflick

import android.graphics.Bitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.hyakuri.kanaflick.proficiency.*
import com.hyakuri.kanaflick.reading.ReadingLibrary
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File

/** Automated UI evidence only; does not replace two real Japanese IMEs or TalkBack. */
class ProficiencyUiTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    @Test fun acceptedSentencePersistsOnceAcrossRecreationAndResetRequiresConfirmation() {
        val repo = ProfileRepository.get(rule.activity)
        rule.waitUntil(15000) { repo.state.value.ready }
        val generation = repo.state.value.profile.generation
        repo.reset()
        rule.waitUntil(5000) { repo.state.value.profile.generation != generation && !repo.state.value.pending }
        rule.waitUntil(15000) { rule.onAllNodesWithText("句子练习").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithText("句子练习").performScrollTo().performClick()
        rule.waitUntil(15000) { rule.onAllNodesWithTag("start-sentence").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("start-sentence").performScrollTo().performClick()
        val article = ReadingLibrary.load { rule.activity.assets.open(it) }.first { it.id == "amabie" }
        rule.onNodeWithTag("answer").performScrollTo().performTextReplacement(article.sentences.first().kana)
        rule.waitUntil(5000) { repo.state.value.profile.stats.isNotEmpty() }
        val saved = repo.state.value.profile
        rule.activityRule.scenario.recreate()
        rule.waitForIdle()
        assertEquals(saved, repo.state.value.profile)
        assertEquals(saved, ProfileStore(rule.activity.noBackupFilesDir).load())
        rule.onNodeWithText("退出练习").performScrollTo().performClick()
        rule.onNodeWithText("确认退出").performClick()
        rule.onNodeWithText("返回首页").performScrollTo().performClick()
        rule.onNodeWithTag("tab-1").performClick()
        rule.onNodeWithText("假名输入熟练度").performScrollTo().performClick()
        rule.onNodeWithText("基础音").assertExists()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val bitmap = requireNotNull(instrumentation.uiAutomation.takeScreenshot())
        val folder = File(rule.activity.getExternalFilesDir(null), "evidence").apply { mkdirs() }
        File(folder, "proficiency.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
        rule.onNodeWithText("重置假名熟练度").performScrollTo().performClick()
        rule.onNodeWithText("取消").performClick()
        assertTrue(repo.state.value.profile.stats.isNotEmpty())
        rule.onNodeWithText("重置假名熟练度").performScrollTo().performClick()
        rule.onNodeWithText("确认重置").performClick()
        rule.waitUntil(5000) { repo.state.value.profile.stats.isEmpty() }
    }
    @Test fun targetedSwitchSurvivesRecreation() {
        rule.waitUntil(15000) { rule.onAllNodesWithTag("tab-2").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("tab-2").performClick()
        val preferences = rule.activity.getSharedPreferences("practice-options", 0)
        val original = preferences.getBoolean("targeted-kana", false)
        rule.onNodeWithTag("targeted-kana").performScrollTo().performClick()
        rule.activityRule.scenario.recreate()
        if (original) rule.onNodeWithTag("targeted-kana").performScrollTo().assertIsOff()
        else rule.onNodeWithTag("targeted-kana").performScrollTo().assertIsOn()
        assertEquals(!original, preferences.getBoolean("targeted-kana", false))
        rule.onNodeWithTag("targeted-kana").performClick()
        assertEquals(original, preferences.getBoolean("targeted-kana", false))
    }
}
