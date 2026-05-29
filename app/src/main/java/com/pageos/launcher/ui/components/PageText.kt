package com.pageos.launcher.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign

/**
 * The semantic text roles Page uses. Each role binds a single typography token
 * and a default color role from the active [MaterialTheme.colorScheme], so text
 * looks identical across every screen and adapts automatically to dark/light.
 */
enum class PageTextRole {
    /** Large, calm hero text (e.g. the home prompt, setup title). */
    Display,

    /** Section/screen headlines. */
    Headline,

    /** Screen and dialog titles. */
    Title,

    /** Primary readable body copy. */
    Body,

    /** De-emphasized supporting copy (subtitles, hints). */
    BodySecondary,

    /** Small uppercase-friendly labels and captions. */
    Label,
}

/**
 * Canonical text primitive for Page. Prefer this over calling Material3 [Text]
 * with ad-hoc styles so typography and color stay consistent and themeable.
 *
 * Pass [color] only to intentionally override the role's default; otherwise the
 * role decides the color from the theme. Inside a colored container (e.g. a
 * filled button) pass the container's content color so text stays legible.
 */
@Composable
fun PageText(
    text: String,
    role: PageTextRole,
    modifier: Modifier = Modifier,
    color: Color? = null,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
) {
    Text(
        text = text,
        modifier = modifier,
        style = role.textStyle(),
        color = color ?: role.defaultColor(),
        textAlign = textAlign,
        maxLines = maxLines,
    )
}

@Composable
private fun PageTextRole.textStyle() = when (this) {
    PageTextRole.Display -> MaterialTheme.typography.displayLarge
    PageTextRole.Headline -> MaterialTheme.typography.headlineMedium
    PageTextRole.Title -> MaterialTheme.typography.titleLarge
    PageTextRole.Body -> MaterialTheme.typography.bodyLarge
    PageTextRole.BodySecondary -> MaterialTheme.typography.bodyMedium
    PageTextRole.Label -> MaterialTheme.typography.labelLarge
}

@Composable
private fun PageTextRole.defaultColor(): Color = when (this) {
    PageTextRole.Display,
    PageTextRole.Headline,
    PageTextRole.Title,
    PageTextRole.Body -> MaterialTheme.colorScheme.onBackground
    PageTextRole.BodySecondary,
    PageTextRole.Label -> MaterialTheme.colorScheme.onSurfaceVariant
}
