package com.pageos.launcher.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * The primary text-first action control on the home screen.
 *
 * A full-width [PageButton] ghost variant with large, calm, centered text.
 * No icon by default to keep the home screen quiet; a leading slot is available
 * for a future "show icons" mode.
 */
@Composable
fun PageTextButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leading: (@Composable () -> Unit)? = null,
) {
    PageButton(
        label = label,
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        variant = PageButtonVariant.Ghost,
        textRole = PageTextRole.Headline,
        leading = leading,
    )
}
