package com.hyakuri.kanaflick

import com.hyakuri.kanaflick.reading.ReadingLibrary
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class ReadingLibraryTest {
    private val root = File("src/main/assets/reading")
    private fun loadWith(vararg replacements: Pair<String, String>) = ReadingLibrary.load { path ->
        val name = path.removePrefix("reading/")
        replacements.toMap()[name]?.byteInputStream() ?: File(root, name).inputStream()
    }
    private fun rejectTsv(transform: (String) -> String) {
        assertThrows(IllegalArgumentException::class.java) {
            loadWith("amabie.tsv" to transform(File(root, "amabie.tsv").readText()))
        }
    }

    @Test fun metadataKeysCannotBeMissingSwappedOrDuplicated() {
        rejectTsv { it.replace("author\t", "title\t") }
        rejectTsv { it.replace("level\tN5", "N5") }
        rejectTsv { it.replace("topic\t日本文化", "topic\t") }
        rejectTsv { it.replace("level\tN5", "level\tN5\textra") }
        rejectTsv { "id\tamabie" }
    }

    @Test fun indexRejectsDuplicatesUnsafePathsAndMismatchedIdsBeforeLoading() {
        for (index in listOf("", "amabie\namabie", "../amabie", "Amabie", "amabie ")) {
            assertThrows(IllegalArgumentException::class.java) { loadWith("index.txt" to index) }
        }
        rejectTsv { it.replace("id\tamabie", "id\tanother") }
    }

    @Test fun malformedPairsAndMissingFilesAreRejected() {
        rejectTsv { it + "\n原文だけ\n" }
        rejectTsv { it + "\n原文\tかな\t余分\n" }
        assertThrows(java.io.FileNotFoundException::class.java) { loadWith("index.txt" to "missing") }
        assertThrows(IllegalArgumentException::class.java) { loadWith("amabie.txt" to "別の本文") }
    }

    @Test fun directValidationAlsoChecksRequiredMetadataAndRealHttpsUrls() {
        val article = loadWith().first()
        val invalid = listOf(article.copy(author = " "), article.copy(title = ""),
            article.copy(topic = ""), article.copy(grading = ""), article.copy(adaptation = ""),
            article.copy(license = ""), article.copy(id = "../a"), article.copy(level = "N6"),
            article.copy(sourceUrl = "https://"), article.copy(sourceUrl = "https:///path"),
            article.copy(licenseUrl = "http://example.org"))
        invalid.forEach { a ->
            assertThrows(IllegalArgumentException::class.java) { ReadingLibrary.validate(listOf(a)) }
        }
        assertThrows(IllegalArgumentException::class.java) { ReadingLibrary.validate(emptyList()) }
        assertThrows(IllegalArgumentException::class.java) {
            ReadingLibrary.validate(listOf(article, article.copy(id = "duplicate-body")))
        }
    }

    @Test fun packagedIndexAccountsForEveryTextAndSentenceFile() {
        val ids = loadWith().map { it.id }.toSet()
        assertEquals(ids, root.listFiles()!!.filter { it.extension == "tsv" }.map { it.nameWithoutExtension }.toSet())
        assertEquals(ids, root.listFiles()!!.filter { it.extension == "txt" && it.name !in listOf("index.txt", "NOTICE.txt") }
            .map { it.nameWithoutExtension }.toSet())
    }

    @Test fun releaseContentHasFiveArticlesPerLevelAndPreservesExistingIds() {
        val articles = loadWith()
        assertEquals(25, articles.size)
        assertEquals(253, articles.sumOf { it.sentences.size })
        assertEquals(ReadingLibrary.levels.associateWith { 5 }, articles.groupingBy { it.level }.eachCount())
        assertEquals(listOf("amabie", "email-name", "window", "ordinary", "manhole", "cake",
            "bread", "insects", "news", "media", "dissonance", "opportunity", "comparative", "utility", "externality"),
            articles.take(15).map { it.id })
        val notice = File(root, "NOTICE.txt").readText()
        articles.forEach { a ->
            assertTrue(notice.contains(a.title))
            assertTrue(notice.contains(a.author))
            assertTrue(notice.contains(a.sourceUrl))
            assertTrue(notice.contains(a.licenseUrl))
            assertTrue(notice.contains(a.adaptation))
        }
    }
}
