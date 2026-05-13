// ui/theme/Theme.kt
package com.example.stockai.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary          = Color(0xFF2979FF),
    background       = Color(0xFF070B14),
    surface          = Color(0xFF0F1923),
    onPrimary        = Color.White,
    onBackground     = Color(0xFFF0F4FF),
    onSurface        = Color(0xFFF0F4FF),
    secondary        = Color(0xFF00E5A0),
    error            = Color(0xFFFF4560),
)

@Composable
fun StockAITheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content     = content
    )
}