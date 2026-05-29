package com.pageos.launcher.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pageos.launcher.ui.screens.AppListScreen
import com.pageos.launcher.ui.screens.HomeScreen
import com.pageos.launcher.ui.screens.SearchScreen
import com.pageos.launcher.ui.screens.SettingsScreen
import com.pageos.launcher.ui.theme.PageTheme
import kotlinx.coroutines.launch

/** Navigation routes for the launcher. */
object PageRoutes {
    const val HOME = "home"
    const val APPS = "apps"
    const val SETTINGS = "settings"
    const val SEARCH = "search"
    const val SEARCH_ARG_QUERY = "query"
    const val SEARCH_WITH_ARG = "$SEARCH?$SEARCH_ARG_QUERY={$SEARCH_ARG_QUERY}"

    fun search(query: String = ""): String = "$SEARCH?$SEARCH_ARG_QUERY=$query"
}

/**
 * The launcher UI root, shared by both [com.pageos.launcher.PageLauncherActivity]
 * and [com.pageos.launcher.MainActivity] so there is a single source of truth.
 */
@Composable
fun PageApp(viewModel: PageViewModel = viewModel()) {
    PageTheme(darkTheme = true) {
        val navController = rememberNavController()
        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()
        val showMessage: (String) -> Unit = { message ->
            scope.launch { snackbarHostState.showSnackbar(message) }
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = PageRoutes.HOME,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                composable(PageRoutes.HOME) {
                    HomeScreen(
                        viewModel = viewModel,
                        onOpenApps = { navController.navigate(PageRoutes.APPS) },
                        onOpenSettings = { navController.navigate(PageRoutes.SETTINGS) },
                        onOpenSearch = { query -> navController.navigate(PageRoutes.search(query)) },
                        onMessage = showMessage,
                    )
                }
                composable(PageRoutes.APPS) {
                    AppListScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() },
                        onMessage = showMessage,
                    )
                }
                composable(
                    route = PageRoutes.SEARCH_WITH_ARG,
                    arguments = listOf(
                        navArgument(PageRoutes.SEARCH_ARG_QUERY) {
                            type = NavType.StringType
                            defaultValue = ""
                        },
                    ),
                ) { backStackEntry ->
                    val initialQuery =
                        backStackEntry.arguments?.getString(PageRoutes.SEARCH_ARG_QUERY).orEmpty()
                    SearchScreen(
                        viewModel = viewModel,
                        initialQuery = initialQuery,
                        onBack = { navController.popBackStack() },
                        onOpenSettings = {
                            navController.navigate(PageRoutes.SETTINGS)
                        },
                        onMessage = showMessage,
                    )
                }
                composable(PageRoutes.SETTINGS) {
                    SettingsScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() },
                        onMessage = showMessage,
                    )
                }
            }
        }
    }
}
