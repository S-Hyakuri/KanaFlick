package com.hyakuri.kanaflick

import com.hyakuri.kanaflick.proficiency.*
import com.hyakuri.kanaflick.practice.*
import com.hyakuri.kanaflick.vocabulary.*
import org.junit.Assert.*
import org.junit.Test
import java.nio.file.Files
import java.io.File
import kotlin.random.Random

class ProficiencyTest {
    private fun session(text: String = "がっこう") = PracticeState().start(listOf(Word("kana", text, text, "", "N5")))
    private fun align(target: String, input: String) = KanaAlignment.firstError(KanaTokenizer.tokenize(target)!!, KanaTokenizer.tokenize(input)!!)

    @Test fun tokenizerNfcKatakanaCombinationsAndStandaloneSmallKana() {
        assertEquals(listOf("が", "っ", "こ", "う"), KanaTokenizer.tokenize("カ\u3099ッコウ。　"))
        assertEquals(listOf("きゃ", "しゅ", "ちょ", "てぃ", "ふぁ", "ゃ", "ん", "ー"), KanaTokenizer.tokenize("キャシュチョティファゃんー"))
        assertEquals(listOf("か", "ゃ"), KanaTokenizer.tokenize("かゃ"))
    }
    @Test fun unsupportedTargetsAreRejectedAsAWhole() {
        listOf("学校", "かa", "か1", "か☆", "ｶﾅ", "か🙂", "", "。 ").forEach { assertNull(it, KanaTokenizer.tokenize(it)) }
    }
    @Test fun explicitTableHasNoDuplicateUnits() {
        assertEquals(KanaTokenizer.groups.values.sumOf { it.size }, KanaTokenizer.units.size)
        KanaTokenizer.units.forEach { assertEquals(listOf(it), KanaTokenizer.tokenize(it)) }
    }
    @Test fun uniqueSubstitutionAndInternalDeletion() {
        assertEquals(1, align("がっこう", "がつこう"))
        assertEquals(1, align("がっこう", "がこう"))
    }
    @Test fun trailingOmissionInsertionAndAmbiguityAreNotErrors() {
        assertNull(align("がっこう", "がっ"))
        assertNull(align("がっこう", "がっこうあ"))
        assertNull(align("かかき", "かき"))
        assertNull(align("かき", "かく")) // trailing replacement vs insertion after a correct prefix
    }
    @Test fun onlyAcceptedEventsContribute() {
        val s = session()
        assertEquals(1, s.evaluateInput("がつこう", 1, 0, composing = true).errors)
        assertEquals(s, s.evaluateInput("がっ", 1, 0))
        assertEquals(s, s.evaluateInput("", 1, 0))
        assertEquals(s, s.evaluateInput("がつこう", 1, 1))
        val wrong = s.evaluateInput("がつこう", 2, 0)
        assertEquals(setOf(1), wrong.kana.errors)
        assertEquals(wrong, wrong.evaluateInput("がつこう", 2, 0))
        assertEquals(1, wrong.errors)
    }
    @Test fun correctionDoesNotRewardFailedUnitTwice() {
        val result = session().evaluateInput("がつこう", 1, 0).evaluateInput("がづこう", 2, 0).evaluateInput("がっこう", 3, 0)
        val batch = result.kana.pending.single()
        assertEquals(setOf("っ"), batch.errors)
        assertEquals(setOf("が", "こ", "う"), batch.success)
        assertEquals(2, result.errors); assertEquals(1, result.correct)
    }
    @Test fun repeatedKanaContributesOneSamplePerQuestion() {
        val result = session("かきかき").evaluateInput("かくかき", 1, 0).evaluateInput("かきかき", 2, 0)
        assertEquals(setOf("き"), result.kana.pending.single().errors)
        assertEquals(setOf("か"), result.kana.pending.single().success)
    }
    @Test fun skipAndExitOnlySettleKnownErrors() {
        val s = session().evaluateInput("がつこう", 1, 0)
        for (result in listOf(s.skip(), s.finish())) {
            assertEquals(setOf("っ"), result.kana.pending.single().errors)
            assertTrue(result.kana.pending.single().success.isEmpty())
        }
        assertTrue(session().evaluateInput("がっ", 1, 0).skip().kana.pending.single().errors.isEmpty())
        assertEquals(s.finish(), s.finish().finish())
    }
    @Test fun kanjiAndNonKanaMistakesNeverInventKanaErrors() {
        assertTrue(session("学校").evaluateInput("がっこう", 1, 0).evaluateInput("学校", 2, 0).kana.pending.single().success.isEmpty())
        assertTrue(session().evaluateInput("学校", 1, 0).finish().kana.pending.single().errors.isEmpty())
    }
    @Test fun scoreBoundariesAndRecentDecay() {
        assertEquals("未练习", KanaStat().status)
        assertEquals("积累中 7/8", KanaStat(7, 7, 1.0).status)
        assertEquals("需加强", KanaStat(8, 3, .30).status)
        assertEquals("巩固中", KanaStat(8, 1, .10).status)
        assertEquals("较稳定", KanaStat(8, 0, .099).status)
        var s = KanaStat().add(true, "2026-10-23")
        assertEquals(1.0, s.recent, 0.0)
        s = s.add(false, "2036-10-23")
        assertEquals(.85, s.recent, 1e-12)
        repeat(20) { s = s.add(false, "2036-10-23") }
        assertEquals("较稳定", s.status); assertEquals(1L, s.errors)
    }
    @Test fun snapshotPreservesErrorPositionsAndPendingSettlements() {
        val s = session().evaluateInput("がつこう", 1, 0).copy(kana = session().kana.copy(session = "test", generation = "generation", errors = setOf(1)))
        assertEquals(s, PracticeSnapshot.restore(PracticeSnapshot.save(s)))
        val finished = s.finish()
        assertEquals(finished, PracticeSnapshot.restore(PracticeSnapshot.save(finished)))
    }
    @Test fun commitIsIdempotentAndRejectsOldSessionOrGeneration() {
        val p = KanaProfile(generation = "g", session = "s")
        val event = KanaProgress("s", "g", pending = listOf(KanaBatch(0, setOf("あ"), setOf("っ"), "2026-10-23")))
        val saved = p.commit(event)
        assertEquals(1L, saved.stats.getValue("あ").samples)
        assertEquals(saved, saved.commit(event))
        assertEquals(saved, saved.commit(event.copy(session = "old")))
        assertEquals(saved, saved.commit(event.copy(generation = "old")))
        val reset = KanaProfile(generation = "new")
        assertEquals(reset, reset.commit(event))
    }
    @Test fun atomicRoundTripAndCorruptFilePreserved() {
        val directory = Files.createTempDirectory("kana-profile-test").toFile()
        try {
            val store = ProfileStore(directory)
            val profile = KanaProfile("g", "s", 2, mapOf("あ" to KanaStat(8, 3, .4, "2026-10-23")))
            store.save(profile); assertEquals(profile, ProfileStore(directory).load())
            store.file.writeBytes(byteArrayOf(1, 2, 3))
            assertThrows(Exception::class.java) { store.load() }
            assertArrayEquals(byteArrayOf(1, 2, 3), store.file.readBytes())
        } finally { directory.deleteRecursively() }
    }
    @Test fun unsupportedVersionPreservesOriginalAndFailedWriteKeepsSavedProfile() {
        val directory = Files.createTempDirectory("kana-profile-test").toFile()
        try {
            val store = ProfileStore(directory); val profile = KanaProfile()
            store.save(profile)
            val bytes = store.file.readBytes(); bytes[7] = 2; store.file.writeBytes(bytes)
            assertThrows(Exception::class.java) { store.load() }; assertArrayEquals(bytes, store.file.readBytes())
            store.save(profile)
            assertThrows(Exception::class.java) { store.save(profile.copy(session = "x".repeat(70000))) }
            assertEquals(profile, store.load())
        } finally { directory.deleteRecursively() }
    }
    @Test fun failedQueueRetainsEventsAndRetriesWithoutDoubleCounting() {
        var fail = true
        var persisted = KanaProfile("g", "s")
        val writer = ProfileWriteQueue(persisted) { if (fail) error("disk full") else persisted = it }
        val progress = KanaProgress("s", "g", pending = listOf(KanaBatch(0, setOf("あ"), emptySet(), "2026-10-23")))
        assertThrows(IllegalStateException::class.java) { writer.accept(ProfileCommand.Commit(progress)) }
        assertTrue(writer.hasPending); assertTrue(writer.profile.stats.isEmpty())
        fail = false; writer.accept(ProfileCommand.Retry)
        writer.accept(ProfileCommand.Commit(progress))
        assertFalse(writer.hasPending); assertEquals(1L, persisted.stats.getValue("あ").samples)
    }
    @Test fun resetClearsFailedPendingAndRejectsLateOldEvents() {
        var fail = true
        val writer = ProfileWriteQueue(KanaProfile("g", "s")) { if (fail) error("disk full") }
        val progress = KanaProgress("s", "g", pending = listOf(KanaBatch(0, emptySet(), setOf("あ"), "2026-10-23")))
        assertThrows(IllegalStateException::class.java) { writer.accept(ProfileCommand.Commit(progress)) }
        assertThrows(IllegalStateException::class.java) { writer.accept(ProfileCommand.Reset("new")) }
        fail = false; writer.accept(ProfileCommand.Retry)
        writer.accept(ProfileCommand.Commit(progress)); writer.accept(ProfileCommand.Begin(progress))
        assertEquals("new", writer.profile.generation); assertTrue(writer.profile.stats.isEmpty())
    }
    @Test fun newSessionFlushesEarlierPendingBeforeReplacingCursor() {
        var fail = true
        val writer = ProfileWriteQueue(KanaProfile("g", "old")) { if (fail) error("disk full") }
        val old = KanaProgress("old", "g", pending = listOf(KanaBatch(0, setOf("あ"), emptySet(), "2026-10-23")))
        assertThrows(IllegalStateException::class.java) { writer.accept(ProfileCommand.Commit(old)) }
        val fresh = old.copy(session = "new")
        assertThrows(IllegalStateException::class.java) { writer.accept(ProfileCommand.Begin(fresh)) }
        fail = false; writer.accept(ProfileCommand.Commit(fresh))
        writer.accept(ProfileCommand.Commit(old.copy(pending = listOf(old.pending.single().copy(question = 1)))))
        assertEquals("new", writer.profile.session); assertEquals(2L, writer.profile.stats.getValue("あ").samples)
    }
    private val words = (0..99).map { Word("w$it", "語$it", if (it < 10) "かき" else "さし", "", "N5") }
    private val weak = mapOf("か" to KanaStat(8, 8, 1.0, "2026-10-23"))
    @Test fun samplingStaysInPoolAndAvoidsPreviousWhenPossible() {
        val previous = words.take(10).map { it.id }.toSet()
        val selected = TargetedVocabulary.select(words, 10, weak, previous, Random(9))
        assertEquals(10, selected.size); assertEquals(10, selected.map { it.id }.toSet().size)
        assertTrue(selected.all { it in words && it.id !in previous })
        assertEquals(3, TargetedVocabulary.select(words.take(3) + words.first(), 20, weak, previous, Random(2)).size)
    }
    @Test fun lengthDoesNotMultiplyWeightAndInvalidReadingsStayRandom() {
        assertEquals(4.0, TargetedVocabulary.weight(words[0].copy(reading = "かかかかか"), weak), 0.0)
        assertEquals(1.0, TargetedVocabulary.weight(words[0].copy(reading = "学校"), weak), 0.0)
        assertEquals(1.0, TargetedVocabulary.weight(words[0], mapOf("か" to KanaStat(7, 7, 1.0))), 0.0)
    }
    @Test fun targetedDistributionExceedsSeededRandomBaseline() {
        var targeted = 0; var baseline = 0
        repeat(500) { seed ->
            targeted += TargetedVocabulary.select(words, 10, weak, random = Random(seed)).count { it.reading == "かき" }
            baseline += VocabularyStudy.select(words, 10, Random(seed)).count { it.reading == "かき" }
        }
        assertTrue("targeted=$targeted baseline=$baseline", targeted > baseline * 3)
    }
    @Test fun fallbackMessagesAndExplorationQuota() {
        assertTrue(TargetedVocabulary.message(words, emptyMap()).startsWith("正在积累"))
        assertEquals("当前词库暂无对应词汇", TargetedVocabulary.message(words.drop(10), weak))
        val selected = TargetedVocabulary.select(words, 10, weak, random = Random(42))
        assertTrue(selected.count { it.reading == "かき" } >= 6)
        assertEquals(10, TargetedVocabulary.select(words, 10, emptyMap(), random = Random(1)).size)
    }
    @Test fun builtInCoverageEvidence() {
        val root = File("src/main/assets")
        val libraries = BuiltInVocabulary.load { File(root, it).inputStream() }
        val lines = libraries.map { lib ->
            val targets = lib.words.count { KanaTokenizer.tokenize(it.text) != null }
            val readings = lib.words.count { KanaTokenizer.tokenize(it.reading) != null }
            "${lib.id}\t${lib.words.size}\t$targets\t$readings"
        }
        val report = File("build/reports/proficiency-coverage.tsv")
        report.parentFile!!.mkdirs(); report.writeText("library\ttotal\tstatistical_targets\trecommendable_readings\n" + lines.joinToString("\n"))
        assertTrue(libraries.isNotEmpty())
    }
}
