package com.hyakuri.kanaflick.vocabulary

data class Word(val id: String, val text: String, val reading: String, val meaning: String, val level: String)
data class Vocabulary(val id: String, val name: String, val source: String, val words: List<Word>, val custom: Boolean = false)
object Samples {
    val words = listOf(Word("sample-school", "学校", "がっこう", "学校", "N5"), Word("sample-water", "水", "みず", "水", "N5"), Word("sample-eat", "食べる", "たべる", "吃", "N5"))
    val basic = Vocabulary("sample-N5", "N5 · 功能样例", "项目自编演示；等级暂定，非完整词库，未做词典核验", words)
}
