package com.hyakuri.kanaflick.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.focus.focusProperties

/** Keep the three small page trees alive; animate layers instead of reconstructing a destination. */
@Composable
internal fun RetainedTabs(selected: Int, content: @Composable (Int) -> Unit) {
    val transition = updateTransition(selected, label = "home-tabs")
    val travel = with(LocalDensity.current) { 12.dp.toPx() }
    Box(Modifier.fillMaxSize()) {
        repeat(3) { index ->
            key(index) {
                val opacity = transition.animateFloat(transitionSpec = { tween(180) }, label = "tab-alpha-$index") {
                    if (it == index) 1f else 0f
                }
                val offset = transition.animateFloat(transitionSpec = { tween(220, easing = LinearOutSlowInEasing) }, label = "tab-offset-$index") {
                    when { it < index -> travel; it > index -> -travel; else -> 0f }
                }
                // Only visibility boundaries invalidate layout; animation frames update layers.
                val visible by remember(selected, index, opacity) {
                    derivedStateOf { index == selected || opacity.value > 0f }
                }
                Layout(content = { content(index) }, modifier = Modifier.fillMaxSize()
                    .zIndex(if (index == selected) 1f else 0f)
                    .focusProperties { canFocus = index == selected }
                    .pointerInput(index == selected) {
                        if (index != selected) awaitPointerEventScope {
                            while (true) awaitPointerEvent(PointerEventPass.Initial).changes.forEach { it.consume() }
                        }
                    }
                    .then(if (index != selected) Modifier.clearAndSetSemantics { } else Modifier)
                    .graphicsLayer { alpha = opacity.value; translationX = offset.value }) { measurables, constraints ->
                    val children = if (visible) measurables.map { it.measure(constraints) } else emptyList()
                    layout(constraints.maxWidth, constraints.maxHeight) {
                        // Hidden pages retain state but are not drawn or hit-tested.
                        children.forEach { it.place(0, 0) }
                    }
                }
            }
        }
    }
}
