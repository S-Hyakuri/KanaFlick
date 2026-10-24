package com.hyakuri.kanaflick

import android.content.res.Configuration
import android.graphics.*
import android.os.Build
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.ui.graphics.toArgb
import androidx.test.platform.app.InstrumentationRegistry
import com.hyakuri.kanaflick.ui.startup.*
import java.io.File
import org.junit.Assert.*
import org.junit.Test

class LogoStartupTest {
    @Test fun transparentCornersStableEndpointAndFrameEvidence() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val folder = File(context.getExternalFilesDir(null), "logo-evidence-v053").apply { mkdirs() }
        fun render(time: Float): Bitmap {
            val bitmap = Bitmap.createBitmap(768, 768, Bitmap.Config.ARGB_8888)
            LogoDrawable().apply { setBounds(0, 0, 768, 768); elapsed = time }.draw(Canvas(bitmap))
            return bitmap
        }
        val first = render(0f)
        val last = render(LogoMotion.DURATION.toFloat())
        assertEquals(0, last.getPixel(0, 0))
        assertFalse(first.sameAs(last))
        assertTrue(last.sameAs(render(LogoMotion.DURATION + 500f)))
        assertEquals(0f, LogoMotion.jump(-100f), .001f)
        assertEquals(0f, LogoMotion.jump(LogoMotion.JUMP_DURATION), .001f)
        listOf(0f, 220f, 470f, 750f, 980f, 1230f, 1330f, 1510f, 1680f, 1820f).forEach { time ->
            render(time).also { bitmap ->
                File(folder, "frame-${time.toInt()}.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
                bitmap.recycle()
            }
        }
        val wordmark = Bitmap.createBitmap(795, 275, Bitmap.Config.ARGB_8888)
        WordmarkDrawable().apply { setBounds(0, 0, 795, 245); elapsed = 1820f }.draw(Canvas(wordmark))
        File(folder, "wordmark.png").outputStream().use { wordmark.compress(Bitmap.CompressFormat.PNG, 100, it) }
        first.recycle(); last.recycle(); wordmark.recycle()
    }

    @Test fun jumpingKaNeverIntersectsAnySignal() {
        val logo = LogoDrawable()
        val signals = logo.signalOutlines
        for (time in 0..1000 step 4) {
            val tile = Path(logo.frontOutline).apply { offset(0f, LogoMotion.jump(time.toFloat())) }
            signals.forEachIndexed { index, source ->
                val signal = Path(source).apply { transform(logo.signalTransform(time.toFloat(), index)) }
                val intersection = Path(tile)
                assertTrue(intersection.op(signal, Path.Op.INTERSECT))
                assertTrue("Signal $index intersects ka at ${time}ms", intersection.isEmpty)
            }
        }
    }

    @Test fun ribbonStartsAtLeftTipAndNeverPaintsTheLowerLeftVoid() {
        val ribbon = RibbonArtwork()
        val pos = FloatArray(2); val tangent = FloatArray(2)
        PathMeasure(ribbon.orbit, false).getPosTan(0f, pos, tangent)
        assertEquals(208f, pos[0], .1f); assertEquals(1173f, pos[1], .1f)
        assertTrue(tangent[0] < 0 && tangent[1] < 0)
        // The user's reported empty sector, below the left tip and left of the foreground tail.
        val void = Path().apply { addRect(0f, 1190f, 470f, 1536f, Path.Direction.CW) }
        for (path in listOf(ribbon.rear, ribbon.front)) {
            val intersection = Path(path)
            intersection.op(void, Path.Op.INTERSECT)
            assertTrue(intersection.isEmpty)
        }
    }

    @Test fun launchSurfaceMatchesMaterialOnModernAndroid() {
        if (Build.VERSION.SDK_INT < 34) return
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val dark = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        val scheme = if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        assertEquals(scheme.surface.toArgb(), context.getColor(R.color.launch_background))
    }
    @Test fun wordmarkCentersAfterNestedAndPaddedLayoutChanges() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.runOnMainSync {
            val host = android.widget.FrameLayout(instrumentation.targetContext).apply { setPadding(37, 61, 11, 9) }
            val nested = android.widget.FrameLayout(host.context)
            val icon = android.widget.ImageView(host.context)
            nested.addView(icon, android.widget.FrameLayout.LayoutParams(300, 300).apply { leftMargin = 53; topMargin = 22 })
            host.addView(nested, android.widget.FrameLayout.LayoutParams(500, 500).apply { leftMargin = 101; topMargin = 77 })
            fun layout(width: Int) {
                host.measure(android.view.View.MeasureSpec.makeMeasureSpec(width, android.view.View.MeasureSpec.EXACTLY),
                    android.view.View.MeasureSpec.makeMeasureSpec(1920, android.view.View.MeasureSpec.EXACTLY))
                host.layout(0, 0, width, 1920)
                host.viewTreeObserver.dispatchOnGlobalLayout()
            }
            layout(1080)
            val label = SplashWordmarkLayout.attach(host, icon, WordmarkDrawable())
            layout(1080); layout(1080)
            assertEquals(550.5f, label.x + label.width / 2f, .5f)
            val iconBounds = Rect(0, 0, icon.width, icon.height)
            host.offsetDescendantRectToMyCoords(icon, iconBounds)
            assertTrue(label.y > iconBounds.centerY())
            layout(720); layout(720)
            assertEquals(370.5f, label.x + label.width / 2f, .5f)
        }
    }

    @Test fun completeBrandRemainsReadableWithoutExtendingStartup() {
        assertTrue(LogoMotion.BRAND_HOLD >= 700f)
        assertTrue(LogoMotion.DURATION + LogoMotion.FADE <= 2000L)
        fun render(time: Float): Bitmap = Bitmap.createBitmap(724, 245, Bitmap.Config.ARGB_8888).also {
            WordmarkDrawable().apply { setBounds(0, 0, 724, 245); elapsed = time }.draw(Canvas(it))
        }
        val settled = render(LogoMotion.WORDMARK_SETTLED)
        val beforeFade = render(LogoMotion.DURATION.toFloat())
        assertTrue(settled.sameAs(beforeFade))
        var left = settled.width; var right = -1
        for (y in 0 until settled.height) for (x in 0 until settled.width) {
            if (Color.alpha(settled.getPixel(x, y)) > 128) { left = minOf(left, x); right = maxOf(right, x) }
        }
        assertTrue(right > left)
        assertEquals((settled.width - 1) / 2f, (left + right) / 2f, 1f)
        settled.recycle(); beforeFade.recycle()
    }

}
