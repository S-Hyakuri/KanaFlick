package com.hyakuri.kanaflick.vocabulary

import com.hyakuri.kanaflick.practice.normalizeText
import java.nio.ByteBuffer
import java.nio.charset.CodingErrorAction

object WordRules {
    val levels = listOf("N5", "N4", "N3", "N2", "N1", "未分级")
    fun error(w: Word): String? = when {
        !w.id.matches(Regex("[A-Za-z0-9_.-]{1,128}")) -> "id 只能包含英文字母、数字、下划线、点和连字符（1–128 字符）"
        w.text.isBlank() || w.text.length > 120 || w.text.any { it.isISOControl() } -> "日语原文必填，最多 120 字符，不含控制字符"
        w.reading.isBlank() || w.reading.length > 120 || w.reading.any { it.isISOControl() } -> "读音必填，最多 120 字符，不含控制字符"
        w.meaning.isBlank() || w.meaning.length > 1000 -> "中文释义必填，最多 1000 字符"
        w.level !in levels -> "等级必须为 N5、N4、N3、N2、N1 或 未分级"
        else -> null
    }
    fun key(w: Word) = normalizeText(w.text) to normalizeText(w.reading)
}
data class CsvPreview(val words: List<Word>, val duplicates: List<Int>, val errors: List<String>) {
    val canImport get() = words.isNotEmpty() && errors.isEmpty()
}
object VocabularyCsv {
    const val HEADER = "id,japanese,reading,meaning_zh,level"
    const val MAX_BYTES = 2 * 1024 * 1024
    fun decode(bytes: ByteArray): String {
        require(bytes.size <= MAX_BYTES) { "CSV 超过 2 MiB 上限" }
        return Charsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(bytes)).toString()
    }
    fun export(words: List<Word>): String = HEADER + "\r\n" + words.joinToString("\r\n") { w ->
        listOf(w.id, w.text, w.reading, w.meaning, w.level).joinToString(",") { "\"" + it.replace("\"", "\"\"") + "\"" }
    } + "\r\n"
    fun preview(text: String, existing: List<Word> = emptyList()): CsvPreview {
        val rows = mutableListOf<Pair<Int, List<String>>>()
        val errors = mutableListOf<String>()
        val fields = mutableListOf<String>()
        val cell = StringBuilder()
        var quoted = false; var closed = false; var line = 1; var startLine = 1; var i = 0
        val source = text.removePrefix("\uFEFF")
        if (source.length > MAX_BYTES) return CsvPreview(emptyList(), emptyList(), listOf("CSV 过大"))
        fun field() { fields.add(cell.toString()); cell.setLength(0); closed = false }
        fun row() { field(); if (fields.any { it.isNotEmpty() }) rows.add(startLine to fields.toList()); fields.clear(); startLine = line + 1 }
        while (i < source.length) {
            val c = source[i]
            if (quoted) {
                if (c == '"') {
                    if (i + 1 < source.length && source[i + 1] == '"') { cell.append('"'); i++ }
                    else { quoted = false; closed = true }
                } else { cell.append(c); if (c == '\n') line++ }
            } else when {
                c == ',' -> field()
                c == '\r' || c == '\n' -> {
                    row(); if (c == '\r' && i + 1 < source.length && source[i + 1] == '\n') i++
                    line++
                }
                closed -> return CsvPreview(emptyList(), emptyList(), listOf("第 $line 行：引号结束后只能接逗号或换行"))
                c == '"' -> {
                    if (cell.isNotEmpty()) return CsvPreview(emptyList(), emptyList(), listOf("第 $line 行：引号必须位于字段开头"))
                    quoted = true
                }
                else -> cell.append(c)
            }
            if (rows.size > 10001) return CsvPreview(emptyList(), emptyList(), listOf("最多导入 10000 条"))
            i++
        }
        if (quoted) return CsvPreview(emptyList(), emptyList(), listOf("第 $startLine 行：引号未闭合"))
        if (cell.isNotEmpty() || fields.isNotEmpty() || closed) row()
        if (rows.firstOrNull()?.second != HEADER.split(',')) return CsvPreview(emptyList(), emptyList(), listOf("表头必须为 $HEADER"))
        val ids = existing.map { it.id }.toMutableSet()
        val keys = existing.map { WordRules.key(it) }.toMutableSet()
        val valid = mutableListOf<Word>(); val duplicates = mutableListOf<Int>()
        rows.drop(1).forEach { (number, cells) ->
            if (cells.size != 5) errors.add("第 $number 行：需要 5 列，实际 ${cells.size} 列")
            else {
                val w = Word(cells[0], cells[1], cells[2], cells[3], cells[4])
                val error = WordRules.error(w)
                if (error != null) errors.add("第 $number 行：$error")
                else if (w.id in ids || WordRules.key(w) in keys) duplicates.add(number)
                else { valid.add(w); ids.add(w.id); keys.add(WordRules.key(w)) }
            }
        }
        if (existing.size + valid.size > 10000) errors.add("词库最多 10000 条")
        return CsvPreview(valid, duplicates, errors)
    }
}
