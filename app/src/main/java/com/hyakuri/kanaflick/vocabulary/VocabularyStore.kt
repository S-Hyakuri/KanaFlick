package com.hyakuri.kanaflick.vocabulary
import java.io.*
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/** One atomic local file, no practice history. A failed write never replaces the previous file. */
class VocabularyStore(private val directory: File) {
    private val file get() = File(directory, "vocabulary-v1.bin")
    @Synchronized fun load(): List<Vocabulary> {
        if (!file.exists()) return emptyList()
        require(file.length() <= 32L * 1024 * 1024) { "词库文件过大，已停止读取；原文件保留" }
        return DataInputStream(BufferedInputStream(FileInputStream(file))).use { input ->
            require(input.readInt() == 0x4B463031) { "词库格式无法识别；原文件保留" }
            val count = input.readInt(); require(count in 0..100)
            val result = List(count) {
                val id = input.readUTF(); val name = input.readUTF(); val source = input.readUTF()
                val size = input.readInt(); require(size in 0..10000)
                Vocabulary(id, name, source, List(size) { Word(input.readUTF(), input.readUTF(), input.readUTF(), input.readUTF(), input.readUTF()) }, true)
            }
            require(input.read() == -1) { "词库文件有多余数据" }
            validate(result); result
        }
    }
    @Synchronized fun save(libraries: List<Vocabulary>) {
        validate(libraries)
        require(directory.isDirectory || directory.mkdirs()) { "无法创建存储目录" }
        val temp = File.createTempFile("vocabulary-", ".tmp", directory)
        try {
            FileOutputStream(temp).use { raw ->
                val out = DataOutputStream(BufferedOutputStream(raw))
                out.writeInt(0x4B463031); out.writeInt(libraries.size)
                libraries.forEach { lib ->
                    out.writeUTF(lib.id); out.writeUTF(lib.name); out.writeUTF(lib.source); out.writeInt(lib.words.size)
                    lib.words.forEach { w -> listOf(w.id,w.text,w.reading,w.meaning,w.level).forEach(out::writeUTF) }
                }
                out.flush(); raw.fd.sync()
            }
            require(temp.length() <= 32L * 1024 * 1024) { "本机词库合计超过 32 MiB 上限" }
            Files.move(temp.toPath(), file.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
        } finally { temp.delete() }
    }
    private fun validate(libraries: List<Vocabulary>) {
        require(libraries.size <= 100) { "最多 100 个自定义词库" }
        require(libraries.map { it.id }.toSet().size == libraries.size) { "词库标识重复" }
        libraries.forEach { lib ->
            require(lib.custom && lib.id.matches(Regex("[A-Za-z0-9_.-]{1,128}")))
            require(lib.name.isNotBlank() && lib.name.length <= 80) { "词库名称为 1–80 字符" }
            require(lib.source.length <= 2000)
            require(lib.words.size <= 10000) { "每库最多 10000 条" }
            require(lib.words.map { it.id }.toSet().size == lib.words.size) { "词条 id 重复" }
            require(lib.words.map { WordRules.key(it) }.toSet().size == lib.words.size) { "原文与读音重复" }
            lib.words.forEach { require(WordRules.error(it) == null) { WordRules.error(it)!! } }
        }
    }
}
