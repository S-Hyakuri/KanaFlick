package com.hyakuri.kanaflick.vocabulary

import kotlin.random.Random

object VocabularyStudy {
    val roundSizes = listOf(5, 10, 20)
    fun select(words: List<Word>, size: Int, random: Random = Random.Default): List<Word> {
        require(size in roundSizes)
        return PracticeVocabulary.filter(words).shuffled(random).take(size)
    }
}
