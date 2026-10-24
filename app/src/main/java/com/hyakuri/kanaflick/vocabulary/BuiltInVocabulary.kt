package com.hyakuri.kanaflick.vocabulary

import java.io.InputStream

object BuiltInVocabulary {
    val levels = listOf("N5", "N4", "N3", "N2", "N1")
    const val SOURCE = "egg rolls · JLPT10k v3.5（2026-09-06）\n民间参考分级，非官方考纲；中文释义来自原牌组。CC BY-NC 4.0，非商业使用。"
    fun load(open: (String) -> InputStream): List<Vocabulary> = levels.map { level ->
        val preview = open("vocabulary/$level.csv").use { VocabularyCsv.preview(VocabularyCsv.decode(it.readBytes())) }
        require(preview.errors.isEmpty() && preview.duplicates.isEmpty() && preview.words.isNotEmpty()) { "$level 内置词库损坏" }
        require(preview.words.all { it.level == level }) { "$level 分级数据不一致" }
        Vocabulary("jlpt-$level", "$level · JLPT 参考词库", SOURCE, preview.words)
    }
}
