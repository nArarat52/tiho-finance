package com.example.financetracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Forest = Color(0xFF244C3E)
val Canvas = Color(0xFFF6F7F2)
val Ink = Color(0xFF20382E)
val Muted = Color(0xFF7A857D)
val Lime = Color(0xFFD7E9A4)
val Line = Color(0xFFE7EAE2)
@Composable
fun FinanceTrackerTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = lightColorScheme(primary = Forest, onPrimary = Color.White,
        secondary = Forest, background = Canvas, surface = Color.White, onBackground = Ink,
        onSurface = Ink, outline = Line, outlineVariant = Line, primaryContainer = Lime,
        onPrimaryContainer = Ink, secondaryContainer = Lime, onSecondaryContainer = Forest,
        tertiary = Forest, surfaceVariant = Canvas, onSurfaceVariant = Muted,
        surfaceContainerHigh = Canvas), typography = Typography, content = content)
}
