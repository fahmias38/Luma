package com.pemmob.luma.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = SakuPrimary,
    secondary = SakuSecondary,
    tertiary = SakuTertiary,
    background = SakuNeutralDark,
    surface = Color(0xFF1E293B),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = SakuPrimary,
    secondary = SakuSecondary,
    tertiary = SakuTertiary,
    background = SakuCanvasBackground,
    surface = SakuCardBackground,
    onPrimary = Color.White,
    onSecondary = Color(0xFF4338CA),
    onTertiary = Color.White,
    onBackground = SakuTextDark,
    onSurface = SakuTextDark
)

@Composable
fun LUMATheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) darkColorScheme() else lightColorScheme()
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
