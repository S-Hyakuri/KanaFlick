package com.hyakuri.kanaflick.proficiency

import java.io.*
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.time.LocalDate

/** Same atomic-file strategy as VocabularyStore. Call only on the single IO writer. */
class ProfileStore(private val directory: File) {
    val file get() = File(directory, "kana-profile-v1.bin")
    fun load(): KanaProfile {
        if (!file.exists()) return KanaProfile()
        require(file.length() <= 128 * 1024) { "画像文件过大" }
        return DataInputStream(BufferedInputStream(FileInputStream(file))).use { input ->
            require(input.readInt() == 0x4B465031 && input.readInt() == 1 && input.readInt() == 1) { "未知画像或规则版本" }
            val generation = input.readUTF(); val session = input.readUTF(); val cursor = input.readInt()
            require(generation.isNotBlank() && generation.length <= 128 && session.length <= 128 && cursor >= -1)
            val count = input.readInt(); require(count in 0..KanaTokenizer.units.size)
            val stats = mutableMapOf<String, KanaStat>()
            repeat(count) {
                val unit = input.readUTF()
                require(unit in KanaTokenizer.units && unit !in stats)
                val s = KanaStat(input.readLong(), input.readLong(), input.readDouble(), input.readUTF())
                require(s.samples > 0 && s.errors in 0..s.samples && s.recent.isFinite() && s.recent in 0.0..1.0)
                LocalDate.parse(s.date); stats[unit] = s
            }
            require(input.read() == -1)
            KanaProfile(generation, session, cursor, stats)
        }
    }
    fun save(profile: KanaProfile) {
        require(directory.isDirectory || directory.mkdirs())
        val temp = File.createTempFile("kana-profile-", ".tmp", directory)
        try {
            FileOutputStream(temp).use { raw ->
                val out = DataOutputStream(BufferedOutputStream(raw))
                out.writeInt(0x4B465031); out.writeInt(1); out.writeInt(1)
                out.writeUTF(profile.generation); out.writeUTF(profile.session); out.writeInt(profile.cursor)
                out.writeInt(profile.stats.size)
                profile.stats.forEach { (unit, s) ->
                    out.writeUTF(unit); out.writeLong(s.samples); out.writeLong(s.errors); out.writeDouble(s.recent); out.writeUTF(s.date)
                }
                out.flush(); raw.fd.sync()
            }
            Files.move(temp.toPath(), file.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
        } finally { temp.delete() }
    }
}
