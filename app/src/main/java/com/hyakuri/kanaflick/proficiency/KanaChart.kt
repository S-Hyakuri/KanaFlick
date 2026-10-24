package com.hyakuri.kanaflick.proficiency

/** Five vowel columns, with empty positions retained instead of shifting the next kana. */
object KanaChart {
    val groups: Map<String, List<List<String>>> = KanaTokenizer.groups.mapValues { (group, units) ->
        when (group) {
            "基础音" -> (units.take(35).chunked(5) + listOf(
                listOf("や", "", "ゆ", "", "よ"),
                listOf("ら", "り", "る", "れ", "ろ"),
                listOf("わ", "ゐ", "", "ゑ", "を"),
                listOf("ん", "", "", "", "")))
            "拗音与外来音" -> units.groupBy { it.dropLast(1) }.values.map { row ->
                List(5) { column -> row.firstOrNull { vowelColumn(it.last()) == column }.orEmpty() }
            }
            "小假名与长音" -> listOf(
                listOf("ぁ", "ぃ", "ぅ", "ぇ", "ぉ"),
                listOf("ゃ", "", "ゅ", "", "ょ"),
                listOf("っ", "ゎ", "ゕ", "ゖ", "ー"))
            else -> units.chunked(5).map { it + List(5 - it.size) { "" } }
        }
    }
    private fun vowelColumn(c: Char) = when (c) {
        'ぁ', 'ゃ' -> 0
        'ぃ' -> 1
        'ぅ', 'ゅ' -> 2
        'ぇ' -> 3
        else -> 4
    }
}
