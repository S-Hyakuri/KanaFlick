package com.hyakuri.kanaflick.practice

/** Sentence layout spaces never require invisible input; word matching remains unchanged. */
fun normalizeSentence(value: String): String = normalizeAnswer(value).filterNot {
    it.isWhitespace() || it == '\u200B' || it == '\uFEFF'
}

fun matchingPrefix(input: String, target: String): Int {
    val actual = foldKana(normalizeSentence(input))
    val expected = foldKana(normalizeSentence(target))
    return actual.zip(expected).takeWhile { (a, b) -> a == b }.size
}

fun firstMismatch(input: String, target: String): Int? {
    val actual = normalizeSentence(input)
    val expected = normalizeSentence(target)
    val prefix = matchingPrefix(actual, expected)
    return if (prefix < actual.length) prefix else null
}
