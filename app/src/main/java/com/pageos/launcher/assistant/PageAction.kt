package com.pageos.launcher.assistant

import com.pageos.launcher.launcher.HomeAction

/**
 * A resolved intent of what the user wants, produced by [IntentParser].
 *
 * This is deliberately a small, deterministic model. It is the seam where a
 * future on-device intent router (Phase 3) can plug in without changing the UI:
 * the parser changes, the action set stays the same.
 */
sealed interface PageAction {

    /** Open a specific installed app by package name. */
    data class OpenApp(val packageName: String) : PageAction

    /** Open one of Android's system settings panels. */
    data class OpenSystemSetting(val setting: SystemSetting) : PageAction

    /** Search installed apps for [query]. */
    data class SearchApps(val query: String) : PageAction

    /** Start a new note (optionally pre-filled with [text]). */
    data class CreateNote(val text: String? = null) : PageAction

    data object OpenMaps : PageAction
    data object OpenBooks : PageAction
    data object OpenMessages : PageAction
    data object OpenPhone : PageAction

    /** No deterministic match; the UI should fall back to app search. */
    data class Unknown(val rawQuery: String) : PageAction
}

/** System settings panels Page can deep-link to via standard Settings intents. */
enum class SystemSetting {
    WIFI,
    BLUETOOTH,
    NOTIFICATIONS,
    BATTERY,
    ACCESSIBILITY,
    APP_DETAILS,
}

/** Maps a [PageAction] back onto a fixed [HomeAction] where one exists. */
fun PageAction.toHomeAction(): HomeAction? = when (this) {
    is PageAction.OpenPhone -> HomeAction.PHONE
    is PageAction.OpenMessages -> HomeAction.MESSAGES
    is PageAction.OpenMaps -> HomeAction.MAPS
    is PageAction.OpenBooks -> HomeAction.BOOKS
    is PageAction.CreateNote -> HomeAction.NOTES
    else -> null
}
