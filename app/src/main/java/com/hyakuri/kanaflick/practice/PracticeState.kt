package com.hyakuri.kanaflick.practice
import com.hyakuri.kanaflick.vocabulary.*
import java.text.Normalizer
import com.hyakuri.kanaflick.proficiency.KanaProgress
import com.hyakuri.kanaflick.proficiency.KanaTokenizer

enum class Page { HOME, PRACTICE, RESULT }
enum class Feedback { NONE, CORRECT, WRONG, SKIPPED }
/** Ignore punctuation and symbols, retaining letters (including ー), marks and spacing. */
fun normalizeText(value: String): String = Normalizer.normalize(value, Normalizer.Form.NFC)
fun normalizeAnswer(value: String): String = buildString {
    normalizeText(value).codePoints().forEach { point ->
        if (Character.getType(point) !in ignoredAnswerTypes) appendCodePoint(point)
    }
}
private val ignoredAnswerTypes = setOf(
    Character.CONNECTOR_PUNCTUATION.toInt(), Character.DASH_PUNCTUATION.toInt(),
    Character.START_PUNCTUATION.toInt(), Character.END_PUNCTUATION.toInt(),
    Character.INITIAL_QUOTE_PUNCTUATION.toInt(), Character.FINAL_QUOTE_PUNCTUATION.toInt(),
    Character.OTHER_PUNCTUATION.toInt(), Character.MATH_SYMBOL.toInt(),
    Character.CURRENCY_SYMBOL.toInt(), Character.MODIFIER_SYMBOL.toInt(), Character.OTHER_SYMBOL.toInt(),
)
data class PracticeState(
    val page: Page = Page.HOME, val correct: Int = 0, val errors: Int = 0,
    val feedback: Feedback = Feedback.NONE, val words: List<Word> = Samples.words, val lastRevision: Long = -1,
    val index: Int = correct, val skipped: Int = 0, val previousFeedback: Feedback = Feedback.NONE,
    val sentenceMode: Boolean = false, val articleTitle: String = "",
    val kana: KanaProgress = KanaProgress(), val lastErrorKey: String = "",
) {
    init { require(words.isNotEmpty()); require(correct in 0..words.size && errors >= 0); require(index in 0..words.size); require(skipped >= 0) }
    val word: Word get() = words[index.coerceAtMost(words.lastIndex)]
    val target: String get() = word.text
    val accuracy: Double get() = Score.accuracy(correct, errors)
    fun start(entries: List<Word> = words, sentences: Boolean = sentenceMode, title: String = articleTitle) =
        PracticeState(page = Page.PRACTICE, words = if (sentences) entries else entries.take(20), sentenceMode = sentences, articleTitle = title)
    fun normalized(value: String) = if (sentenceMode) normalizeSentence(value) else normalizeAnswer(value)
    fun exit() = PracticeState()
    fun finish() = if (page == Page.PRACTICE) copy(page = Page.RESULT, kana = kana.settle(target, index, false)) else this
    fun skip(): PracticeState {
        if (page != Page.PRACTICE) return this
        val next = index + 1
        return copy(lastErrorKey = "", kana = kana.settle(target, index, false), index = next, skipped = skipped + 1, feedback = Feedback.SKIPPED, previousFeedback = Feedback.SKIPPED,
            page = if (next == words.size) Page.RESULT else Page.PRACTICE)
    }
    val answers get() = acceptedAnswers(word, sentenceMode)
    fun match(text: String, composing: Boolean = false) = liveMatch(normalized(text), answers, composing)
    /** Every fresh edit, including composition, is checked without a debounce or candidate confirmation. */
    fun evaluateInput(text: String, revision: Long, question: Int, composing: Boolean = false): PracticeState {
        val answer = normalized(text)
        if (page != Page.PRACTICE || question != index || revision <= lastRevision) return this
        val match = liveMatch(answer, answers, composing)
        if (answer.isBlank() || match.pendingConversion || (!match.complete && match.validPrefix)) {
            return if (feedback == Feedback.WRONG) copy(feedback = Feedback.NONE, lastErrorKey = "") else this
        }
        if (!match.complete) {
            val errorKey = match.errorKey(answer)
            return copy(errors = errors + if (errorKey == lastErrorKey) 0 else 1, feedback = Feedback.WRONG,
                lastRevision = revision, lastErrorKey = errorKey, kana = kana.wrong(target, text))
        }
        val next = index + 1
        val observableKana = KanaTokenizer.tokenize(target)?.let { it == KanaTokenizer.tokenize(text) } == true
        return copy(kana = kana.settle(target, index, observableKana), page = if (next == words.size) Page.RESULT else Page.PRACTICE,
            index = next, correct = correct + 1, feedback = Feedback.CORRECT, previousFeedback = Feedback.CORRECT,
            lastRevision = revision, lastErrorKey = "")
    }
    fun submit(text: String, revision: Long, question: Int, composing: Boolean = false) = evaluateInput(text, revision, question, composing)
}
