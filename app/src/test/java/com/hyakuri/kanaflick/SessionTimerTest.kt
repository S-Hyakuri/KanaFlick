package com.hyakuri.kanaflick

import com.hyakuri.kanaflick.practice.*
import org.junit.Assert.*
import org.junit.Test

class SessionTimerTest {
    @Test fun foregroundSegmentsExcludeBackgroundAndDuplicateEvents() {
        var now = 100L
        val timer = SessionTimer({ now })
        now = 1100; assertEquals(0L, timer.elapsedMillis())
        timer.resume(); now = 2100; timer.resume()
        assertEquals(1000L, timer.elapsedMillis())
        timer.pause(); timer.pause(); now = 9100
        assertEquals(1000L, timer.elapsedMillis())
        timer.resume(); now = 9600; timer.pause()
        assertEquals(1500L, timer.elapsedMillis())
    }
    @Test fun restorationPreservesElapsedButDoesNotCountTimeAway() {
        var now = 0L
        val original = SessionTimer({ now })
        original.resume(); now = 4321
        val snapshot = original.elapsedMillis()
        now = 90000
        val restored = SessionTimer({ now }, snapshot)
        assertEquals(4321L, restored.elapsedMillis())
        restored.resume(); now += 679
        assertEquals(5000L, restored.elapsedMillis())
    }
    @Test fun completionFreezesAndRestartExitReset() {
        var now = 0L
        val timer = SessionTimer({ now })
        timer.resume(); now = 10000; timer.pause(); now += 10000
        assertEquals(10000L, timer.elapsedMillis())
        timer.reset(); assertEquals(0L, timer.elapsedMillis())
        timer.resume(); now += 250; assertEquals(250L, timer.elapsedMillis())
        timer.reset(); now += 2000; assertEquals(0L, timer.elapsedMillis())
    }
    @Test fun minutesDoNotWrapAtAnHour() {
        assertEquals("0:00", formatElapsed(999))
        assertEquals("1:01", formatElapsed(61000))
        assertEquals("60:00", formatElapsed(3600000))
    }
}
