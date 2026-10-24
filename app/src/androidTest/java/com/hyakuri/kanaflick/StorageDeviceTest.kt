package com.hyakuri.kanaflick
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.hyakuri.kanaflick.vocabulary.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

/** Optional seed/verify/cleanup phases allow a real force-stop between separate invocations. */
class StorageDeviceTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    @Test fun persistenceAndRestartProbe() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val phase = InstrumentationRegistry.getArguments().getString("probePhase")
        val dir = if (phase == null) java.io.File(context.cacheDir,"storage-device-test") else context.noBackupFilesDir
        val store = VocabularyStore(dir)
        val fixture = Vocabulary("restart-probe", "重启持久化检查", "自动化测试临时词库", Samples.words, true)
        when (phase) {
            "seed" -> store.save(store.load().filterNot { it.id == fixture.id } + fixture)
            "verify" -> {
                assertEquals(fixture, store.load().find { it.id == fixture.id })
                rule.waitUntil(5000) { rule.onAllNodesWithText("重启持久化检查 · 3 词", substring = true).fetchSemanticsNodes().isNotEmpty() }
            }
            "cleanup" -> store.save(store.load().filterNot { it.id == fixture.id })
            else -> {
                store.save(listOf(fixture))
                assertEquals(listOf(fixture), VocabularyStore(dir).load())
                val before = java.io.File(dir,"vocabulary-v1.bin").readBytes()
                assertThrows(IllegalArgumentException::class.java) { store.save(listOf(fixture.copy(words = Samples.words + Samples.words))) }
                assertArrayEquals(before, java.io.File(dir,"vocabulary-v1.bin").readBytes())
                store.save(emptyList())
            }
        }
    }
}
