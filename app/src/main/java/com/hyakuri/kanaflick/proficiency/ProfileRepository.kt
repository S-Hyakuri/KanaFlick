package com.hyakuri.kanaflick.proficiency

import android.content.Context
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ProfileView(val profile: KanaProfile = KanaProfile(), val ready: Boolean = false, val unavailable: Boolean = false, val message: String = "", val pending: Boolean = false)

/** Process-owned queue survives Activity recreation. Published stats are always durably saved. */
class ProfileRepository private constructor(context: Context) {
    private val store = ProfileStore(context.noBackupFilesDir)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutable = MutableStateFlow(ProfileView())
    val state = mutable.asStateFlow()
    @Volatile var generationForNewSessions: String = ""
        private set
    private val commands = Channel<ProfileCommand>(Channel.UNLIMITED)
    // Session-level avoidance is deliberately memory-only and reset with the profile.
    var previousIds: Set<String> = emptySet()
        private set
    fun rememberWords(ids: Set<String>) { previousIds = ids }
    fun begin(progress: KanaProgress) { commands.trySend(ProfileCommand.Begin(progress)) }
    fun submit(progress: KanaProgress) { if (progress.pending.isNotEmpty()) commands.trySend(ProfileCommand.Commit(progress)) }
    fun retry() { commands.trySend(ProfileCommand.Retry) }
    fun reset() {
        previousIds = emptySet()
        generationForNewSessions = java.util.UUID.randomUUID().toString()
        commands.trySend(ProfileCommand.Reset(generationForNewSessions))
    }

    init {
        scope.launch {
            try {
                val loaded = store.load()
                generationForNewSessions = loaded.generation
                mutable.value = ProfileView(loaded, ready = true)
            }
            catch (_: Exception) { mutable.value = ProfileView(ready = true, unavailable = true, message = "画像暂不可用，原文件已保留；普通练习仍可进行") }
            val writer = ProfileWriteQueue(mutable.value.profile) { updated ->
                if (mutable.value.unavailable && store.file.exists()) {
                    store.file.copyTo(java.io.File(store.file.parentFile, "kana-profile-unreadable.bin"), overwrite = true)
                }
                store.save(updated)
            }
            for (command in commands) {
                if (mutable.value.unavailable && generationForNewSessions.isEmpty() && command !is ProfileCommand.Reset && command != ProfileCommand.Retry) continue
                try {
                    writer.accept(command)
                    // Retry without a reset cannot make an unreadable profile available.
                    if (!mutable.value.unavailable || command is ProfileCommand.Reset || writer.profile.generation != mutable.value.profile.generation)
                        mutable.value = ProfileView(writer.profile, ready = true)
                } catch (_: Exception) {
                    mutable.value = mutable.value.copy(message = "熟练度保存失败，待提交数据已保留，请重试", pending = writer.hasPending)
                }
            }
        }
    }
    companion object {
        @Volatile private var instance: ProfileRepository? = null
        fun get(context: Context): ProfileRepository = instance ?: synchronized(this) {
            instance ?: ProfileRepository(context.applicationContext).also { instance = it }
        }
    }
}
