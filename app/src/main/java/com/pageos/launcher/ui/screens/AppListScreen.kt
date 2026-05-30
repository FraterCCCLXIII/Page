package com.pageos.launcher.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pageos.launcher.R
import com.pageos.launcher.data.AppInfo
import com.pageos.launcher.data.AppLayout
import com.pageos.launcher.ui.PageViewModel
import com.pageos.launcher.ui.components.PageAppRow
import com.pageos.launcher.ui.components.PageAppTile
import com.pageos.launcher.ui.components.PageScreenHeader
import com.pageos.launcher.ui.components.PageText
import com.pageos.launcher.ui.components.PageTextRole
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
    val layout by viewModel.appLayout.collectAsStateWithLifecycle()
    val noAppMessage = stringResource(R.string.no_app_installed)

    val launch: (AppInfo) -> Unit = { app ->
        if (!viewModel.launchApp(app)) onMessage(noAppMessage)
    }

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
        } else when (layout) {
            AppLayout.LIST -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(apps, key = { it.componentKey }) { app ->
                    PageAppRow(app = app, showIcon = showIcons, onClick = { launch(app) })
                }
            }

            AppLayout.TILES -> LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 104.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = spacing.xxl),
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                verticalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                gridItems(apps, key = { it.componentKey }) { app ->
                    PageAppTile(app = app, showIcon = showIcons, onClick = { launch(app) })
                }
            }
        }
    }
}

@Composable
internal fun EmptyState(text: String) {
    PageText(
        text = text,
        role = PageTextRole.BodySecondary,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = PageTheme.spacing.xl),
    )
}
