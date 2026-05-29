package com.pageos.launcher.ui

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pageos.launcher.assistant.IntentParser
import com.pageos.launcher.assistant.PageAction
import com.pageos.launcher.assistant.SystemSetting
import com.pageos.launcher.data.AppInfo
import com.pageos.launcher.data.AppRepository
import com.pageos.launcher.data.PagePreferences
import com.pageos.launcher.data.ThemeMode
import com.pageos.launcher.launcher.CompanionApp
import com.pageos.launcher.launcher.CompanionCatalog
import com.pageos.launcher.launcher.DefaultLauncher
import com.pageos.launcher.launcher.HomeAction
import com.pageos.launcher.launcher.LaunchOutcome
import com.pageos.launcher.launcher.LauncherIntentHandler
import com.pageos.launcher.launcher.SystemSettingsLauncher
import com.pageos.launcher.notifications.PageNotificationListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** What a parsed command resolved to, for the screen to act on. */
sealed interface CommandResult {
    data object OpenedExternal : CommandResult
    data object GoToSettings : CommandResult
    data class GoToSearch(val query: String) : CommandResult
    data object NoAppAvailable : CommandResult
    data object Empty : CommandResult
}

/**
 * Holds Page's app/preferences state and routes user intents to launches.
 *
 * Uses [AndroidViewModel] so it can own application-scoped collaborators
 * (PackageManager-backed repository, DataStore-backed preferences) without
 * leaking an Activity context.
 */
class PageViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository(application)
    private val preferences = PagePreferences(application)
    private val intentParser = IntentParser()
    private val intentHandler = LauncherIntentHandler(application, repository)

    val showAppIcons: StateFlow<Boolean> = preferences.showAppIcons
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    /** The user's chosen dark/light appearance mode. */
    val themeMode: StateFlow<ThemeMode> = preferences.themeMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeMode.DEFAULT)

    private val _apps = MutableStateFlow<List<AppInfo>>(emptyList())
    val apps: StateFlow<List<AppInfo>> = _apps.asStateFlow()

    /** Package names of companion apps currently installed. */
    private val _installedCompanions = MutableStateFlow<Set<String>>(emptySet())
    val installedCompanions: StateFlow<Set<String>> = _installedCompanions.asStateFlow()

    /**
     * The launcher's start destination, resolved once at launch:
     * the setup screen when essential companions are missing and setup hasn't
     * been dismissed, otherwise the home screen. Null until resolved.
     */
    private val _startRoute = MutableStateFlow<String?>(null)
    val startRoute: StateFlow<String?> = _startRoute.asStateFlow()

    init {
        refreshApps()
        resolveStartRoute()
    }

    /** Re-checks which companion apps are installed (e.g. after returning from an install). */
    fun refreshCompanionStatus() {
        viewModelScope.launch { _installedCompanions.value = computeInstalledCompanions() }
    }

    private fun resolveStartRoute() {
        viewModelScope.launch {
            val installed = computeInstalledCompanions()
            _installedCompanions.value = installed
            val dismissed = preferences.setupDismissed.first()
            val missingEssentials = CompanionCatalog.essential.any { it.packageName !in installed }
            _startRoute.value =
                if (!dismissed && missingEssentials) PageRoutes.SETUP else PageRoutes.HOME
        }
    }

    private suspend fun computeInstalledCompanions(): Set<String> =
        withContext(Dispatchers.IO) {
            CompanionCatalog.allPackages.filter { repository.isAppInstalled(it) }.toSet()
        }

    /** Opens the install page (F-Droid package page or GitHub releases) for a companion. */
    fun installCompanion(app: CompanionApp): Boolean =
        repository.startSafely(Intent(Intent.ACTION_VIEW, Uri.parse(app.installUrl)))

    /** Marks first-run setup as finished/skipped so it won't auto-appear again. */
    fun dismissSetup() {
        viewModelScope.launch { preferences.setSetupDismissed(true) }
    }

    fun refreshApps() {
        viewModelScope.launch {
            _apps.value = repository.loadLaunchableApps(loadIcons = showAppIcons.value)
        }
    }

    /** Filters the in-memory app list for the search screen. */
    fun filterApps(query: String): List<AppInfo> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return emptyList()
        return _apps.value.filter { it.label.contains(trimmed, ignoreCase = true) }
    }

    /** Launches one of the fixed home-screen actions. */
    fun performHomeAction(action: HomeAction): LaunchOutcome = intentHandler.handle(action)

    /** Launches an installed app by package name. */
    fun launchApp(app: AppInfo): Boolean = repository.launchComponent(app)

    /** Opens a system settings panel via a public Settings intent. */
    fun openSystemSetting(setting: SystemSetting): Boolean =
        repository.startSafely(SystemSettingsLauncher.intentFor(getApplication(), setting))

    /** Opens the system "default home app" chooser. */
    fun openDefaultLauncherSettings(): Boolean =
        repository.startSafely(SystemSettingsLauncher.defaultLauncherIntent())

    /** Whether Page is currently the device's default Home app. */
    fun isDefaultLauncher(): Boolean = DefaultLauncher.isDefault(getApplication())

    /** Opens the system "Notification access" screen for the digest stub. */
    fun openNotificationAccessSettings(): Boolean =
        repository.startSafely(PageNotificationListener.settingsIntent())

    fun setShowAppIcons(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setShowAppIcons(enabled)
            refreshApps()
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { preferences.setThemeMode(mode) }
    }

    /**
     * Parses [raw] command text and performs the deterministic action,
     * returning a [CommandResult] the screen uses for navigation/feedback.
     */
    fun runCommand(raw: String): CommandResult = when (val action = intentParser.parse(raw)) {
        is PageAction.OpenPhone -> outcomeToResult(intentHandler.handle(HomeAction.PHONE))
        is PageAction.OpenMessages -> outcomeToResult(intentHandler.handle(HomeAction.MESSAGES))
        is PageAction.OpenMaps -> outcomeToResult(intentHandler.handle(HomeAction.MAPS))
        is PageAction.OpenBooks -> outcomeToResult(intentHandler.handle(HomeAction.BOOKS))
        is PageAction.CreateNote -> outcomeToResult(intentHandler.handle(HomeAction.NOTES))
        is PageAction.OpenApp -> handleOpenApp(action.packageName)
        is PageAction.OpenSystemSetting ->
            if (openSystemSetting(action.setting)) CommandResult.OpenedExternal
            else CommandResult.NoAppAvailable
        is PageAction.SearchApps -> CommandResult.GoToSearch(action.query)
        is PageAction.Unknown -> CommandResult.Empty
    }

    private fun handleOpenApp(packageName: String): CommandResult = when (packageName) {
        IntentParser.PAGE_SETTINGS_SENTINEL -> CommandResult.GoToSettings
        IntentParser.MUSIC_SENTINEL -> outcomeToResult(intentHandler.handle(HomeAction.MUSIC))
        IntentParser.CAMERA_SENTINEL -> outcomeToResult(intentHandler.handle(HomeAction.CAMERA))
        IntentParser.CALENDAR_SENTINEL -> outcomeToResult(intentHandler.handle(HomeAction.CALENDAR))
        IntentParser.FILES_SENTINEL -> outcomeToResult(intentHandler.handle(HomeAction.FILES))
        else ->
            if (repository.launchApp(packageName)) CommandResult.OpenedExternal
            else CommandResult.NoAppAvailable
    }

    private fun outcomeToResult(outcome: LaunchOutcome): CommandResult = when (outcome) {
        LaunchOutcome.Launched -> CommandResult.OpenedExternal
        LaunchOutcome.OpenPageSettings -> CommandResult.GoToSettings
        LaunchOutcome.NoAppAvailable -> CommandResult.NoAppAvailable
    }
}
