package com.hyakuri.kanaflick.proficiency

sealed interface ProfileCommand {
    data class Begin(val progress: KanaProgress) : ProfileCommand
    data class Commit(val progress: KanaProgress) : ProfileCommand
    data class Reset(val generation: String) : ProfileCommand
    data object Retry : ProfileCommand
}

/** Single-writer state machine. Failed writes retain their commands and never publish new stats. */
class ProfileWriteQueue(initial: KanaProfile, private val save: (KanaProfile) -> Unit) {
    var profile = initial
        private set
    private val pending = ArrayDeque<ProfileCommand>()
    val hasPending get() = pending.isNotEmpty()
    fun accept(command: ProfileCommand) {
        if (command is ProfileCommand.Reset) pending.clear()
        if (command != ProfileCommand.Retry) pending.addLast(command)
        while (pending.isNotEmpty()) {
            val next = pending.first()
            val updated = when (next) {
                is ProfileCommand.Begin -> if (next.progress.generation == profile.generation && next.progress.session != profile.session)
                    profile.copy(session = next.progress.session, cursor = -1) else profile
                is ProfileCommand.Commit -> profile.commit(next.progress)
                is ProfileCommand.Reset -> KanaProfile(generation = next.generation)
                ProfileCommand.Retry -> profile
            }
            if (updated != profile) save(updated)
            profile = updated
            pending.removeFirst()
        }
    }
}
