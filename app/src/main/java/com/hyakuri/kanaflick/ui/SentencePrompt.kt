package com.hyakuri.kanaflick.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.hyakuri.kanaflick.practice.*

/** Pixel offset derives exclusively from the live matching prefix, including deletion. */
@Composable internal fun SentencePrompt(target: String, confirmed: String) {
    val expected = remember(target) { normalizeSentence(target) }
    val prefix = matchingPrefix(confirmed, expected)
    val measurer = rememberTextMeasurer()
    val style = MaterialTheme.typography.headlineLarge
    val layout = remember(expected, style, measurer) { measurer.measure(AnnotatedString(expected), style, softWrap = false) }
    val offset = if (prefix < expected.length) layout.getCursorRect(prefix).left else layout.size.width.toFloat()
    val scroll by animateFloatAsState(offset, tween(100), label = "confirmed-prefix")
    val ink = MaterialTheme.colorScheme.onSurface
    val farInk = MaterialTheme.colorScheme.onSurfaceVariant
    val marker = MaterialTheme.colorScheme.primary
    Surface(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainerLow) {
        Canvas(Modifier.fillMaxWidth().height(with(androidx.compose.ui.platform.LocalDensity.current) { layout.size.height.toDp() } + 32.dp)
            .testTag("sentence-prompt").semantics {
                contentDescription = "待输入文本：$expected"
                stateDescription = "已匹配 $prefix 字"
            }) {
            val inset = 16.dp.toPx()
            val width = (size.width - 2 * inset).coerceAtLeast(1f)
            clipRect(inset, 0f, size.width - inset, size.height) {
                drawText(layout, brush = Brush.horizontalGradient(listOf(ink, farInk), startX = scroll, endX = scroll + width),
                    topLeft = Offset(inset - scroll, 10.dp.toPx()))
            }
            drawLine(marker, Offset(inset, size.height - 6.dp.toPx()), Offset(inset + 24.dp.toPx(), size.height - 6.dp.toPx()), 2.dp.toPx())
        }
    }
}
