package com.hyakuri.kanaflick
import com.hyakuri.kanaflick.practice.*
import org.junit.Assert.*
import org.junit.Test
class PracticeStateTest {
    @Test fun explicitSubmissionAndCompletion() {
        var s = PracticeState().start()
        repeat(s.words.size) { s = s.submit(s.target, it.toLong(), s.correct) }
        assertEquals(Page.RESULT, s.page); assertEquals(100.0, s.accuracy, 0.0)
        assertEquals(s, s.submit(s.target, 100, s.correct))
    }
    @Test fun compositionCompletesButBlankAndStaleEventsDoNotScore() {
        val s = PracticeState().start()
        assertEquals(1, s.submit("学校", 1, 0, true).correct); assertEquals(s, s.submit("　 ", 1, 0)); assertEquals(s, s.submit("学校", 1, 1))
    }
    @Test fun wrongStaysAndDoubleSubmitIsIgnored() {
        val s = PracticeState().start().submit("がつこう", 1, 0)
        assertEquals(0, s.correct); assertEquals(1, s.errors); assertEquals(s, s.submit("がつこう", 1, 0))
        val next = s.submit("学校", 2, 0)
        assertEquals(1, next.correct); assertEquals(next, next.submit("学校", 2, 0))
    }
    @Test fun normalizationIsConservative() {
        assertEquals(normalizeAnswer("が"), normalizeAnswer("か\u3099"))
        assertNotEquals(normalizeAnswer("学校"), normalizeAnswer(" 学校"))
        assertNotEquals(normalizeAnswer("が"), normalizeAnswer("ガ")); assertNotEquals(normalizeAnswer("Ａ"), normalizeAnswer("A"))
    }
    @Test fun restartExitAndZero() {
        val s = PracticeState().start().submit("x", 1, 0)
        assertEquals(PracticeState().start(), s.start()); assertEquals(PracticeState(), s.exit())
        assertEquals(0.0, Score.accuracy(0,0), 0.0); assertEquals(50.0, Score.accuracy(Int.MAX_VALUE,Int.MAX_VALUE), 0.001)
    }
    @Test fun automaticInputChecksPrefixAndComposition() {
        val s = PracticeState().start()
        assertEquals(s, s.evaluateInput("学", 1, 0))
        assertEquals(1, s.evaluateInput("学校", 2, 0, composing = true).correct)
        assertEquals(1, s.evaluateInput("学校", 2, 0).correct)
    }
    @Test fun automaticWrongRevisionScoresOnlyOnceAndCanBeCorrected() {
        val s = PracticeState().start().evaluateInput("がつこう", 1, 0)
        assertEquals(1, s.errors)
        assertEquals(s, s.evaluateInput("がつこう", 1, 0))
        assertEquals(s, s.evaluateInput("学校", 2, 1))
        val next = s.evaluateInput("学校", 2, 0)
        assertEquals(1, next.correct)
        assertEquals(1, next.errors)
        assertEquals(next, next.evaluateInput("学校", 2, 0))
    }}
