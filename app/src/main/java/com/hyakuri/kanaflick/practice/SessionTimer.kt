package com.hyakuri.kanaflick.practice

/** Monotonic foreground time. No ticking state or input/scoring dependencies. */
class SessionTimer(private val now: () -> Long, initialMillis: Long = 0L) {
    private var accumulated = initialMillis.coerceAtLeast(0L)
    private var startedAt: Long? = null
    fun elapsedMillis(): Long = accumulated + (startedAt?.let { (now() - it).coerceAtLeast(0L) } ?: 0L)
    fun resume() { if (startedAt == null) startedAt = now() }
    fun pause() { accumulated = elapsedMillis(); startedAt = null }
    fun reset() { accumulated = 0L; startedAt = null }
}

fun formatElapsed(millis: Long): String {
    val seconds = millis.coerceAtLeast(0L) / 1000
    return "${seconds / 60}:${(seconds % 60).toString().padStart(2, '0')}"
}
