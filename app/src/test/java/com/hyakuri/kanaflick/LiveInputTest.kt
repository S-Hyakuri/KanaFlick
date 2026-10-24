package com.hyakuri.kanaflick

import com.hyakuri.kanaflick.practice.*
import com.hyakuri.kanaflick.proficiency.*
import com.hyakuri.kanaflick.vocabulary.*
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import kotlin.random.Random

class LiveInputTest {
    private fun state(text: String = "学校", reading: String = "がっこう", sentences: Boolean = false) =
        PracticeState().start(listOf(Word("test", text, reading, "学校", "N5")), sentences)
    @Test fun composingKanaCompletesWithoutCandidateConfirmation() {
        var s = state()
        listOf("が", "がっ", "がっこ").forEachIndexed { i, text ->
            s = s.evaluateInput(text, i.toLong(), 0, true)
            assertEquals(0, s.errors); assertEquals(0, s.correct)
            assertEquals(text.length, s.match(text, true).prefix)
        }
        s = s.evaluateInput("がっこう", 4, 0, true)
        assertEquals(1, s.correct); assertEquals(Page.RESULT, s.page)
        assertEquals(s, s.evaluateInput("学校", 5, 0))
    }
    @Test fun bothListedKanjiAndKanaAreAccepted() {
        assertEquals(1, state().evaluateInput("学校", 1, 0).correct)
        assertEquals(1, state().evaluateInput("がっこう", 1, 0).correct)
        assertEquals(1, state("がっこうへいきます。", "学校へ行きます。", true).evaluateInput("学校へ行きます。", 1, 0).correct)
        assertEquals(1, state("がっこうへいきます。", "学校へ行きます。", true).evaluateInput("がっこうへいきます", 1, 0, true).correct)
        assertEquals(1, state().evaluateInput("学こう", 1, 0).errors) // No arbitrary mixed-spelling inference.
    }
    @Test fun correctPrefixesDoNotScoreButWrongKanaIsImmediate() {
        val s = state().evaluateInput("が", 1, 0, true)
        val wrong = s.evaluateInput("がに", 2, 0, true)
        assertEquals(1, wrong.errors); assertEquals(0, wrong.index)
        val extended = wrong.evaluateInput("がにこ", 3, 0, true)
        assertEquals(1, extended.errors) // Do not punish each following character for one unchanged mismatch.
        val fixedPrefix = extended.evaluateInput("がっ", 4, 0, true)
        assertEquals(Feedback.NONE, fixedPrefix.feedback)
        assertEquals(2, fixedPrefix.evaluateInput("がに", 5, 0, true).errors)
    }
    @Test fun smallKanaAndVoicingIntermediateFormsDoNotCountAsErrors() {
        assertEquals(0, state().evaluateInput("か", 1, 0, true).errors)
        assertEquals(0, state().evaluateInput("がつ", 2, 0, true).errors)
        assertEquals(1, state().evaluateInput("がつこ", 3, 0, true).errors)
        assertEquals(0, state("ぱん", "ぱん").evaluateInput("は", 1, 0, true).errors)
        assertEquals(0, state("きゃく", "きゃく").evaluateInput("きや", 1, 0, true).errors)
        assertEquals(0, state().evaluateInput("か", 1, 0, false).errors)
    }
    @Test fun romanCompositionWaitsOnlyForConversionNotForCandidateSelection() {
        assertEquals(0, state().evaluateInput("g", 1, 0, true).errors)
        assertEquals(0, state().evaluateInput("がっk", 2, 0, true).errors)
        assertEquals(1, state().evaluateInput("がにk", 3, 0, true).errors)
        assertEquals(0, state().evaluateInput("g", 1, 0, false).errors)
    }
    @Test fun cursorOnlyAndOldQuestionCannotProduceAnotherSample() {
        val s = state("がっこう", "がっこう").evaluateInput("がにこう", 1, 0, true)
        assertEquals(s, s.evaluateInput("がにこう", 1, 0, false))
        assertEquals(s, s.evaluateInput("がっこう", 2, 1, true))
        val restored = PracticeSnapshot.restore(PracticeSnapshot.save(s))
        assertEquals(s, restored)
        assertEquals(1, restored.evaluateInput("がにこう", 2, 0).errors)
    }
    @Test fun kanaSamplesDoNotGetInventedFromKanjiCompletion() {
        val kanji = state("がっこう", "学校", true).evaluateInput("学校", 1, 0)
        assertTrue(kanji.kana.pending.single().success.isEmpty())
        val kana = state("がっこう", "学校", true).evaluateInput("がっこう", 1, 0, true)
        assertEquals(setOf("が", "っ", "こ", "う"), kana.kana.pending.single().success)
        assertTrue(state().evaluateInput("がっこう", 1, 0, true).kana.pending.single().success.isEmpty())
    }
    @Test fun symbolFilterKeepsLongSoundAndOriginalStoredData() {
        val good = Word("good", "コーヒー", "コーヒー", "咖啡（说明可含符号）", "N5")
        val bad = listOf("〜する", "A・B", "猫（ねこ）", "猫/犬", "猫★", "😀", "a-b").mapIndexed { i, t -> good.copy(id = "b$i", text = t) }
        val all = listOf(good) + bad + good.copy(id = "reading", reading = "こーひー/コーヒー")
        assertEquals(listOf(good), PracticeVocabulary.filter(all))
        assertEquals(listOf(good), VocabularyStudy.select(all, 20, Random(1)))
        assertEquals(listOf(good), TargetedVocabulary.select(all, 20, emptyMap(), random = Random(1)))
        assertEquals(9, all.size)
    }
    @Test fun legacyV090SnapshotStillRestores() {
        val s = state("がっこう", "がっこう").evaluateInput("がにこう", 1, 0)
        val latest = PracticeSnapshot.save(s)
        val old = latest.take(10) + listOf("kana-v1") + latest.subList(11, 15) + latest.drop(16)
        assertEquals(s.copy(lastErrorKey = ""), PracticeSnapshot.restore(old))
    }
    @Test fun packagedSymbolExclusionAudit() {
        val libraries = BuiltInVocabulary.load { File("src/main/assets", it).inputStream() }
        val report = File("build/reports/symbol-filter.tsv")
        report.parentFile!!.mkdirs()
        report.writeText("library\toriginal\teligible\texcluded\n" + libraries.joinToString("\n") {
            val filtered = PracticeVocabulary.filter(it.words)
            "${it.id}\t${it.words.size}\t${filtered.size}\t${it.words.size - filtered.size}"
        })
        assertTrue(libraries.all { PracticeVocabulary.filter(it.words).isNotEmpty() })
    }
}
