package com.lingkarmas.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun LingkarMasTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Navy, onPrimary = Color.White,
            secondary = Gold, onSecondary = Navy,
            background = Bg, surface = Color.White, onSurface = Navy
        ),
        content = content
    )
}