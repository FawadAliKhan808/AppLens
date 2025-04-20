package com.android.applens.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

private val AppColorScheme = lightColorScheme(
    primary = BlueDark,
    onPrimary = TextBlue,
    secondary = SkyAccent,
    background = BackgroundDark,
    onBackground = TextBlue,
    surface = SurfaceDark,
    onSurface = TextBlue
)

@Composable
fun AppLensTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = Typography,
        content = content
    )
}
