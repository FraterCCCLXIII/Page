package com.pageos.launcher.assistant

/**
 * Deterministic, rule-based text -> [PageAction] mapper.
 *
 * This is intentionally NOT an AI model. It recognizes a small set of command
 * keywords and otherwise treats input as an app-search query. Keeping it simple
 * and predictable means the launcher never surprises the user.
 *
 * Phase 3 may replace this with an on-device intent router behind the same
 * [parse] signature; nothing in the UI needs to change.
 */
class IntentParser {

    /**
     * Resolves [input] to a [PageAction].
     *
     * Matching is case-insensitive. A leading command keyword (optionally with
     * trailing arguments) wins; e.g. "note buy milk" -> [PageAction.CreateNote].
     * Anything unrecognized becomes a [PageAction.SearchApps] over the raw text.
     */
    fun parse(input: String): PageAction {
        val raw = input.trim()
        if (raw.isEmpty()) return PageAction.Unknown(raw)

        val lower = raw.lowercase()
        val keyword = lower.substringBefore(' ')
        val remainder = raw.substringAfter(' ', missingDelimiterValue = "").trim()

        return when (keyword) {
            "call", "phone", "dial" -> PageAction.OpenPhone
            "message", "messages", "text", "sms" -> PageAction.OpenMessages
            "map", "maps", "navigate", "directions" -> PageAction.OpenMaps
            "note", "notes", "write" -> PageAction.CreateNote(remainder.ifBlank { null })
            "book", "books", "read" -> PageAction.OpenBooks
            "music", "play", "song" -> PageAction.OpenApp(MUSIC_SENTINEL)
            "camera", "photo", "picture" -> PageAction.OpenApp(CAMERA_SENTINEL)
            "calendar", "agenda", "events" -> PageAction.OpenApp(CALENDAR_SENTINEL)
            "files", "file", "documents" -> PageAction.OpenApp(FILES_SENTINEL)
            "settings", "setting" -> resolveSettings(remainder)
            "wifi", "wi-fi" -> PageAction.OpenSystemSetting(SystemSetting.WIFI)
            "bluetooth" -> PageAction.OpenSystemSetting(SystemSetting.BLUETOOTH)
            "battery" -> PageAction.OpenSystemSetting(SystemSetting.BATTERY)
            else -> PageAction.SearchApps(raw)
        }
    }

    private fun resolveSettings(remainder: String): PageAction = when (remainder.lowercase()) {
        "wifi", "wi-fi" -> PageAction.OpenSystemSetting(SystemSetting.WIFI)
        "bluetooth" -> PageAction.OpenSystemSetting(SystemSetting.BLUETOOTH)
        "notifications", "notification" -> PageAction.OpenSystemSetting(SystemSetting.NOTIFICATIONS)
        "battery" -> PageAction.OpenSystemSetting(SystemSetting.BATTERY)
        "accessibility" -> PageAction.OpenSystemSetting(SystemSetting.ACCESSIBILITY)
        // Bare "settings" opens Page's own settings; the UI interprets this.
        else -> PageAction.OpenApp(PAGE_SETTINGS_SENTINEL)
    }

    companion object {
        /**
         * Sentinel package strings the UI recognizes to route to a [HomeAction]
         * or in-app screen rather than a literal package. Using sentinels keeps
         * [PageAction.OpenApp] honest (it always carries a String) while letting
         * the parser express "the music action" without hardcoding a package.
         */
        const val PAGE_SETTINGS_SENTINEL = "page:settings"
        const val MUSIC_SENTINEL = "page:music"
        const val CAMERA_SENTINEL = "page:camera"
        const val CALENDAR_SENTINEL = "page:calendar"
        const val FILES_SENTINEL = "page:files"
    }
}
