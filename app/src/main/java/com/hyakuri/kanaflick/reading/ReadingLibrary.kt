package com.hyakuri.kanaflick.reading

import com.hyakuri.kanaflick.practice.normalizeSentence
import com.hyakuri.kanaflick.vocabulary.Word
import java.io.InputStream
import java.net.URI

data class ReadingSentence(val original: String, val kana: String)
data class ReadingArticle(
    val id: String, val level: String, val title: String, val author: String,
    val sourceUrl: String, val license: String, val licenseUrl: String,
    val adaptation: String, val topic: String, val grading: String,
    val sentences: List<ReadingSentence>,
    val fullText: String = sentences.joinToString("\n") { it.original },
) {
    val characterCount get() = fullText.count { !it.isWhitespace() }
    fun practiceWords() = sentences.mapIndexed { index, s -> Word("$id-$index", s.kana, s.original, title, level) }
}

object ReadingLibrary {
    val levels = listOf("N5", "N4", "N3", "N2", "N1")
    private val metadataKeys = listOf("id", "level", "title", "author", "sourceUrl", "license",
        "licenseUrl", "adaptation", "topic", "grading")
    private val stableId = Regex("[a-z0-9]+(?:-[a-z0-9]+)*")
    private fun isHttpsUrl(value: String): Boolean = runCatching {
        val uri = URI(value)
        uri.scheme == "https" && !uri.host.isNullOrBlank() && uri.userInfo == null
    }.getOrDefault(false)

    fun load(open: (String) -> InputStream): List<ReadingArticle> {
        val names = open("reading/index.txt").bufferedReader().use { it.readLines() }.filter { it.isNotBlank() }
        require(names.isNotEmpty() && names.all { stableId.matches(it) }) { "阅读索引为空或 ID 格式错误" }
        require(names.distinct().size == names.size) { "阅读索引包含重复 ID" }
        val articles = names.map { name ->
            val lines = open("reading/$name.tsv").bufferedReader().use { it.readLines() }
            require(lines.size >= metadataKeys.size) { "阅读元数据缺失：$name" }
            val meta = metadataKeys.mapIndexed { index, key ->
                val fields = lines[index].split('\t')
                require(fields.size == 2 && fields[0] == key && fields[1].isNotBlank()) {
                    "阅读元数据字段错误：$name / $key"
                }
                fields[1]
            }
            require(meta[0] == name) { "阅读索引与 ID 不一致：$name" }
            val sentences = lines.drop(metadataKeys.size).filter { it.isNotBlank() }.mapIndexed { index, line ->
                val pair = line.split('\t')
                require(pair.size == 2) { "阅读句对格式错误：$name / ${index + 1}" }
                ReadingSentence(pair[0], pair[1])
            }
            val fullText = open("reading/$name.txt").bufferedReader().use { it.readLines().joinToString("\n").trimEnd() }
            ReadingArticle(meta[0], meta[1], meta[2], meta[3], meta[4], meta[5], meta[6], meta[7], meta[8], meta[9], sentences, fullText)
        }
        validate(articles)
        return articles
    }
    fun validate(articles: List<ReadingArticle>) {
        require(articles.isNotEmpty()) { "阅读库为空" }
        require(articles.map { it.id }.distinct().size == articles.size)
        require(articles.map { it.fullText }.distinct().size == articles.size)
        articles.forEach { a ->
            require(stableId.matches(a.id)) { "阅读 ID 格式错误：${a.id}" }
            require(listOf(a.title, a.author, a.license, a.adaptation, a.topic, a.grading).all { it.isNotBlank() }) {
                "阅读元数据缺失：${a.id}"
            }
            require(a.level in levels && isHttpsUrl(a.sourceUrl) && isHttpsUrl(a.licenseUrl)) {
                "阅读等级或来源链接错误：${a.id}"
            }
            require(a.sentences.isNotEmpty())
            require(a.fullText == a.sentences.joinToString("\n") { it.original }) { "全文与拆句不一致：${a.id}" }
            a.sentences.forEach { s ->
                require(s.original.isNotBlank() && normalizeSentence(s.kana).isNotEmpty())
                require(normalizeSentence(s.kana).all { it in '\u3041'..'\u3096' || it in '\u30A1'..'\u30FA' || it == 'ー' }) { "答案包含非假名：${a.id}" }
            }
        }
    }
}
