package com.hyakuri.kanaflick

import com.hyakuri.kanaflick.practice.*
import com.hyakuri.kanaflick.vocabulary.*
import org.junit.Assert.*
import org.junit.Test

class PracticeV050Test {
    @Test fun skipAdvancesWithoutScoringAndRejectsOldQuestion() {
        val initial = PracticeState().start()
        val skipped = initial.skip()
        assertEquals(1, skipped.index); assertEquals(1, skipped.skipped)
        assertEquals(0, skipped.correct); assertEquals(0, skipped.errors)
        assertEquals(skipped, skipped.submit(initial.target, 10, 0))
        val next = skipped.submit(skipped.target, 11, 1)
        assertEquals(2, next.index); assertEquals(1, next.correct)
        assertEquals(Page.RESULT, next.skip().page)
    }
    @Test fun wrongThenSkipPreservesExactlyOneError() {
        val wrong = PracticeState().start().evaluateInput("誤答", 1, 0)
        assertEquals(0, wrong.index)
        val skipped = wrong.skip()
        assertEquals(1, skipped.errors); assertEquals(1, skipped.skipped)
        assertEquals(Feedback.SKIPPED, skipped.previousFeedback)
        assertEquals(0.0, skipped.accuracy, 0.0)
    }
    @Test fun earlyFinishPreservesScoresAndCannotScoreAfterwards() {
        val active = PracticeState().start().submit("学校", 1, 0)
        val ended = active.finish()
        assertEquals(Page.RESULT, ended.page); assertEquals(1, ended.correct)
        assertEquals(ended, ended.submit(ended.target, 2, ended.index))
        assertEquals(ended, ended.skip())
        assertEquals(0, ended.start().index)
        assertEquals(0.0, PracticeState().start().finish().accuracy, 0.0)
    }
    @Test fun punctuationIsIgnoredButJapaneseSoundsArePreserved() {
        val initial = PracticeState().start(listOf(Samples.words.first().copy(text = "「コーヒー」！")))
        assertEquals(Page.RESULT, initial.evaluateInput("コーヒー", 1, 0).page)
        assertEquals(initial, initial.evaluateInput("コー", 1, 0))
        assertEquals(1, initial.evaluateInput("コヒ", 1, 0).errors)
        assertEquals(initial, initial.evaluateInput("！？", 1, 0))
        assertEquals(Page.RESULT, initial.evaluateInput("コーヒー", 1, 0, true).page)
        assertEquals("学校", normalizeAnswer("「学校」★😀"))
        assertNotEquals(WordRules.key(Samples.words.first()), WordRules.key(Samples.words.first().copy(text = "「学校」")))
    }
    @Test fun skipEveryWordFinishesWithZeroAccuracy() {
        var state = PracticeState().start()
        repeat(state.words.size) { state = state.skip() }
        assertEquals(Page.RESULT, state.page)
        assertEquals(0.0, state.accuracy, 0.0)
        assertEquals(state.words.size, state.skipped)
    }
}