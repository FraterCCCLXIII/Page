package com.pageos.launcher.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pageos.launcher.R
import com.pageos.launcher.ui.CommandResult
import com.pageos.launcher.ui.PageViewModel
import com.pageos.launcher.ui.components.PageAppRow
import com.pageos.launcher.ui.components.PageCommandBar
import com.pageos.launcher.ui.components.PageScreenHeader
import com.pageos.launcher.ui.theme.PageTheme

/**
 * Live app + command search. The same field drives command parsing on submit
 * (e.g. "wifi") and incremental app filtering as the user types.
 */
@Composable
fun SearchScreen(
    viewModel: PageViewModel,
    initialQuery: String,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onMessage: (String) -> Unit,
) {
    val spacing = PageTheme.spacing
    var query by remember { mutableStateOf(initialQuery) }
    val showIcons by viewModel.showAppIcons.collectAsStateWithLifecycle()
    val apps by viewModel.apps.collectAsStateWithLifecycle()
    val noAppMessage = stringResource(R.string.no_app_installed)

    val results = remember(query, apps) { viewModel.filterApps(query) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = spacing.screenPadding),
    ) {
        PageScreenHeader(
            title = stringResource(R.string.search),
            onBack = onBack,
        )

        PageCommandBar(
            value = query,
            onValueChange = { query = it },
            onSubmit = {
                when (val result = viewModel.runCommand(query)) {
                    is CommandResult.GoToSettings -> onOpenSettings()
                    is CommandResult.GoToSearch -> query = result.query
                    is CommandResult.NoAppAvailable -> onMessage(noAppMessage)
                    is CommandResult.OpenedExternal -> Unit
                    is CommandResult.Empty -> Unit
                }
            },
            hint = stringResource(R.string.command_hint),
        )

        Spacer(Modifier.height(spacing.md))

        if (query.isNotBlank() && results.isEmpty()) {
            EmptyState(text = stringResource(R.string.no_results))
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(results, key = { it.componentKey }) { app ->
                    PageAppRow(
                        app = app,
                        showIcon = showIcons,
                        onClick = {
                            if (!viewModel.launchApp(app)) onMessage(noAppMessage)
                        },
                    )
                }
            }
        }
    }
}
