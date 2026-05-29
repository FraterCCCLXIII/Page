package com.pageos.launcher.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.pageos.launcher.data.ThemeMode

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
 * Page's root theme. A single source of truth for color, type, and spacing
 * tokens so every screen and component themes consistently in both modes.
 *
 * @param darkTheme Whether to render the dark palette. Defaults to the system
 *   setting; pass an explicit value to honor a user override.
 */
@Composable
fun PageTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) PageDarkColorScheme else PageLightColorScheme

    // Keep the system bars in step with the active palette so the status/nav
    // bar icons stay legible against Page's background in either mode.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = colorScheme.background.toArgb()
            @Suppress("DEPRECATION")
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

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

/** Resolves a persisted [ThemeMode] to a concrete dark/light value. */
@Composable
fun ThemeMode.resolveIsDark(): Boolean = when (this) {
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
}
