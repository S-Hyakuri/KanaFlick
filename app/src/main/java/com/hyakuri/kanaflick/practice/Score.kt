package com.hyakuri.kanaflick.practice

import java.util.Locale

object Score {
    fun accuracy(correct: Int, errors: Int): Double {
        require(correct >= 0 && errors >= 0)
        val attempts = correct.toLong() + errors.toLong()
        return if (attempts == 0L) 0.0 else correct.toDouble() / attempts * 100
    }
    fun formatAccuracy(value: Double): String = String.format(Locale.ROOT, "%.2f%%", value)
}
