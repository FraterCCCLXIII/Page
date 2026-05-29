package com.pageos.launcher.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pageos.launcher.R
import com.pageos.launcher.launcher.CompanionApp
import com.pageos.launcher.launcher.CompanionCatalog
import com.pageos.launcher.ui.PageViewModel
import com.pageos.launcher.ui.theme.PageTheme

/**
 * First-run setup. Shown automatically only when essential companion apps are
 * missing (see [PageViewModel.startRoute]). Detects installed companions live
 * and re-checks whenever the user returns from an install, so rows flip to
 * "Installed" without a manual refresh.
 */
@Composable
fun SetupScreen(
    viewModel: PageViewModel,
    onFinish: () -> Unit,
) {
    val spacing = PageTheme.spacing
    val installed by viewModel.installedCompanions.collectAsStateWithLifecycle()

    // Re-check install status each time this screen resumes (e.g. back from F-Droid).
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refreshCompanionStatus()
    }

    val essentialInstalled = CompanionCatalog.essential.count { it.packageName in installed }
    val allEssentialsReady = essentialInstalled == CompanionCatalog.essential.size

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = spacing.screenPadding),
    ) {
        item {
            Spacer(Modifier.height(spacing.xl))
            Text(
                text = stringResource(R.string.setup_title),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(spacing.sm))
            Text(
                text = stringResource(R.string.setup_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(spacing.md))
            Text(
                text = stringResource(
                    R.string.setup_progress,
                    essentialInstalled,
                    CompanionCatalog.essential.size,
                ),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(spacing.lg))
            SectionLabel(stringResource(R.string.setup_essential))
        }

        items(CompanionCatalog.essential, key = { it.packageName }) { app ->
            CompanionRow(
                app = app,
                installed = app.packageName in installed,
                onInstall = { viewModel.installCompanion(app) },
            )
        }

        item {
            Spacer(Modifier.height(spacing.md))
            SectionLabel(stringResource(R.string.setup_recommended))
        }

        items(CompanionCatalog.recommended, key = { it.packageName }) { app ->
            CompanionRow(
                app = app,
                installed = app.packageName in installed,
                onInstall = { viewModel.installCompanion(app) },
            )
        }

        item {
            Spacer(Modifier.height(spacing.xl))
            Button(
                onClick = onFinish,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(spacing.cornerRadius),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onBackground,
                    contentColor = MaterialTheme.colorScheme.background,
                ),
            ) {
                Text(
                    text = stringResource(
                        if (allEssentialsReady) R.string.setup_continue else R.string.setup_skip,
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = spacing.sm),
                )
            }
            Spacer(Modifier.height(spacing.xxl))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = PageTheme.spacing.xs),
    )
}

@Composable
private fun CompanionRow(
    app: CompanionApp,
    installed: Boolean,
    onInstall: () -> Unit,
) {
    val spacing = PageTheme.spacing
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = spacing.rowVertical),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = app.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = "${app.purpose} · ${app.source.displayName}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (installed) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = stringResource(R.string.setup_installed),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.End,
                    modifier = Modifier.padding(start = spacing.xs),
                )
            }
        } else {
            OutlinedButton(
                onClick = onInstall,
                shape = RoundedCornerShape(spacing.cornerRadius),
            ) {
                Text(
                    text = stringResource(R.string.setup_install),
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
    }
}
