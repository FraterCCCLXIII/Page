package com.pageos.launcher.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pageos.launcher.assistant.IntentParser
import com.pageos.launcher.assistant.PageAction
import com.pageos.launcher.assistant.SystemSetting
import com.pageos.launcher.data.AppInfo
import com.pageos.launcher.data.AppRepository
import com.pageos.launcher.data.PagePreferences
import com.pageos.launcher.launcher.HomeAction
import com.pageos.launcher.launcher.LaunchOutcome
import com.pageos.launcher.launcher.LauncherIntentHandler
import com.pageos.launcher.launcher.SystemSettingsLauncher
import com.pageos.launcher.notifications.PageNotificationListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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

    private val _apps = MutableStateFlow<List<AppInfo>>(emptyList())
    val apps: StateFlow<List<AppInfo>> = _apps.asStateFlow()

    init {
        refreshApps()
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

    /** Opens the system "Notification access" screen for the digest stub. */
    fun openNotificationAccessSettings(): Boolean =
        repository.startSafely(PageNotificationListener.settingsIntent())

    fun setShowAppIcons(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setShowAppIcons(enabled)
            refreshApps()
        }
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
