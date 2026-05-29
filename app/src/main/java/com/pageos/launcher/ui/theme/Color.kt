package com.pageos.launcher.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Page color tokens. Monochrome by design.
 *
 * Page ships dark-first; light tokens are defined now so a light theme can be
 * switched on in a later phase without touching component code.
 */
object PageColors {
    // Dark (default)
    val DarkBackground = Color(0xFF000000)
    val DarkSurface = Color(0xFF0A0A0A)
    val DarkSurfaceVariant = Color(0xFF161616)
    val DarkOnBackground = Color(0xFFF2F2F2)
    val DarkOnSurface = Color(0xFFF2F2F2)
    val DarkOnSurfaceDim = Color(0xFF8A8A8A)
    val DarkOutline = Color(0xFF2A2A2A)

    // Light (reserved for a future phase)
    val LightBackground = Color(0xFFFFFFFF)
    val LightSurface = Color(0xFFF6F6F6)
    val LightSurfaceVariant = Color(0xFFEDEDED)
    val LightOnBackground = Color(0xFF0A0A0A)
    val LightOnSurface = Color(0xFF0A0A0A)
    val LightOnSurfaceDim = Color(0xFF6A6A6A)
    val LightOutline = Color(0xFFD8D8D8)
}
