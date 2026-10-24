package com.hyakuri.kanaflick
import com.hyakuri.kanaflick.vocabulary.*
import com.hyakuri.kanaflick.practice.*
import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

class StudyTest {
    private val words = (1..40).map { Word("w$it", "学校$it", "がっこう$it", "学校$it", "N5") }
    @Test fun samplingHasRequestedSizeNoDuplicatesAndCanReachBeyondFirstPage() {
        listOf(5,10,20).forEach { size ->
            val selected = VocabularyStudy.select(words, size, Random(7))
            assertEquals(size, selected.size); assertEquals(size, selected.toSet().size)
            assertEquals(size, PracticeState().start(selected).words.size)
        }
        val selected = VocabularyStudy.select(words,20,Random(7))
        assertTrue(selected.any { it.id.removePrefix("w").toInt() > 30 })
        assertEquals(3,VocabularyStudy.select(Samples.words,20).size)
        assertTrue(VocabularyStudy.select(emptyList(),10).isEmpty())
        assertThrows(IllegalArgumentException::class.java) { VocabularyStudy.select(words,100) }
    }
    @Test fun twentyWordRoundCompletesOnceAndRestartPreservesSnapshot() {
        val selected=VocabularyStudy.select(words,20,Random(8))
        var state=PracticeState().start(selected)
        repeat(20) { state=state.submit(state.target,it.toLong(),it) }
        assertEquals(Page.RESULT,state.page); assertEquals(20,state.correct)
        assertEquals(selected,state.start().words); assertEquals(0,state.start().correct)
        assertEquals(PracticeState(),state.exit())
    }
}
