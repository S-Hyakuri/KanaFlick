package com.hyakuri.kanaflick.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import com.hyakuri.kanaflick.R

@Composable
internal fun AppLogo(size: Dp) {
    Image(painterResource(R.drawable.kanaflick_logo), contentDescription = null,
        modifier = Modifier.size(size).clip(MaterialTheme.shapes.large))
}
