package com.hyakuri.kanaflick.ui.theme

import android.os.Build
import androidx.compose.ui.res.colorResource
import com.hyakuri.kanaflick.R
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun KanaFlickTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colors = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(LocalContext.current)
            else dynamicLightColorScheme(LocalContext.current)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }
    MaterialTheme(
        colorScheme = colors.copy(
            // Same resource as the system splash, including API 34+ dynamic surface roles.
            surface = colorResource(R.color.launch_background),
            tertiaryContainer = if (darkTheme) PracticeSuccessDark else PracticeSuccessLight,
            onTertiaryContainer = if (darkTheme) OnPracticeSuccessDark else OnPracticeSuccessLight,
        ),
        typography = Typography,
        shapes = Shapes(large = RoundedCornerShape(24.dp), extraLarge = RoundedCornerShape(28.dp)),
        content = content,
    )
}


