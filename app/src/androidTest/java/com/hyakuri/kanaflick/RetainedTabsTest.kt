package com.hyakuri.kanaflick

import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import com.hyakuri.kanaflick.ui.RetainedTabs
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertEquals

class RetainedTabsTest {
    @get:Rule val compose = createComposeRule()
    @Test fun repeatedSwitchesKeepPageTreesAlive() {
        val selected = mutableIntStateOf(0)
        val created = IntArray(3)
        val disposed = IntArray(3)
        compose.setContent {
            RetainedTabs(selected.intValue) { page ->
                DisposableEffect(Unit) {
                    created[page]++
                    onDispose { disposed[page]++ }
                }
                Text("Page $page")
            }
        }
        listOf(1, 2, 0, 2, 1, 0).forEach { page ->
            compose.runOnIdle { selected.intValue = page }
            compose.waitForIdle()
            compose.onNodeWithText("Page $page").assertIsDisplayed()
        }
        compose.runOnIdle {
            for (page in 0..2) {
                assertEquals(1, created[page])
                assertEquals(0, disposed[page])
            }
        }
    }
}
