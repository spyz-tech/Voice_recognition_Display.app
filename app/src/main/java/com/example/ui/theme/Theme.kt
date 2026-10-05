package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = LedRed,
    secondary = LedAmber,
    tertiary = LedGreen,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // LED Voice Sign is always an immersive dark cyberpunk LED experience
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
