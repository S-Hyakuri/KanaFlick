package com.hyakuri.kanaflick.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hyakuri.kanaflick.practice.*

/** Stable keys keep the same word moving through slots, including interrupted transitions. */
@Composable
internal fun WordCarousel(session: PracticeState) {
    BoxWithConstraints(Modifier.fillMaxWidth().heightIn(min = 80.dp).clipToBounds()) {
        val slot = maxWidth / 3
        session.words.forEachIndexed { index, word ->
            key(index) {
                val relative = index - session.index
                val position by animateFloatAsState(relative.toFloat(), tween(220), label = "word-position")
                val scale by animateFloatAsState(if (relative == 0) 1f else 0.76f, tween(220), label = "word-size")
                val opacity by animateFloatAsState(if (relative in -1..1) 1f else 0f, tween(220), label = "word-visibility")
                val previous = relative == -1
                val passed = session.previousFeedback == Feedback.CORRECT
                val color by animateColorAsState(when {
                    previous && passed -> MaterialTheme.colorScheme.tertiaryContainer
                    previous -> MaterialTheme.colorScheme.errorContainer
                    relative == 0 -> MaterialTheme.colorScheme.primaryContainer
                    else -> MaterialTheme.colorScheme.surfaceContainerLow
                }, tween(220), label = "word-color")
                val ink = when {
                    previous && passed -> MaterialTheme.colorScheme.onTertiaryContainer
                    previous -> MaterialTheme.colorScheme.onErrorContainer
                    relative == 0 -> MaterialTheme.colorScheme.onPrimaryContainer
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                Surface(Modifier.width(slot).align(Alignment.Center).graphicsLayer {
                    translationX = position * slot.toPx()
                    scaleX = scale; scaleY = scale; alpha = opacity
                }.then(if (relative == 0) Modifier else Modifier.clearAndSetSemantics {
                    if (relative == -1) contentDescription = "上一词 ${word.text}，${if (passed) "正确" else "已跳过"}"
                    if (relative == 1) contentDescription = "下一词 ${word.text}"
                }), shape = MaterialTheme.shapes.large, color = color) {
                    Column(Modifier.padding(horizontal = 4.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(word.text, modifier = if (relative == 0) Modifier.testTag("target") else Modifier, maxLines = 3, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, style = MaterialTheme.typography.headlineLarge, textAlign = TextAlign.Center, color = ink)
                        if (previous) Text(if (passed) "✓ 正确" else "已跳过", style = MaterialTheme.typography.labelSmall, color = ink)
                    }
                }
            }
        }
    }
}