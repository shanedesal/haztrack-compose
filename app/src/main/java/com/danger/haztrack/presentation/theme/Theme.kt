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
    secondary = HaztrackBlueBright,
    onSecondary = HaztrackDarkBackground,
    secondaryContainer = HaztrackDarkSurfaceVariant,
    onSecondaryContainer = HaztrackDarkOnSurface,
    tertiary = HaztrackBlueDark,
    onTertiary = HaztrackDarkBackground,
    background = HaztrackDarkBackground,
    onBackground = HaztrackDarkOnSurface,
    surface = HaztrackDarkSurface,
    onSurface = HaztrackDarkOnSurface,
    surfaceVariant = HaztrackDarkSurfaceVariant,
    onSurfaceVariant = HaztrackDarkOnSurfaceVariant,
    surfaceContainerLowest = HaztrackDarkBackground,
    surfaceContainerLow = HaztrackDarkSurface,
    surfaceContainer = HaztrackDarkSurfaceElevated,
    surfaceContainerHigh = HaztrackDarkSurfaceVariant,
    surfaceContainerHighest = HaztrackDarkSurfaceVariant,
    outline = HaztrackDarkOutline,
    outlineVariant = HaztrackDarkOutlineVariant,
)

private val LightColorScheme = lightColorScheme(
    primary = HaztrackBlue,
    onPrimary = HaztrackLightBackground,
    primaryContainer = HaztrackBlueContainer,
    onPrimaryContainer = HaztrackBlueDeep,
    secondary = HaztrackBlueDeep,
    onSecondary = HaztrackLightBackground,
    secondaryContainer = HaztrackLightSurfaceVariant,
    onSecondaryContainer = HaztrackLightOnSurface,
    tertiary = HaztrackBlue,
    onTertiary = HaztrackLightBackground,
    background = HaztrackLightBackground,
    onBackground = HaztrackLightOnSurface,
    surface = HaztrackLightSurface,
    onSurface = HaztrackLightOnSurface,
    surfaceVariant = HaztrackLightSurfaceVariant,
    onSurfaceVariant = HaztrackLightOnSurfaceVariant,
    surfaceContainerLowest = HaztrackLightBackground,
    surfaceContainerLow = HaztrackLightBackground,
    surfaceContainer = HaztrackLightSurfaceElevated,
    surfaceContainerHigh = HaztrackLightSurfaceVariant,
    surfaceContainerHighest = HaztrackLightSurfaceVariant,
    outline = HaztrackLightOutline,
    outlineVariant = HaztrackLightOutlineVariant,
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
        shapes = Shapes,
        content = content
    )
}
