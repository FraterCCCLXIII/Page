package com.pageos.launcher.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val PageDarkColorScheme = darkColorScheme(
    background = PageColors.DarkBackground,
    onBackground = PageColors.DarkOnBackground,
    surface = PageColors.DarkSurface,
    onSurface = PageColors.DarkOnSurface,
    surfaceVariant = PageColors.DarkSurfaceVariant,
    onSurfaceVariant = PageColors.DarkOnSurfaceDim,
    primary = PageColors.DarkOnBackground,
    onPrimary = PageColors.DarkBackground,
    outline = PageColors.DarkOutline,
)

private val PageLightColorScheme = lightColorScheme(
    background = PageColors.LightBackground,
    onBackground = PageColors.LightOnBackground,
    surface = PageColors.LightSurface,
    onSurface = PageColors.LightOnSurface,
    surfaceVariant = PageColors.LightSurfaceVariant,
    onSurfaceVariant = PageColors.LightOnSurfaceDim,
    primary = PageColors.LightOnBackground,
    onPrimary = PageColors.LightBackground,
    outline = PageColors.LightOutline,
)

/**
 * Page's root theme.
 *
 * @param darkTheme Page ships monochrome-dark first, so this defaults to dark
 *   regardless of the system setting. Pass the system value to opt into light
 *   mode once it is supported.
 */
@Composable
fun PageTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) PageDarkColorScheme else PageLightColorScheme

    CompositionLocalProvider(LocalPageSpacing provides PageSpacing()) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = PageTypography,
            content = content,
        )
    }
}

/** Convenience accessor mirroring [MaterialTheme] for spacing tokens. */
object PageTheme {
    val spacing: PageSpacing
        @Composable get() = LocalPageSpacing.current
}

/** Reserved for future use: respect the system dark/light setting. */
@Composable
fun systemDarkTheme(): Boolean = isSystemInDarkTheme()
