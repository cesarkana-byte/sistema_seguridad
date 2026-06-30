package com.example.seguridad.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = SecurityPrimary,
    onPrimary = Color.White,
    primaryContainer = SecuritySurfaceVariant,
    onPrimaryContainer = SecurityText,

    secondary = SecurityAccent,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE3C2),
    onSecondaryContainer = SecurityText,

    tertiary = SecurityInfo,
    onTertiary = Color.White,

    background = SecurityBackground,
    onBackground = SecurityText,

    surface = SecuritySurface,
    onSurface = SecurityText,

    surfaceVariant = SecuritySurfaceVariant,
    onSurfaceVariant = SecurityTextSoft,

    outline = SecurityBorder,
    outlineVariant = Color(0xFFE7D8CD),

    error = SecurityDanger,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = SecurityAccent,
    onPrimary = Color.Black,
    primaryContainer = SecurityPrimaryDark,
    onPrimaryContainer = SecurityDarkText,

    secondary = SecurityAccent,
    onSecondary = Color.Black,

    background = SecurityDarkBackground,
    onBackground = SecurityDarkText,

    surface = SecurityDarkSurface,
    onSurface = SecurityDarkText,

    surfaceVariant = SecurityDarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFD8D0C8),

    outline = Color(0xFF4A4F55),
    outlineVariant = Color(0xFF363B40),

    error = Color(0xFFFF6B6B),
    onError = Color.Black
)

@Composable
fun SeguridadTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
