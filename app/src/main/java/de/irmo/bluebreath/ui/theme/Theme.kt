package de.irmo.bluebreath.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = LightBluePrimary,
    onPrimary = LightBlueOnPrimary,
    primaryContainer = LightBluePrimaryContainer,
    onPrimaryContainer = LightBlueOnPrimaryContainer,
    secondary = LightBlueSecondary,
    onSecondary = LightBlueOnSecondary,
    secondaryContainer = LightBlueSecondaryContainer,
    onSecondaryContainer = LightBlueOnSecondaryContainer,
    tertiary = LightBlueTertiary,
    onTertiary = LightBlueOnTertiary,
    tertiaryContainer = LightBlueTertiaryContainer,
    onTertiaryContainer = LightBlueOnTertiaryContainer,
    error = LightBlueError,
    onError = LightBlueOnError,
    errorContainer = LightBlueErrorContainer,
    onErrorContainer = LightBlueOnErrorContainer,
    background = LightBlueBackground,
    onBackground = LightBlueOnBackground,
    surface = LightBlueSurface,
    onSurface = LightBlueOnSurface,
    surfaceVariant = LightBlueSurfaceVariant,
    onSurfaceVariant = LightBlueOnSurfaceVariant,
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkBluePrimary,
    onPrimary = DarkBlueOnPrimary,
    primaryContainer = DarkBluePrimaryContainer,
    onPrimaryContainer = DarkBlueOnPrimaryContainer,
    secondary = DarkBlueSecondary,
    onSecondary = DarkBlueOnSecondary,
    secondaryContainer = DarkBlueSecondaryContainer,
    onSecondaryContainer = DarkBlueOnSecondaryContainer,
    tertiary = DarkBlueTertiary,
    onTertiary = DarkBlueOnTertiary,
    tertiaryContainer = DarkBlueTertiaryContainer,
    onTertiaryContainer = DarkBlueOnTertiaryContainer,
    error = DarkBlueError,
    onError = DarkBlueOnError,
    errorContainer = DarkBlueErrorContainer,
    onErrorContainer = DarkBlueOnErrorContainer,
    background = DarkBlueBackground,
    onBackground = DarkBlueOnBackground,
    surface = DarkBlueSurface,
    onSurface = DarkBlueOnSurface,
    surfaceVariant = DarkBlueSurfaceVariant,
    onSurfaceVariant = DarkBlueOnSurfaceVariant,
)

@Composable
fun BlueBreathTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
