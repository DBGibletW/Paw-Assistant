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

private val NavyDark = Color(0xFF0F172A)
private val NavySurface = Color(0xFF1E293B)
private val BluePrimaryLight = Color(0xFF1D4ED8)
private val BlueContainerLight = Color(0xFFDBEAFE)
private val OnBlueContainerLight = Color(0xFF0F172A)
private val TurquoiseLight = Color(0xFF0891B2)
private val TurquoiseContainerLight = Color(0xFFCFFAFE)
private val OnTurquoiseContainerLight = Color(0xFF083344)

private val LightBackground = Color(0xFFF8FAFC)
private val LightSurface = Color(0xFFFFFFFF)
private val LightSurfaceVariant = Color(0xFFF1F5F9)

private val BluePrimaryDark = Color(0xFF60A5FA)
private val OnBluePrimaryDark = Color(0xFF0F172A)
private val BlueContainerDark = Color(0xFF1E3A8A)
private val OnBlueContainerDark = Color(0xFFDBEAFE)
private val TurquoiseDark = Color(0xFF22D3EE)
private val TurquoiseContainerDark = Color(0xFF164E63)
private val OnTurquoiseContainerDark = Color(0xFFCFFAFE)

private val DarkBackground = Color(0xFF0B1120)
private val DarkOnText = Color(0xFFF1F5F9)

private val LightColorScheme = lightColorScheme(
    primary = BluePrimaryLight,
    onPrimary = Color.White,
    primaryContainer = BlueContainerLight,
    onPrimaryContainer = OnBlueContainerLight,
    secondary = TurquoiseLight,
    onSecondary = Color.White,
    secondaryContainer = TurquoiseContainerLight,
    onSecondaryContainer = OnTurquoiseContainerLight,
    background = LightBackground,
    onBackground = NavyDark,
    surface = LightSurface,
    onSurface = NavyDark,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF475569)
)

private val DarkColorScheme = darkColorScheme(
    primary = BluePrimaryDark,
    onPrimary = OnBluePrimaryDark,
    primaryContainer = BlueContainerDark,
    onPrimaryContainer = OnBlueContainerDark,
    secondary = TurquoiseDark,
    onSecondary = NavyDark,
    secondaryContainer = TurquoiseContainerDark,
    onSecondaryContainer = OnTurquoiseContainerDark,
    background = DarkBackground,
    onBackground = DarkOnText,
    surface = NavySurface,
    onSurface = DarkOnText,
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = Color(0xFFCBD5E1)
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
