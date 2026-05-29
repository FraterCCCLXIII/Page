package com.pageos.launcher.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pageos.launcher.ui.theme.PageTheme

/**
 * Page's button styles. All variants share the same shape, spacing, and text
 * treatment and pull every color from the active theme, so they look identical
 * across screens and adapt automatically to dark/light.
 */
enum class PageButtonVariant {
    /** High-emphasis filled action (the primary call to action on a screen). */
    Primary,

    /** Medium-emphasis outlined action. */
    Secondary,

    /** Low-emphasis, chrome-free action (Page's calm text-first default). */
    Ghost,
}

/**
 * Canonical button for Page. Prefer this over Material3 `Button`/`OutlinedButton`
 * with ad-hoc colors so every action shares one consistent, themeable style.
 *
 * Width is caller-controlled: pass `Modifier.fillMaxWidth()` for a full-width
 * action, or leave the default to wrap the label.
 */
@Composable
fun PageButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: PageButtonVariant = PageButtonVariant.Primary,
    textRole: PageTextRole = PageTextRole.Body,
    leading: (@Composable () -> Unit)? = null,
) {
    val spacing = PageTheme.spacing
    val container = variant.containerColor()
    val content = variant.contentColor()
    val border = variant.borderStroke()

    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(spacing.cornerRadius),
        color = container,
        contentColor = content,
        border = border,
    ) {
        Row(
            modifier = Modifier.padding(
                vertical = spacing.rowVertical,
                horizontal = spacing.md,
            ),
            horizontalArrangement = Arrangement.spacedBy(spacing.md, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leading?.invoke()
            PageText(
                text = label,
                role = textRole,
                color = LocalContentColor.current,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun PageButtonVariant.containerColor(): Color = when (this) {
    PageButtonVariant.Primary -> MaterialTheme.colorScheme.primary
    PageButtonVariant.Secondary,
    PageButtonVariant.Ghost -> Color.Transparent
}

@Composable
private fun PageButtonVariant.contentColor(): Color = when (this) {
    PageButtonVariant.Primary -> MaterialTheme.colorScheme.onPrimary
    PageButtonVariant.Secondary,
    PageButtonVariant.Ghost -> MaterialTheme.colorScheme.onBackground
}

@Composable
private fun PageButtonVariant.borderStroke(): BorderStroke? = when (this) {
    PageButtonVariant.Secondary -> BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    else -> null
}
