package com.pageos.launcher.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pageos.launcher.R
import com.pageos.launcher.launcher.HomeAction
import com.pageos.launcher.launcher.LaunchOutcome
import com.pageos.launcher.ui.CommandResult
import com.pageos.launcher.ui.PageViewModel
import com.pageos.launcher.ui.components.PageCommandBar
import com.pageos.launcher.ui.components.PageTextButton
import com.pageos.launcher.ui.theme.PageTheme

/** Ordered primary actions shown on the home screen. */
private val primaryActions: List<Pair<HomeAction, Int>> = listOf(
    HomeAction.PHONE to R.string.action_phone,
    HomeAction.MESSAGES to R.string.action_messages,
    HomeAction.MAPS to R.string.action_maps,
    HomeAction.BOOKS to R.string.action_books,
    HomeAction.NOTES to R.string.action_notes,
    HomeAction.MUSIC to R.string.action_music,
    HomeAction.CAMERA to R.string.action_camera,
    HomeAction.CALENDAR to R.string.action_calendar,
    HomeAction.FILES to R.string.action_files,
    HomeAction.SETTINGS to R.string.action_settings,
)

@Composable
fun HomeScreen(
    viewModel: PageViewModel,
    onOpenApps: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenSearch: (String) -> Unit,
    onMessage: (String) -> Unit,
) {
    val spacing = PageTheme.spacing
    var query by remember { mutableStateOf("") }
    val showCommandBar by viewModel.showCommandBar.collectAsStateWithLifecycle()
    val noAppMessage = stringResource(R.string.no_app_installed)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = spacing.screenPadding),
    ) {
        // Primary actions: vertically centered when they fit, scrollable if not.
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(spacing.xs),
            ) {
                Spacer(Modifier.height(spacing.lg))
                primaryActions.forEach { (action, labelRes) ->
                    PageTextButton(
                        label = stringResource(labelRes),
                        onClick = {
                            when (action) {
                                HomeAction.SETTINGS -> onOpenSettings()
                                else -> {
                                    val outcome = viewModel.performHomeAction(action)
                                    if (outcome == LaunchOutcome.NoAppAvailable) onMessage(noAppMessage)
                                }
                            }
                        },
                    )
                }
                Spacer(Modifier.height(spacing.sm))
                PageTextButton(
                    label = stringResource(R.string.all_apps),
                    onClick = onOpenApps,
                )
                Spacer(Modifier.height(spacing.lg))
            }
        }

        // The command bar sticks to the bottom and only appears when enabled.
        if (showCommandBar) {
            PageCommandBar(
                value = query,
                onValueChange = { query = it },
                onSubmit = {
                    handleCommand(
                        raw = query,
                        viewModel = viewModel,
                        onOpenSettings = onOpenSettings,
                        onOpenSearch = onOpenSearch,
                        onNoApp = { onMessage(noAppMessage) },
                    )
                    query = ""
                },
                hint = stringResource(R.string.command_hint),
                modifier = Modifier
                    .imePadding()
                    .padding(top = spacing.sm, bottom = spacing.lg),
            )
        }
    }
}

private fun handleCommand(
    raw: String,
    viewModel: PageViewModel,
    onOpenSettings: () -> Unit,
    onOpenSearch: (String) -> Unit,
    onNoApp: () -> Unit,
) {
    when (val result = viewModel.runCommand(raw)) {
        is CommandResult.GoToSettings -> onOpenSettings()
        is CommandResult.GoToSearch -> onOpenSearch(result.query)
        is CommandResult.NoAppAvailable -> onNoApp()
        is CommandResult.OpenedExternal -> Unit
        is CommandResult.Empty -> Unit
    }
}
