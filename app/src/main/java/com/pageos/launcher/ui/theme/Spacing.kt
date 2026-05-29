package com.pageos.launcher.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Spacing and shape tokens for Page's calm, roomy layout.
 *
 * Exposed via [LocalPageSpacing] so components reference tokens instead of
 * hardcoded values, keeping spacing themeable in one place.
 */
data class PageSpacing(
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 16.dp,
    val lg: Dp = 24.dp,
    val xl: Dp = 32.dp,
    val xxl: Dp = 48.dp,
    val screenPadding: Dp = 24.dp,
    val rowVertical: Dp = 14.dp,
    val cornerRadius: Dp = 16.dp,
)

val LocalPageSpacing = staticCompositionLocalOf { PageSpacing() }
