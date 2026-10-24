package com.hyakuri.kanaflick.ui.startup

import android.view.animation.PathInterpolator
import kotlin.math.sin

/** Milliseconds; artwork distances use the source image's 1536-unit coordinate system. */
object LogoMotion {
    const val DURATION = 1740L
    const val FADE = 160L
    const val TILE_DELAY = 110f
    const val JUMP_HEIGHT = 46f
    const val JUMP_DURATION = 420f
    const val REBOUND = 0.10f
    const val SIGNAL_START = 260f
    const val SIGNAL_STAGGER = 50f
    const val SIGNAL_DURATION = 270f
    const val SIGNAL_DISTANCE = 30f
    const val SIGNAL_SCALE = 0.07f
    const val SIGNAL_REBOUND = 0.08f
    const val RIBBON_START = 100f
    const val RIBBON_DURATION = 720f
    const val RIBBON_OVERSHOOT = 0.018f
    const val WORDMARK_START = 560f
    const val WORDMARK_GROUP_DELAY = 70f
    const val WORDMARK_GROUP_DURATION = 240f
    const val WORDMARK_SIGNAL_START = 240f
    const val WORDMARK_SIGNAL_STAGGER = 30f
    const val WORDMARK_SIGNAL_DURATION = 90f
    const val WORDMARK_SETTLED = WORDMARK_START + WORDMARK_SIGNAL_START + 2 * WORDMARK_SIGNAL_STAGGER + WORDMARK_SIGNAL_DURATION
    const val BRAND_HOLD = DURATION - WORDMARK_SETTLED
    const val WORDMARK_RISE = 18f // Source wordmark coordinates (795 x 245).
    const val WORDMARK_OVERSHOOT = 0.10f
    const val WORDMARK_WIDTH_RATIO = 0.88f
    const val WORDMARK_TOP_RATIO = 0.39f
    const val WORDMARK_OFFSET_X_RATIO = 0.035f
    const val PLATFORM_ICON_NORMALIZATION = 1.5f
    // Source artwork units; retain the original icon position.
    const val ART_OFFSET_X = 0f
    const val ART_SCALE = 0.64f
    val ribbonEasing = PathInterpolator(0.48f, 0f, 0.18f, 1f)
    fun jump(time: Float): Float {
        val p = (time / JUMP_DURATION).coerceIn(0f, 1f)
        return when {
            p < 0.40f -> { val q = p / 0.40f; -JUMP_HEIGHT * (2 * q - q * q) }
            p < 0.78f -> { val q = (p - 0.40f) / 0.38f; -JUMP_HEIGHT * (1 - q * q) }
            else -> { val q = (p - 0.78f) / 0.22f; -JUMP_HEIGHT * REBOUND * 4 * q * (1 - q) }
        }
    }
    fun signalPulse(time: Float, index: Int): Float {
        val t = ((time - SIGNAL_START - index * SIGNAL_STAGGER) / SIGNAL_DURATION).coerceIn(0f, 1f)
        return if (t < 0.76f) sin(t / 0.76f * Math.PI).toFloat()
        else -SIGNAL_REBOUND * sin((t - 0.76f) / 0.24f * Math.PI).toFloat()
    }
}
