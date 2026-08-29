package com.danger.haztrack.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = HaztrackBlueDark,
    onPrimary = HaztrackDarkBackground,
    primaryContainer = HaztrackBlueDarkContainer,
    onPrimaryContainer = HaztrackBlueContainer,
    secondary = HaztrackBlueDark,
    onSecondary = HaztrackDarkBackground,
    background = HaztrackDarkBackground,
    onBackground = HaztrackDarkOnSurface,
    surface = HaztrackDarkSurface,
    onSurface = HaztrackDarkOnSurface,
    surfaceVariant = HaztrackDarkSurfaceVariant,
    onSurfaceVariant = HaztrackDarkOnSurfaceVariant,
    outline = HaztrackDarkOutline
)

private val LightColorScheme = lightColorScheme(
    primary = HaztrackBlue,
    onPrimary = HaztrackLightBackground,
    primaryContainer = HaztrackBlueContainer,
    onPrimaryContainer = HaztrackLightOnSurface,
    secondary = HaztrackBlue,
    onSecondary = HaztrackLightBackground,
    background = HaztrackLightBackground,
    onBackground = HaztrackLightOnSurface,
    surface = HaztrackLightBackground,
    onSurface = HaztrackLightOnSurface,
    surfaceVariant = HaztrackLightSurfaceVariant,
    onSurfaceVariant = HaztrackLightOnSurfaceVariant,
    outline = HaztrackLightOutline
)

@Suppress("FunctionNaming")
@Composable
fun HaztrackTheme(
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
