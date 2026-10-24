package com.hyakuri.kanaflick.ui.startup

import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.core.graphics.PathParser

/** Original user wordmark outline, grouped for the selected Kana -> Flick -> three signals relay. */
class WordmarkDrawable : Drawable() {
    private val paths = WordmarkPaths.glyphs.map { PathParser.createPathFromPathData(it.data)!! }
    // Center the visible outline, not the source image's transparent side gutters.
    private val inkBounds = RectF().apply {
        val glyphBounds = RectF()
        paths.forEach { it.computeBounds(glyphBounds, true); union(glyphBounds) }
    }
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isDither = true
        shader = LinearGradient(170f, 240f, 650f, 90f,
            0xFF087BFF.toInt(), 0xFF19B6F8.toInt(), Shader.TileMode.CLAMP)
    }
    var elapsed = 0f
        set(value) { field = value; invalidateSelf() }
    override fun getIntrinsicWidth() = kotlin.math.ceil(inkBounds.width()).toInt()
    override fun getIntrinsicHeight() = 245
    override fun draw(canvas: Canvas) {
        val outer = canvas.save()
        val scale = minOf(bounds.width() / inkBounds.width(), bounds.height() / 245f)
        canvas.translate(bounds.exactCenterX() - inkBounds.centerX() * scale,
            bounds.exactCenterY() - 122.5f * scale)
        canvas.scale(scale, scale)
        var signal = 0
        WordmarkPaths.glyphs.forEachIndexed { index, glyph ->
            val local = elapsed - LogoMotion.WORDMARK_START
            val start = if (glyph.group == 2) LogoMotion.WORDMARK_SIGNAL_START + signal++ * LogoMotion.WORDMARK_SIGNAL_STAGGER
                else glyph.group * LogoMotion.WORDMARK_GROUP_DELAY
            val duration = if (glyph.group == 2) LogoMotion.WORDMARK_SIGNAL_DURATION else LogoMotion.WORDMARK_GROUP_DURATION
            val p = ((local - start) / duration).coerceIn(0f, 1f)
            if (p > 0f) {
                // Fast arrival, a restrained overshoot, then a zero-velocity settle.
                val q = p - 1f
                val overshoot = 1f + (1f + LogoMotion.WORDMARK_OVERSHOOT * 10) * q * q * q + LogoMotion.WORDMARK_OVERSHOOT * 10 * q * q
                paint.alpha = (255 * (p / .55f).coerceIn(0f, 1f)).toInt()
                val save = canvas.save()
                canvas.translate(0f, LogoMotion.WORDMARK_RISE * (1 - overshoot))
                canvas.drawPath(paths[index], paint)
                canvas.restoreToCount(save)
            }
        }
        canvas.restoreToCount(outer)
    }
    override fun setAlpha(alpha: Int) { }
    override fun setColorFilter(colorFilter: ColorFilter?) { }
    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun getOpacity() = PixelFormat.TRANSLUCENT
}
