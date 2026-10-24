package com.hyakuri.kanaflick
import com.hyakuri.kanaflick.vocabulary.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class VocabularyTest {
    @get:Rule val folder = TemporaryFolder()
    private val word = Word("w1", "学校", "がっこう", "学校", "N5")
    @Test fun csvRoundtripBomQuotesCommaAndNewline() {
        val words = listOf(word.copy(meaning = "学校,\"学堂\"\n学习场所"))
        val result = VocabularyCsv.preview("\uFEFF" + VocabularyCsv.export(words))
        assertEquals(words, result.words); assertTrue(result.canImport)
    }
    @Test fun semanticErrorsAndDuplicatesArePreviewedWithoutChangingExisting() {
        val existing = listOf(word)
        val text = VocabularyCsv.HEADER + "\n" +
            "w1,学校,がっこう,学校,N5\n" + "w2,水,みず,水,N5\n" + "w3,雨,あめ,,N5\n" + "w4,天気,てんき,天气,N9\n"
        val result = VocabularyCsv.preview(text, existing)
        assertEquals(listOf(2), result.duplicates); assertEquals(1, result.words.size)
        assertEquals(2, result.errors.size); assertFalse(result.canImport); assertEquals(listOf(word), existing)
    }
    @Test fun malformedCsvRejected() {
        listOf("wrong,header", VocabularyCsv.HEADER + "\nw,\"unclosed", VocabularyCsv.HEADER + "\nw,\"x\"z,r,m,N5", VocabularyCsv.HEADER + "\na,b,c").forEach {
            assertFalse(VocabularyCsv.preview(it).canImport)
            assertTrue(VocabularyCsv.preview(it).errors.isNotEmpty())
        }
    }
    @Test fun invalidUtf8Rejected() {
        assertThrows(Exception::class.java) { VocabularyCsv.decode(byteArrayOf(0xC3.toByte(), 0x28)) }
        assertThrows(IllegalArgumentException::class.java) { VocabularyCsv.decode(ByteArray(VocabularyCsv.MAX_BYTES + 1)) }
    }
    @Test fun duplicatesUseNfcButNotKanaFolding() {
        val words = listOf(Word("a","が","が","甲","N5"), Word("b","か\u3099","が","乙","N5"), Word("c","ガ","が","丙","N5"))
        val result = VocabularyCsv.preview(VocabularyCsv.export(words))
        assertEquals(2, result.words.size); assertEquals(listOf(3), result.duplicates)
    }
    @Test fun storeSurvivesNewRepositoryAndRenameEditDelete() {
        val dir = folder.newFolder(); val lib = Vocabulary("test", "测试", "用户", listOf(word), true)
        VocabularyStore(dir).save(listOf(lib))
        assertEquals(listOf(lib), VocabularyStore(dir).load())
        val edited = lib.copy(name = "改名", words = listOf(word.copy(meaning = "修改释义")))
        VocabularyStore(dir).save(listOf(edited)); assertEquals(listOf(edited), VocabularyStore(dir).load())
        VocabularyStore(dir).save(emptyList()); assertTrue(VocabularyStore(dir).load().isEmpty())
    }
    @Test fun rejectedSavePreservesBytes() {
        val dir = folder.newFolder(); val store = VocabularyStore(dir)
        val lib = Vocabulary("test", "测试", "用户", listOf(word), true)
        store.save(listOf(lib)); val bytes = java.io.File(dir,"vocabulary-v1.bin").readBytes()
        assertThrows(IllegalArgumentException::class.java) { store.save(listOf(lib.copy(words = listOf(word,word)))) }
        assertArrayEquals(bytes, java.io.File(dir,"vocabulary-v1.bin").readBytes())
        assertEquals(listOf(lib), store.load())
    }
    @Test fun corruptFileIsReportedAndNotSilentlyReset() {
        val dir = folder.newFolder(); val file = java.io.File(dir,"vocabulary-v1.bin")
        file.writeText("broken")
        assertThrows(Exception::class.java) { VocabularyStore(dir).load() }
        assertEquals("broken", file.readText())
    }
    @Test fun allRealLevelsHaveValidUniqueWordsAndDeclaredCounts() {
        val libraries = BuiltInVocabulary.load { java.io.File("src/main/assets/$it").inputStream() }
        assertEquals(listOf(805,757,1815,3214,4044), libraries.map { it.words.size })
        assertTrue(libraries.all { it.source.contains("非官方") && !it.name.contains("样例") })
        val words = libraries.flatMap { it.words }
        assertEquals(10635, words.map { it.id }.toSet().size)
        libraries.forEach { lib ->
            assertEquals(lib.words.size, lib.words.map(WordRules::key).toSet().size)
            assertTrue(lib.words.all { WordRules.error(it) == null && !it.meaning.contains("<") })
        }
    }
}
