package com.hyakuri.kanaflick.practice

import com.hyakuri.kanaflick.proficiency.KanaTokenizer
import com.hyakuri.kanaflick.vocabulary.Word

/** Listed surface form or a complete reliable reading; never guess kanji pronunciations. */
fun acceptedAnswers(word: Word, sentence: Boolean): List<String> =
    (listOf(word.text) + if (sentence || KanaTokenizer.tokenize(word.reading) != null) listOf(word.reading) else emptyList())
        .map { if (sentence) normalizeSentence(it) else normalizeAnswer(it) }.filter { it.isNotBlank() }.distinct()

data class LiveMatch(val complete: Boolean, val prefix: Int, val expected: String, val pendingConversion: Boolean = false, val validPrefix: Boolean = false) {
    fun errorKey(input: String): String = "$prefix:${foldKana(input).getOrNull(prefix)}"
}

/** Comparison only: keep the original spelling for display and stored vocabulary. */
fun foldKana(text: String): String = normalizeText(text).map {
    if (it in 'ァ'..'ヶ') (it.code - 0x60).toChar() else it
}.joinToString("")

@Suppress("UNUSED_PARAMETER") // IMEs disagree about whether intermediate text is composing.
fun liveMatch(answer: String, alternatives: List<String>, composing: Boolean): LiveMatch {
    val actual = foldKana(answer)
    alternatives.firstOrNull { foldKana(it) == actual }?.let { return LiveMatch(true, actual.length, it) }
    val expected = alternatives.maxByOrNull { matchingLength(actual, foldKana(it)) } ?: ""
    val prefix = matchingLength(actual, foldKana(expected))
    if (foldKana(expected).startsWith(actual)) return LiveMatch(false, prefix, expected, validPrefix = true)
    // A Japanese IME can expose k -> か, は -> ば/ぱ, つ -> っ, etc.
    // Defer only the unfinished final unit, not earlier mismatches or arbitrary wrong kana.
    val pending = alternatives.any { original ->
        val target = foldKana(original)
        val position = matchingLength(actual, target)
        val suffix = actual.drop(position)
        (suffix.isNotEmpty() && suffix.all(::isRomanLetter) && target.drop(position).any { it in '\u3041'..'\u30ff' }) ||
            (suffix.isNotEmpty() && suffix.drop(1).all(::isRomanLetter) &&
                target.getOrNull(position)?.let { canTransform(suffix[0], it) } == true)
    }
    return LiveMatch(false, prefix, expected, pending)
}

private fun isRomanLetter(c: Char) = c in 'a'..'z' || c in 'A'..'Z' || c in 'ａ'..'ｚ' || c in 'Ａ'..'Ｚ'
private fun matchingLength(a: String, b: String) = a.zip(b).takeWhile { it.first == it.second }.size
private fun canTransform(actual: Char, expected: Char): Boolean {
    fun hiragana(c: Char) = if (c in 'ァ'..'ヶ') (c.code - 0x60).toChar() else c
    val from = hiragana(actual); val to = hiragana(expected)
    val small = "あぁいぃうぅえぇおぉつっやゃゆゅよょわゎかゕけゖ".chunked(2).associate { it[0] to it[1] }
    if (small[from] == to) return true
    return listOf('\u3099', '\u309a').any { normalizeText("$from$it") == to.toString() }
}
