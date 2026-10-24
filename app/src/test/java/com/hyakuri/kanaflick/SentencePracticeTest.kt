package com.hyakuri.kanaflick

import com.hyakuri.kanaflick.practice.*
import com.hyakuri.kanaflick.reading.*
import com.hyakuri.kanaflick.vocabulary.Word
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class SentencePracticeTest {
    private val words = listOf(Word("s1", "わたしは、コーヒーをのみます。", "私はコーヒーを飲みます。", "", "N5"),
        Word("s2", "がっこうへいきます。", "学校へ行きます。", "", "N5"))
    private fun session() = PracticeState().start(words, true, "朝")
    @Test fun sentenceWhitespacePunctuationAndNfcKeepParticlesAndLongMark() {
        assertEquals("がっこうへコーヒーを", normalizeSentence("か\u3099っこうへ\n　コーヒー を！\t\u200B"))
        assertNotEquals(normalizeSentence("わたしは"), normalizeSentence("わたしわ"))
        assertNotEquals(normalizeSentence("コーヒー"), normalizeSentence("コヒ"))
        assertEquals("あ い", normalizeAnswer("あ い")) // Existing word contract.
    }
    @Test fun prefixAndEmptyNeverScoreWhileCompleteCompositionAdvances() {
        val s = session()
        listOf("", "\n　！？", "わたしは", "わたしは、コー").forEach { assertEquals(s, s.evaluateInput(it, 1, 0)) }
        assertEquals(1, s.evaluateInput(s.target, 1, 0, composing = true).correct)
    }
    @Test fun correctSentenceAdvancesOnceAndStaleQuestionIsRejected() {
        val s = session(); val next = s.evaluateInput(s.target, 1, 0)
        assertEquals(1, next.index); assertEquals(1, next.correct)
        assertEquals(next, next.evaluateInput(next.target, 1, 1))
        assertEquals(next, next.evaluateInput(next.target, 2, 0))
        assertEquals(Page.RESULT, next.evaluateInput(next.target, 2, 1).page)
    }
    @Test fun errorsRetainQuestionAndRevisionCountsOnlyOnce() {
        val s = session().evaluateInput("わたしわ", 3, 0)
        assertEquals(0, s.index); assertEquals(1, s.errors)
        assertEquals(s, s.evaluateInput("わたしわ", 3, 0))
        assertEquals(1, s.evaluateInput(s.target, 4, 0).correct)
    }
    @Test fun prefixTracksEditsIncludingBacktrackingAndOverlongInput() {
        assertEquals(3, matchingPrefix("わたしわ", "わたしは"))
        assertEquals(2, matchingPrefix("わた", "わたしは"))
        assertEquals(3, firstMismatch("わたしわ", "わたしは"))
        assertNull(firstMismatch("わたし", "わたしは"))
        assertEquals(4, firstMismatch("わたしはあ", "わたしは"))
    }
    @Test fun skipDoesNotCountAsAttemptAndEndIsIdempotent() {
        val s = session().skip().skip()
        assertEquals(Page.RESULT, s.page); assertEquals(2, s.skipped)
        assertEquals(0, s.correct); assertEquals(0, s.errors); assertEquals(0.0, s.accuracy, 0.0)
        assertEquals(s, s.skip()); assertEquals(s, s.evaluateInput(s.target, 5, 2))
    }
    @Test fun fullArticleIsNotCappedAtTwenty() {
        val many = (1..35).map { words.first().copy(id = "s$it") }
        var s = PracticeState().start(many, true, "長い記事")
        repeat(35) { s = s.evaluateInput(s.target, it.toLong(), it) }
        assertEquals(35, s.correct); assertEquals(Page.RESULT, s.page)
    }
    @Test fun snapshotPreservesModeQuestionCountsAndRevision() {
        val s = session().skip().evaluateInput("まちがい", 7, 1)
        val restored = PracticeSnapshot.restore(PracticeSnapshot.save(s))
        assertEquals(s, restored)
        assertEquals(s, restored.evaluateInput("まちがい", 7, 1))
        assertEquals(Page.RESULT, restored.evaluateInput(restored.target, 8, 1).page)
    }
    @Test fun earlyExitAndRestartResetCountsWithoutLosingArticle() {
        val s = session().skip().finish()
        assertEquals(Page.RESULT, s.page)
        val restarted = s.start()
        assertTrue(restarted.sentenceMode); assertEquals("朝", restarted.articleTitle)
        assertEquals(0, restarted.index); assertEquals(0, restarted.skipped)
        assertEquals(PracticeState(), restarted.exit())
    }
    @Test fun priorWordSnapshotCanStillBeRestored() {
        val s = PracticeState().start(words).skip()
        val current = PracticeSnapshot.save(s)
        val legacy = current.take(8) + s.words.flatMap { listOf(it.id, it.text, it.reading, it.meaning, it.level) }
        assertEquals(s.copy(kana = com.hyakuri.kanaflick.proficiency.KanaProgress()), PracticeSnapshot.restore(legacy))
    }
    @Test fun packagedReadingsHaveValidAttributionAndOrderedKana() {
        val dir = File("src/main/assets")
        val readings = ReadingLibrary.load { File(dir, it).inputStream() }
        assertTrue(readings.isNotEmpty())
        readings.forEach { a ->
            assertTrue(a.author.isNotBlank()); assertTrue(a.adaptation.isNotBlank()); assertTrue(a.grading.isNotBlank())
            assertEquals(a.sentences.map { it.kana }, a.practiceWords().map { it.text })
            assertEquals(a.sentences.map { it.original }, a.practiceWords().map { it.reading })
            assertEquals(a.sentences.joinToString("\n") { it.original }, a.fullText)
        }
        assertTrue(readings.first { it.id == "window" }.sentences.size > 20)
    }
    @Test fun invalidAndDuplicateReadingsAreRejected() {
        val a = ReadingLibrary.load { File("src/main/assets", it).inputStream() }.first()
        assertThrows(IllegalArgumentException::class.java) { ReadingLibrary.validate(listOf(a, a)) }
        assertThrows(IllegalArgumentException::class.java) { ReadingLibrary.validate(listOf(a.copy(sentences = listOf(ReadingSentence("一日", "1にち"))))) }
        assertThrows(IllegalArgumentException::class.java) { ReadingLibrary.validate(listOf(a.copy(sentences = emptyList()))) }
    }
}
