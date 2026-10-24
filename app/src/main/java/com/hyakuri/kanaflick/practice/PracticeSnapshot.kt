package com.hyakuri.kanaflick.practice

import com.hyakuri.kanaflick.vocabulary.Word
import com.hyakuri.kanaflick.proficiency.*

/** Bundle-compatible snapshot shared by restoration and pure unit tests. */
object PracticeSnapshot {
    fun save(s: PracticeState): List<Any> = listOf(s.page.name, s.correct, s.errors, s.feedback.name,
        s.lastRevision, s.index, s.skipped, s.previousFeedback.name, s.sentenceMode, s.articleTitle, "kana-v2", s.kana.session, s.kana.generation,
        s.kana.errors.joinToString(","), s.kana.pending.joinToString(";") { it.encode() }, s.lastErrorKey) +
        s.words.flatMap { listOf(it.id, it.text, it.reading, it.meaning, it.level) }
    fun restore(v: List<Any>): PracticeState {
        val live = v.getOrNull(10) == "kana-v2"
        val hasKana = live || v.getOrNull(10) == "kana-v1"
        val hasMode = v.getOrNull(8) is Boolean
        return PracticeState(
        Page.valueOf(v[0] as String), v[1] as Int, v[2] as Int, Feedback.valueOf(v[3] as String),
        v.drop(if (live) 16 else if (hasKana) 15 else if (hasMode) 10 else 8).chunked(5).map { Word(it[0] as String, it[1] as String, it[2] as String, it[3] as String, it[4] as String) },
        v[4] as Long, v[5] as Int, v[6] as Int, Feedback.valueOf(v[7] as String),
        if (hasMode) v[8] as Boolean else false, if (hasMode) v[9] as String else "",
        if (hasKana) KanaProgress(v[11] as String, v[12] as String,
            (v[13] as String).split(',').mapNotNull { it.toIntOrNull() }.toSet(),
            (v[14] as String).split(';').filter { it.isNotEmpty() }.map(KanaBatch::decode)) else KanaProgress(),
        if (live) v[15] as String else "",
    )
    }
}
