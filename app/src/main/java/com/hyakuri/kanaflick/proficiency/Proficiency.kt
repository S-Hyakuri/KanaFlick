package com.hyakuri.kanaflick.proficiency

import java.time.LocalDate
import java.util.UUID

data class KanaStat(val samples: Long = 0, val errors: Long = 0, val recent: Double = 0.0, val date: String = "") {
    val status: String get() = when {
        samples == 0L -> "未练习"
        samples < 8 -> "积累中 $samples/8"
        recent >= 0.30 -> "需加强"
        recent >= 0.10 -> "巩固中"
        else -> "较稳定"
    }
    val weak get() = samples >= 8 && recent >= 0.30
    fun add(error: Boolean, day: String): KanaStat {
        val x = if (error) 1.0 else 0.0
        return KanaStat(samples + 1, errors + if (error) 1 else 0, if (samples == 0L) x else 0.85 * recent + 0.15 * x, day)
    }
}

data class KanaBatch(val question: Int, val success: Set<String>, val errors: Set<String>, val date: String) {
    fun encode() = listOf(question.toString(), success.joinToString(","), errors.joinToString(","), date).joinToString("|")
    companion object {
        fun decode(text: String): KanaBatch {
            val p = text.split('|')
            fun units(s: String) = s.split(',').filter { it.isNotEmpty() }.toSet()
            return KanaBatch(p[0].toInt(), units(p[1]), units(p[2]), p[3])
        }
    }
}

/** Only current-question positions and bounded current-session settlement metadata; no input text. */
data class KanaProgress(
    val session: String = "", val generation: String = "",
    val errors: Set<Int> = emptySet(), val pending: List<KanaBatch> = emptyList(),
) {
    fun wrong(target: String, text: String): KanaProgress {
        val expected = KanaTokenizer.tokenize(target) ?: return this
        val actual = KanaTokenizer.tokenize(text) ?: return this
        val position = KanaAlignment.firstError(expected, actual) ?: return this
        return copy(errors = errors + position)
    }
    fun settle(target: String, question: Int, complete: Boolean, day: String = LocalDate.now().toString()): KanaProgress {
        val units = KanaTokenizer.tokenize(target).orEmpty()
        val failed = errors.mapNotNull { units.getOrNull(it) }.toSet()
        val passed = if (complete) units.toSet() - failed else emptySet()
        return copy(errors = emptySet(), pending = pending + KanaBatch(question, passed, failed, day))
    }
}

data class KanaProfile(
    val generation: String = UUID.randomUUID().toString(), val session: String = "", val cursor: Int = -1,
    val stats: Map<String, KanaStat> = emptyMap(),
) {
    fun commit(progress: KanaProgress): KanaProfile {
        if (generation != progress.generation || session != progress.session) return this
        var result = this
        progress.pending.sortedBy { it.question }.forEach { batch ->
            if (batch.question > result.cursor) {
                val next = result.stats.toMutableMap()
                (batch.success + batch.errors).forEach { unit ->
                    next[unit] = (next[unit] ?: KanaStat()).add(unit in batch.errors, batch.date)
                }
                result = result.copy(cursor = batch.question, stats = next)
            }
        }
        return result
    }
}
