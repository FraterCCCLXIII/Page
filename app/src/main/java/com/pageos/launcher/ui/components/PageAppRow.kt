package com.pageos.launcher.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmapOrNull
import com.pageos.launcher.data.AppInfo
import com.pageos.launcher.ui.theme.PageTheme

/**
 * A single text-first app row. The icon slot is opt-in (text-first by default)
 * and is shown only when [showIcon] is true and the app carries an icon.
 */
@Composable
fun PageAppRow(
    app: AppInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showIcon: Boolean = false,
) {
    val spacing = PageTheme.spacing
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(spacing.cornerRadius),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = spacing.rowVertical, horizontal = spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (showIcon) {
                AppIcon(app)
            }
            Text(
                text = app.label,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
private fun AppIcon(app: AppInfo) {
    val bitmap = remember(app.componentKey, app.icon) {
        app.icon?.toBitmapOrNull()?.asImageBitmap()
    }
    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = null,
            modifier = Modifier.size(28.dp),
        )
    } else {
        // Keep the text baseline aligned even when no icon is available.
        androidx.compose.foundation.layout.Spacer(Modifier.size(28.dp))
    }
}
