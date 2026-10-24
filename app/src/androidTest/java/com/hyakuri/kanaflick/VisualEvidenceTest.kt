package com.hyakuri.kanaflick

import android.graphics.Bitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.hyakuri.kanaflick.practice.PracticeState
import java.io.File
import org.junit.Rule
import org.junit.Test

/** Device screenshots, including system bars. Run once per system theme/display configuration. */
class VisualEvidenceTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    private fun capture(name: String) {
        rule.waitForIdle()
        android.os.SystemClock.sleep(500) // Let system IME/window transitions settle before visual evidence.
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val bitmap = requireNotNull(instrumentation.uiAutomation.takeScreenshot())
        val folder = File(instrumentation.targetContext.getExternalFilesDir(null), "evidence")
        folder.mkdirs()
        File(folder, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }
    @Test fun captureThreePagesAndError() {
        rule.waitUntil(15000) { rule.onAllNodesWithText("开始练习").fetchSemanticsNodes().isNotEmpty() }
        capture("home")
        rule.onNodeWithTag("tab-1").performClick()
        rule.activity.runOnUiThread { rule.activity.getSystemService(android.view.inputmethod.InputMethodManager::class.java).hideSoftInputFromWindow(rule.activity.window.decorView.windowToken, 0) }
        capture("learning")
        rule.onNodeWithTag("tab-2").performClick(); capture("options")
        rule.onNodeWithTag("tab-0").performClick(); startSampleRound(rule)
        rule.onNodeWithTag("answer").assertIsFocused()
        android.os.SystemClock.sleep(InstrumentationRegistry.getArguments().getString("practicePauseMillis")?.toLongOrNull()?.coerceIn(0, 60000) ?: 0)
        capture("practice")
        rule.onNodeWithTag("answer").performTextReplacement("誤答")
        rule.mainClock.advanceTimeBy(750); rule.waitForIdle(); rule.onNodeWithTag("feedback").performScrollTo(); capture("error")
        repeat(3) {
            rule.onNodeWithTag("answer").performScrollTo().performTextReplacement(currentTarget(rule))
            rule.mainClock.advanceTimeBy(750); rule.waitForIdle()
        }
        rule.onNodeWithTag("accuracy").assertTextEquals("75.00%")
        capture("result")
    }
}





