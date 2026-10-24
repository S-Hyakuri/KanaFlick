package com.hyakuri.kanaflick.vocabulary

/** Exclude a whole entry, rather than silently changing its spelling or deleting user data. */
object PracticeVocabulary {
    private val excluded = setOf(
        Character.CONNECTOR_PUNCTUATION.toInt(), Character.DASH_PUNCTUATION.toInt(),
        Character.START_PUNCTUATION.toInt(), Character.END_PUNCTUATION.toInt(),
        Character.INITIAL_QUOTE_PUNCTUATION.toInt(), Character.FINAL_QUOTE_PUNCTUATION.toInt(),
        Character.OTHER_PUNCTUATION.toInt(), Character.MATH_SYMBOL.toInt(),
        Character.CURRENCY_SYMBOL.toInt(), Character.MODIFIER_SYMBOL.toInt(), Character.OTHER_SYMBOL.toInt(),
    )
    fun hasSymbols(value: String) = value.codePoints().anyMatch { it != 'ー'.code && Character.getType(it) in excluded }
    fun eligible(word: Word) = !hasSymbols(word.text) && !hasSymbols(word.reading)
    fun filter(words: List<Word>) = words.filter(::eligible)
}
