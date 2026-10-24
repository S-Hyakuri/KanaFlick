package com.hyakuri.kanaflick

import com.hyakuri.kanaflick.practice.*
import com.hyakuri.kanaflick.proficiency.*
import com.hyakuri.kanaflick.vocabulary.Word
import org.junit.Assert.*
import org.junit.Test

class InputCompatibilityTest {
    private fun state(target: String) = PracticeState().start(listOf(Word("test", target, target, "test", "N5")))

    @Test fun hiraganaCompletesKatakanaWithoutErrorsOrFalseProfileFailures() {
        var s = state("キャベツ")
        listOf("き", "きゃ", "きゃべ", "きゃべつ").forEachIndexed { i, input ->
            s = s.evaluateInput(input, i + 1L, 0)
            assertEquals(0, s.errors)
        }
        assertEquals(1, s.correct)
        assertEquals(setOf("きゃ", "べ", "つ"), s.kana.pending.single().success)
    }
    @Test fun equivalenceIsSymmetricAndPreservesOriginalPrompt() {
        val match = liveMatch("コー", listOf("こーひー"), false)
        assertTrue(match.validPrefix)
        assertEquals("こーひー", match.expected)
        assertEquals(2, matchingPrefix("こー", "コーヒー"))
        assertEquals(1, state("こーひー").evaluateInput("コーヒー", 1, 0).correct)
        assertFalse(liveMatch("こひ", listOf("コーヒー"), false).validPrefix)
        assertFalse(liveMatch("きやく", listOf("キャク"), false).complete)
    }
    @Test fun everySmallKanaWaitsUntilTheNextKanaRegardlessOfComposition() {
        "あぁいぃうぅえぇおぉつっやゃゆゅよょわゎかゕけゖ".chunked(2).forEach { pair ->
            for (composing in listOf(false, true)) {
                val target = "き${pair[1]}く"
                val pending = "き${pair[0]}"
                assertTrue(liveMatch(pending, listOf(target), composing).pendingConversion)
                assertTrue(liveMatch(pending + "k", listOf(target), composing).pendingConversion)
                assertEquals(1, state(target).evaluateInput(pending + "く", 1, 0, composing).errors)
                assertEquals(1, state(target).evaluateInput(target, 1, 0, composing).correct)
            }
        }
    }
    @Test fun finalLargeKanaDoesNotSilentlyCountAsTheSmallAnswer() {
        val result = state("きゃ").evaluateInput("きや", 1, 0)
        assertEquals(0, result.errors)
        assertEquals(0, result.correct)
        assertEquals(1, result.evaluateInput("きゃ", 2, 0).correct)
    }
    @Test fun qwertyTextEventsWorkWithoutCompositionMarkers() {
        var s = state("きゃく")
        listOf("k", "ky", "kya", "きゃ", "きゃk", "きゃく").forEachIndexed { i, text ->
            s = s.evaluateInput(text, i + 1L, 0, i % 2 == 0)
            assertEquals(0, s.errors)
        }
        assertEquals(1, s.correct)
        assertTrue(liveMatch("ｋｙ", listOf("きゃく"), false).pendingConversion)
    }
    @Test fun romanSuffixDoesNotHideAnEarlierWrongKana() {
        val s = state("きゃく").evaluateInput("きやくk", 1, 0)
        assertEquals(1, s.errors)
        assertEquals(1, s.evaluateInput("キヤクka", 2, 0).errors)
        assertEquals(1, s.evaluateInput("きゃく", 3, 0).correct)
    }
    @Test fun chartContainsEachSupportedUnitOnceInFiveColumns() {
        val rows = KanaChart.groups.values.flatten()
        assertTrue(rows.all { it.size == 5 })
        val units = rows.flatten().filter { it.isNotEmpty() }
        assertEquals(KanaTokenizer.units, units.toSet())
        assertEquals(units.size, units.toSet().size)
        assertTrue(KanaChart.groups.getValue("基础音").contains(listOf("や", "", "ゆ", "", "よ")))
        assertTrue(KanaChart.groups.getValue("基础音").contains(listOf("わ", "ゐ", "", "ゑ", "を")))
    }
}
