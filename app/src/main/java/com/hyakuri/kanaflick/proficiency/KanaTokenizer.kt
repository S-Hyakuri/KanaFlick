package com.hyakuri.kanaflick.proficiency

import java.text.Normalizer

object KanaTokenizer {
    val groups: Map<String, List<String>> = linkedMapOf(
        "基础音" to "あいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよらりるれろわゐゑをん".map { it.toString() },
        "浊音与半浊音" to "がぎぐげござじずぜぞだぢづでどばびぶべぼぱぴぷぺぽゔ".map { it.toString() },
        "拗音与外来音" to (listOf("き", "ぎ", "し", "じ", "ち", "ぢ", "に", "ひ", "び", "ぴ", "み", "り").flatMap { base -> listOf("ゃ", "ゅ", "ょ").map { base + it } } +
            "いぇ うぃ うぇ うぉ くぁ くぃ くぇ くぉ ぐぁ ぐぃ ぐぇ ぐぉ しぇ じぇ ちぇ つぁ つぃ つぇ つぉ てぃ てゅ とぅ でぃ でゅ どぅ ふぁ ふぃ ふぇ ふぉ ふゅ ゔぁ ゔぃ ゔぇ ゔぉ ゔゅ".split(" ")),
        "小假名与长音" to "ぁぃぅぇぉゃゅょっゎゕゖー".map { it.toString() },
    )
    val units = groups.values.flatten().toSet()
    /** Reject the whole target if any non-kana remains. Never infer a reading. */
    fun tokenize(text: String): List<String>? {
        val normalized = buildString {
            Normalizer.normalize(text, Normalizer.Form.NFC).forEach { c ->
                val type = Character.getType(c)
                if (c.isWhitespace() || Character.isSpaceChar(c) || type in punctuation) return@forEach
                append(if (c in 'ァ'..'ヶ') (c.code - 0x60).toChar() else c)
            }
        }
        val result = mutableListOf<String>()
        var i = 0
        while (i < normalized.length) {
            val pair = normalized.substring(i, (i + 2).coerceAtMost(normalized.length))
            val unit = if (pair.length == 2 && pair in units) pair else normalized[i].toString()
            if (unit !in units) return null
            result += unit; i += unit.length
        }
        return result.takeIf { it.isNotEmpty() }
    }
    private val punctuation = setOf(20, 21, 22, 23, 24, 29, 30)
}

/** Semi-global edit alignment: an untyped target suffix costs nothing.
 * Track all optimal paths, rejecting any ambiguous alignment rather than guessing.
 */
object KanaAlignment {
    fun firstError(target: List<String>, input: List<String>): Int? {
        if (target.size > 1024 || input.size > 1024 || input.isEmpty()) return null
        val n = target.size; val m = input.size
        val cost = Array(n + 1) { IntArray(m + 1) }
        val ways = Array(n + 1) { IntArray(m + 1) }
        for (i in 0..n) { cost[i][0] = i; ways[i][0] = 1 }
        for (j in 0..m) { cost[0][j] = j; ways[0][j] = 1 }
        for (i in 1..n) for (j in 1..m) {
            val diagonal = cost[i - 1][j - 1] + if (target[i - 1] == input[j - 1]) 0 else 1
            val best = minOf(diagonal, cost[i - 1][j] + 1, cost[i][j - 1] + 1)
            cost[i][j] = best
            ways[i][j] = ((if (diagonal == best) ways[i - 1][j - 1] else 0) +
                (if (cost[i - 1][j] + 1 == best) ways[i - 1][j] else 0) +
                (if (cost[i][j - 1] + 1 == best) ways[i][j - 1] else 0)).coerceAtMost(2)
        }
        val best = (0..n).minOf { cost[it][m] }
        val ends = (0..n).filter { cost[it][m] == best }
        if (best == 0 || ends.size != 1 || ways[ends.single()][m] != 1) return null
        var i = ends.single(); var j = m
        var first: Int? = null
        while (i > 0 || j > 0) {
            if (i > 0 && j > 0 && cost[i][j] == cost[i - 1][j - 1] + if (target[i - 1] == input[j - 1]) 0 else 1) {
                if (target[i - 1] != input[j - 1]) first = i - 1
                i--; j--
            } else if (i > 0 && cost[i][j] == cost[i - 1][j] + 1) { first = i - 1; i-- }
            else { j-- }
        }
        return first
    }
}
