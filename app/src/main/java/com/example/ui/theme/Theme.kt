package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = RazorBlue,
    secondary = InstaPink,
    tertiary = RazorTeal,
    background = ObsidianDark,
    surface = CardBackground,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = LightText,
    onSurface = LightText,
    surfaceVariant = MinimalSurfaceContainer,
    onSurfaceVariant = GrayText,
    outline = MinimalBorder
)

private val LightColorScheme = darkColorScheme( // Enforce high-fidelity dark mode by default for that premium cyberpunk experience
    primary = RazorBlue,
    secondary = InstaPink,
    tertiary = RazorTeal,
    background = ObsidianDark,
    surface = CardBackground,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = LightText,
    onSurface = LightText,
    surfaceVariant = MinimalSurfaceContainer,
    onSurfaceVariant = GrayText,
    outline = MinimalBorder
)

@Composable
fun SocialHubTheme(
    darkTheme: Boolean = true, // Force the brilliant Dark space layout shown in the PDF
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
