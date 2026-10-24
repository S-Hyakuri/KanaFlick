package com.hyakuri.kanaflick.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.unit.IntSize
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.hyakuri.kanaflick.ui.startup.LogoDrawable
import com.hyakuri.kanaflick.ui.startup.LogoMotion
import com.hyakuri.kanaflick.ui.startup.WordmarkDrawable

/** The supplied brand artwork, static and transparent on the home surface. */
@Composable
internal fun BrandWordmark(modifier: Modifier = Modifier) {
    val artwork = remember { WordmarkDrawable().apply { elapsed = LogoMotion.WORDMARK_SETTLED } }
    Box(modifier.width(187.2.dp)
        .aspectRatio(artwork.intrinsicWidth.toFloat() / artwork.intrinsicHeight)
        .semantics { contentDescription = "KanaFlick"; heading() }
        .drawWithCache {
            // Render once per size at 3x device pixels, then filter down for smooth edge coverage.
            val width = size.width.toInt().coerceAtLeast(1)
            val height = size.height.toInt().coerceAtLeast(1)
            val bitmap = Bitmap.createBitmap(width * 3, height * 3, Bitmap.Config.ARGB_8888)
            artwork.setBounds(0, 0, bitmap.width, bitmap.height)
            artwork.draw(AndroidCanvas(bitmap))
            val image = bitmap.asImageBitmap()
            onDrawBehind { drawImage(image, dstSize = IntSize(width, height), filterQuality = FilterQuality.High) }
        })
}

@Composable
internal fun HomeBrandIcon() {
    val artwork = remember { LogoDrawable(normalization = 1.5f).apply { elapsed = LogoMotion.DURATION.toFloat() } }
    Box(Modifier.size(64.dp).drawWithCache {
        val width = size.width.toInt().coerceAtLeast(1)
        val height = size.height.toInt().coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(width * 3, height * 3, Bitmap.Config.ARGB_8888)
        artwork.setBounds(0, 0, bitmap.width, bitmap.height)
        artwork.draw(AndroidCanvas(bitmap))
        val image = bitmap.asImageBitmap()
        onDrawBehind { drawImage(image, dstSize = IntSize(width, height), filterQuality = FilterQuality.High) }
    })
}
