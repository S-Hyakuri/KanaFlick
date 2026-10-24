package com.hyakuri.kanaflick.proficiency

import com.hyakuri.kanaflick.vocabulary.*
import kotlin.random.Random

object TargetedVocabulary {
    fun message(words: List<Word>, stats: Map<String, KanaStat>): String = when {
        stats.values.none { it.weak } -> "正在积累熟练度，本局按普通方式出题"
        words.none { weight(it, stats) > 1.0 } -> "当前词库暂无对应词汇"
        else -> "优先安排薄弱假名，保留随机探索；从下一局生效"
    }
    fun weight(word: Word, stats: Map<String, KanaStat>): Double = 1.0 + 3.0 *
        (KanaTokenizer.tokenize(word.reading)?.mapNotNull { stats[it]?.takeIf { s -> s.weak }?.recent }?.maxOrNull() ?: 0.0)

    fun select(words: List<Word>, size: Int, stats: Map<String, KanaStat>, previous: Set<String> = emptySet(), random: Random = Random.Default): List<Word> {
        require(size in VocabularyStudy.roundSizes)
        val unique = PracticeVocabulary.filter(words).distinctBy { it.id }
        val k = minOf(size, unique.size)
        val fresh = unique.filterNot { it.id in previous }
        val pool = (fresh + unique.filter { it.id in previous }.shuffled(random).take((k - fresh.size).coerceAtLeast(0))).toMutableList()
        val selected = mutableListOf<Word>()
        val weighted = pool.map { it to weight(it, stats) }.filter { it.second > 1.0 }.toMutableList()
        repeat((k * 0.6).toInt()) {
            if (weighted.isNotEmpty()) {
                var ticket = random.nextDouble() * weighted.sumOf { it.second }
                val choice = weighted.firstOrNull { ticket -= it.second; ticket < 0 }?.first ?: weighted.last().first
                selected += choice; pool.remove(choice); weighted.removeAll { it.first.id == choice.id }
            }
        }
        selected += pool.shuffled(random).take(k - selected.size)
        return selected.shuffled(random)
    }
}
