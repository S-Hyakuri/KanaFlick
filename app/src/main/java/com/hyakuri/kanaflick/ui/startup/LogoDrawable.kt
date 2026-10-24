package com.hyakuri.kanaflick.ui.startup

import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.core.graphics.PathParser

/** Hand-traced silhouettes from docs/icon.jpg; no bitmap background or system font. */
class LogoDrawable(private val intrinsicSize: Int = -1, private val normalization: Float = 1f) : Drawable() {
    override fun getIntrinsicWidth() = intrinsicSize
    override fun getIntrinsicHeight() = intrinsicSize
    private class Part(val data: String, val from: String, val to: String) {
        val path = PathParser.createPathFromPathData(data)!!
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            val box = RectF().also { path.computeBounds(it, true) }
            isDither = true
            shader = LinearGradient(box.left, box.top, box.right, box.bottom,
                Color.parseColor(from), Color.parseColor(to), Shader.TileMode.CLAMP)
        }
    }
    private val ribbon = RibbonArtwork()
    private val back = Part("M 944,295 L 1250,377 Q 1373,410 1342,537 L 1260,844 Q 1226,965 1110,931 L 803,849 Q 689,818 721,698 L 797,395 Q 832,268 944,295 Z", "#CDEBFC", "#B5DFFC")
    private val na = Part("M 1021,424 Q 1034,412 1060,427 Q 1081,438 1069,459 L 1053,486 L 1093,495 Q 1124,503 1111,532 Q 1105,557 1080,550 L 1021,538 Q 979,615 935,661 L 907,607 Q 939,569 962,525 L 929,518 Q 903,514 912,483 Q 916,457 940,462 L 993,473 Z M 1138,533 Q 1160,513 1181,534 L 1227,579 Q 1247,602 1225,622 Q 1203,641 1185,619 L 1133,566 Q 1117,550 1138,533 Z M 1080,614 Q 1084,587 1115,593 Q 1146,596 1140,621 L 1128,680 Q 1155,700 1184,729 Q 1200,746 1179,767 Q 1160,786 1141,766 L 1111,739 Q 1084,805 1011,779 Q 946,757 956,702 Q 966,627 1044,647 L 1073,653 Z M 1063,704 Q 1014,681 1009,710 Q 1007,733 1034,734 Q 1059,734 1063,704 Z", "#198AFC", "#187AF4")
    private val front = Part("M 392,574 L 760,471 Q 870,439 903,557 L 1028,986 Q 1058,1102 945,1139 L 549,1253 Q 427,1289 391,1171 L 289,746 Q 258,620 392,574 Z", "#1385FF", "#139DF7")
    private val ka = Part("M 530,701 Q 531,679 563,677 Q 604,669 603,705 L 598,765 Q 684,740 710,760 Q 753,789 769,936 Q 782,1045 709,1075 Q 668,1095 626,1090 Q 597,1087 597,1056 Q 590,1018 619,1016 Q 677,1017 685,981 Q 698,940 672,848 Q 660,811 624,828 L 595,835 Q 580,1004 529,1099 Q 517,1129 479,1112 Q 440,1100 455,1071 Q 505,971 518,856 L 458,877 Q 429,893 420,854 Q 408,823 436,811 L 524,785 Z M 734,701 Q 758,677 780,694 Q 853,754 894,848 Q 908,873 875,893 Q 846,913 834,887 Q 792,798 734,744 Q 714,724 734,701 Z", "#FFFFFF", "#FFFFFF")
    private val signals = listOf(
        Part("M 186,550 L 252,568 Q 294,582 287,617 Q 280,659 239,651 L 166,634 Q 124,625 133,586 Q 144,544 186,550 Z", "#AFE6F4", "#0A99FB"),
        Part("M 292,373 L 392,463 Q 430,501 401,537 Q 369,576 328,539 L 218,446 Q 185,411 218,376 Q 250,345 292,373 Z", "#6DD6EF", "#078BFF"),
        Part("M 519,259 L 579,409 Q 596,456 552,482 Q 496,512 466,458 L 401,304 Q 380,253 422,229 Q 485,197 519,259 Z", "#36BEF0", "#006EFF")
    )
    var elapsed = 0f
        set(value) { field = value; invalidateSelf() }
    override fun draw(canvas: Canvas) {
        val save = canvas.save()
        val side = minOf(bounds.width(), bounds.height()).toFloat() * LogoMotion.ART_SCALE * normalization
        canvas.translate(bounds.exactCenterX() - side / 2, bounds.exactCenterY() - side / 2)
        canvas.scale(side / 1536f, side / 1536f)
        canvas.translate(LogoMotion.ART_OFFSET_X, 0f)
        ribbon.update(elapsed)
        ribbon.draw(canvas, foreground = false)
        translated(canvas, LogoMotion.jump(elapsed - LogoMotion.TILE_DELAY)) { drawPart(canvas, back); drawPart(canvas, na) }
        translated(canvas, LogoMotion.jump(elapsed)) { drawPart(canvas, front); drawPart(canvas, ka) }
        ribbon.draw(canvas, foreground = true)
        signals.forEachIndexed { index, part ->
            val count = canvas.save()
            canvas.concat(signalTransform(elapsed, index))
            drawPart(canvas, part); canvas.restoreToCount(count)
        }
        canvas.restoreToCount(save)
    }
    private val signalMatrix = Matrix()
    internal val frontOutline get() = front.path
    internal val signalOutlines get() = signals.map { it.path }
    internal fun signalTransform(time: Float, index: Int): Matrix {
        val pulse = LogoMotion.signalPulse(time, index)
        return signalMatrix.apply {
            setScale(1 + pulse * LogoMotion.SIGNAL_SCALE, 1 + pulse * LogoMotion.SIGNAL_SCALE,
                210f + index * 135f, 600f - index * 120f)
            postTranslate(-pulse * LogoMotion.SIGNAL_DISTANCE * (1 - index * .25f),
                LogoMotion.jump(time) - pulse * LogoMotion.SIGNAL_DISTANCE * (.3f + index * .35f))
        }
    }
    private fun drawPart(c: Canvas, p: Part) { c.drawPath(p.path, p.paint) }
    private inline fun translated(c: Canvas, y: Float, draw: () -> Unit) { val n = c.save(); c.translate(0f, y); draw(); c.restoreToCount(n) }
    override fun setAlpha(alpha: Int) { }
    override fun setColorFilter(colorFilter: ColorFilter?) { }
    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun getOpacity() = PixelFormat.TRANSLUCENT
}

