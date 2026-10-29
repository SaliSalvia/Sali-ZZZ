package com.salizzai.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = SaliPurpleLight,
    onPrimary = Color.White,
    primaryContainer = SaliPurpleDark,
    onPrimaryContainer = Color.White,
    secondary = SaliPurpleAccent,
    onSecondary = Color.White,
    secondaryContainer = SaliPurpleDark,
    onSecondaryContainer = Color.White,
    tertiary = SaliPurpleAccent,
    onTertiary = Color.White,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutline,
    error = ErrorRed,
    onError = Color.White,
    errorContainer = ErrorRed.copy(alpha = 0.15f),
    onErrorContainer = ErrorRed,
)

private val LightColorScheme = lightColorScheme(
    primary = SaliPurple,
    onPrimary = Color.White,
    primaryContainer = SaliPurpleLight,
    onPrimaryContainer = Color.White,
    secondary = SaliPurpleAccent,
    onSecondary = Color.White,
    secondaryContainer = SaliPurpleLight,
    onSecondaryContainer = Color.White,
    tertiary = SaliPurpleAccent,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    outlineVariant = LightOutline,
    error = ErrorRed,
    onError = Color.White,
    errorContainer = ErrorRed.copy(alpha = 0.10f),
    onErrorContainer = ErrorRed,
)

val SaliShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun SaliZZaiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = SaliTypography,
        shapes = SaliShapes,
        content = content
    )
}
