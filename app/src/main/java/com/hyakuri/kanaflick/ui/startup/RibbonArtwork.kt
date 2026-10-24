package com.hyakuri.kanaflick.ui.startup

import android.graphics.*
import androidx.core.graphics.PathParser

/** Separate visible rear/foreground silhouettes: reveal never paints the empty lower-left sector. */
internal class RibbonArtwork {
    val rear = PathParser.createPathFromPathData("M 208,1173 C 99,1126 117,1044 159,986 C 194,936 252,889 314,853 L 330,914 C 260,958 204,1007 180,1053 C 154,1101 185,1149 208,1173 Z")!!
    val front = PathParser.createPathFromPathData("M 1300,682 C 1399,707 1469,759 1473,838 C 1482,959 1363,1065 1226,1145 C 1019,1265 736,1336 482,1343 C 553,1298 632,1277 734,1242 C 1001,1152 1190,1054 1295,951 C 1360,887 1384,827 1360,783 C 1346,758 1320,737 1289,722 Z")!!
    // Clockwise in screen coordinates: left tip -> upper/rear arc -> right side -> lower/front tip.
    val orbit = Path().apply {
        moveTo(208f, 1173f)
        cubicTo(91f, 1076f, 224f, 946f, 340f, 879f)
        cubicTo(655f, 684f, 1060f, 598f, 1325f, 722f)
        cubicTo(1562f, 817f, 1405f, 1040f, 1118f, 1175f)
        cubicTo(903f, 1276f, 655f, 1310f, 482f, 1343f)
    }
    private val measure = PathMeasure(orbit, false)
    private val segment = Path()
    private val reveal = Path()
    private val sweep = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; strokeWidth = 190f; strokeCap = Paint.Cap.ROUND
    }
    private val rearPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isDither = true
        shader = LinearGradient(150f, 1115f, 340f, 845f,
            intArrayOf(0xFFB4E7F4.toInt(), 0xFF99D8F5.toInt(), 0xFF7BC9F7.toInt()),
            floatArrayOf(0f, .52f, 1f), Shader.TileMode.CLAMP)
    }
    private val frontPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isDither = true
        shader = LinearGradient(490f, 1335f, 1460f, 815f,
            intArrayOf(0xFF54B9F3.toInt(), 0xFF9ED8FA.toInt(), 0xFFBDE7FC.toInt(), 0xFF9BD7FA.toInt(), 0xFF30B4F1.toInt()),
            floatArrayOf(0f, .22f, .47f, .73f, 1f), Shader.TileMode.CLAMP)
    }
    private val highlightShader = RadialGradient(1010f, 1160f, 420f,
        intArrayOf(0x66FFFFFF, 0x26FFFFFF, 0x00FFFFFF), floatArrayOf(0f, .48f, 1f), Shader.TileMode.CLAMP)
    private val highlight = Paint(Paint.ANTI_ALIAS_FLAG).apply { shader = highlightShader }
    private val matrix = Matrix()
    private var complete = false
    fun update(time: Float) {
        val p = ((time - LogoMotion.RIBBON_START) / LogoMotion.RIBBON_DURATION).coerceIn(0f, 1f)
        val q = LogoMotion.ribbonEasing.getInterpolation((p / .9f).coerceIn(0f, 1f))
        complete = q >= 1f
        segment.rewind(); reveal.rewind()
        if (!complete) {
            measure.getSegment(0f, measure.length * q, segment, true)
            sweep.getFillPath(segment, reveal)
        }
        // Only the soft highlight settles; the silhouette cannot drift into surrounding whitespace.
        val settle = if (p > .82f) kotlin.math.sin((p - .82f) / .18f * Math.PI).toFloat() else 0f
        matrix.setTranslate(settle * LogoMotion.RIBBON_OVERSHOOT * 1536f, 0f)
        highlightShader.setLocalMatrix(matrix)
    }
    fun draw(canvas: Canvas, foreground: Boolean) {
        val save = canvas.save()
        if (!complete) canvas.clipPath(reveal)
        val path = if (foreground) front else rear
        canvas.drawPath(path, if (foreground) frontPaint else rearPaint)
        if (foreground) canvas.drawPath(path, highlight)
        canvas.restoreToCount(save)
    }
}
