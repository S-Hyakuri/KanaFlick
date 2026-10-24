package com.hyakuri.kanaflick.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp

/** Allocate only measured spare room; larger text and keyboards reclaim decorative gaps. */
private object PracticeSpacing : Arrangement.Vertical {
    override val spacing = 4.dp
    override fun Density.arrange(totalSize: Int, sizes: IntArray, outPositions: IntArray) {
        val baseGap = spacing.roundToPx()
        val spare = (totalSize - sizes.sum() - baseGap * (sizes.size - 1).coerceAtLeast(0)).coerceAtLeast(0)
        val topBudget = 32.dp.roundToPx()
        val answerBudget = 44.dp.roundToPx()
        val budget = topBudget + answerBudget
        val available = spare.coerceAtMost(budget)
        val topGap = if (budget == 0) 0 else available * topBudget / budget
        val answerGap = available - topGap
        var y = 0
        sizes.forEachIndexed { index, height ->
            // Header, progress, words, meaning, input group, feedback, counts.
            if (index == 2) y += topGap
            if (index == 4) y += answerGap
            outPositions[index] = y
            y += height + baseGap
        }
    }
}

@Composable
internal fun PracticeBody(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier.fillMaxSize()) {
        BoxWithConstraints(Modifier.safeDrawingPadding().imePadding().fillMaxSize().padding(horizontal = 20.dp, vertical = 4.dp)) {
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).heightIn(min = maxHeight),
                verticalArrangement = PracticeSpacing, content = content)
        }
    }
}