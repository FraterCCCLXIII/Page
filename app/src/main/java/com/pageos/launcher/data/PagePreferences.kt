package com.pageos.launcher.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

/** Single DataStore for all Page settings. Local-only, no cloud sync. */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "page_settings")

/**
 * Typed, reactive accessor for Page's local preferences.
 *
 * Everything here is stored on-device via Jetpack DataStore. There is no
 * account, no sync, and no telemetry.
 */
class PagePreferences(private val context: Context) {

    /** Whether to render app icons. Page is text-first, so this is off by default. */
    val showAppIcons: Flow<Boolean> = context.dataStore.data
        .safe()
        .map { it[Keys.SHOW_APP_ICONS] ?: DEFAULT_SHOW_ICONS }

    /** Package names the user has chosen to hide from lists/search (Phase 2). */
    val hiddenApps: Flow<Set<String>> = context.dataStore.data
        .safe()
        .map { it[Keys.HIDDEN_APPS] ?: emptySet() }

    /** Whether focus mode is engaged (Phase 2 placeholder). */
    val focusMode: Flow<Boolean> = context.dataStore.data
        .safe()
        .map { it[Keys.FOCUS_MODE] ?: false }

    suspend fun setShowAppIcons(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SHOW_APP_ICONS] = enabled }
    }

    suspend fun setFocusMode(enabled: Boolean) {
        context.dataStore.edit { it[Keys.FOCUS_MODE] = enabled }
    }

    suspend fun setHiddenApps(packages: Set<String>) {
        context.dataStore.edit { it[Keys.HIDDEN_APPS] = packages }
    }

    /** Swallow read errors (e.g. corrupt file) by emitting empty preferences. */
    private fun Flow<Preferences>.safe(): Flow<Preferences> = catch { throwable ->
        if (throwable is IOException) emit(emptyPreferences()) else throw throwable
    }

    private object Keys {
        val SHOW_APP_ICONS = booleanPreferencesKey("show_app_icons")
        val HIDDEN_APPS = stringSetPreferencesKey("hidden_apps")
        val FOCUS_MODE = booleanPreferencesKey("focus_mode")
    }

    private companion object {
        const val DEFAULT_SHOW_ICONS = false
    }
}
