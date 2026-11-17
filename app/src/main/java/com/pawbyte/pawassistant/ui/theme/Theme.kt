package com.pawbyte.pawassistant.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1A73E8),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF1F3F4),
    onPrimaryContainer = Color(0xFF1F1F1F),
    secondary = Color(0xFF0891B2),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8F0FE),
    onSecondaryContainer = Color(0xFF174EA6),
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1F1F1F),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1F1F1F),
    surfaceVariant = Color(0xFFF1F3F4),
    onSurfaceVariant = Color(0xFF5F6368),
    outline = Color(0xFFDADCE0)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF8AB4F8),
    onPrimary = Color(0xFF0F172A),
    primaryContainer = Color(0xFF242B35),
    onPrimaryContainer = Color(0xFFE8EAED),
    secondary = Color(0xFF22D3EE),
    onSecondary = Color(0xFF0F172A),
    secondaryContainer = Color(0xFF1E3A5F),
    onSecondaryContainer = Color(0xFFD2E3FC),
    background = Color(0xFF0F131A),
    onBackground = Color(0xFFE8EAED),
    surface = Color(0xFF171C24),
    onSurface = Color(0xFFE8EAED),
    surfaceVariant = Color(0xFF242B35),
    onSurfaceVariant = Color(0xFF9AA0A6),
    outline = Color(0xFF303642)
)

@Composable
fun PawAssistantTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
