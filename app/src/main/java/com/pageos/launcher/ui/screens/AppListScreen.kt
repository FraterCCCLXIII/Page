package com.pageos.launcher.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pageos.launcher.R
import com.pageos.launcher.ui.PageViewModel
import com.pageos.launcher.ui.components.PageAppRow
import com.pageos.launcher.ui.components.PageScreenHeader
import com.pageos.launcher.ui.theme.PageTheme

@Composable
fun AppListScreen(
    viewModel: PageViewModel,
    onBack: () -> Unit,
    onMessage: (String) -> Unit,
) {
    val spacing = PageTheme.spacing
    val apps by viewModel.apps.collectAsStateWithLifecycle()
    val showIcons by viewModel.showAppIcons.collectAsStateWithLifecycle()
    val noAppMessage = stringResource(R.string.no_app_installed)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = spacing.screenPadding),
    ) {
        PageScreenHeader(
            title = stringResource(R.string.all_apps),
            onBack = onBack,
        )

        if (apps.isEmpty()) {
            EmptyState(text = stringResource(R.string.no_results))
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(apps, key = { it.componentKey }) { app ->
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

@Composable
internal fun EmptyState(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = PageTheme.spacing.xl),
    )
}
