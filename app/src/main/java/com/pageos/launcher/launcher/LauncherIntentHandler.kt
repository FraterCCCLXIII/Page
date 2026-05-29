package com.pageos.launcher.launcher

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import com.pageos.launcher.data.AppRepository

/** The fixed set of home-screen shortcuts. */
enum class HomeAction {
    PHONE,
    MESSAGES,
    MAPS,
    BOOKS,
    NOTES,
    MUSIC,
    CAMERA,
    CALENDAR,
    FILES,
    SETTINGS,
}

/** Result of attempting to handle a [HomeAction]. */
sealed interface LaunchOutcome {
    /** An external app/activity was started. */
    data object Launched : LaunchOutcome

    /** Caller should navigate to Page's own in-app settings screen. */
    data object OpenPageSettings : LaunchOutcome

    /** Nothing on the device can handle this action. */
    data object NoAppAvailable : LaunchOutcome
}

/**
 * Translates Page's home-screen shortcuts into concrete launches.
 *
 * Strategy for every external action:
 *   1. Try the user's preferred companion package(s) from [DefaultApps].
 *   2. Fall back to a generic Android intent action so stock apps work too.
 *   3. If nothing handles it, report [LaunchOutcome.NoAppAvailable] so the UI
 *      can show "No app installed for this action."
 *
 * No permissions are required: every intent here is a standard, user-visible
 * action (dial, view geo, capture image, etc.).
 */
class LauncherIntentHandler(
    private val context: Context,
    private val repository: AppRepository,
) {

    fun handle(action: HomeAction): LaunchOutcome = when (action) {
        HomeAction.PHONE -> launchIntent(Intent(Intent.ACTION_DIAL))
        HomeAction.MESSAGES -> openMessages()
        HomeAction.MAPS -> openWithFallback(DefaultApps.maps, geoIntent())
        HomeAction.BOOKS -> openPreferredOnly(DefaultApps.books)
        HomeAction.NOTES -> openPreferredOnly(DefaultApps.notes)
        HomeAction.MUSIC -> openWithFallback(DefaultApps.music, appCategory(Intent.CATEGORY_APP_MUSIC))
        HomeAction.CAMERA -> openWithFallback(DefaultApps.camera, cameraIntent())
        HomeAction.CALENDAR -> openWithFallback(DefaultApps.calendar, appCategory(Intent.CATEGORY_APP_CALENDAR))
        HomeAction.FILES -> openPreferredOnly(DefaultApps.files)
        HomeAction.SETTINGS -> LaunchOutcome.OpenPageSettings
    }

    /** Launches the first installed package in [packages], else falls back. */
    private fun openWithFallback(packages: List<String>, fallback: Intent?): LaunchOutcome {
        if (launchFirstInstalled(packages)) return LaunchOutcome.Launched
        return if (fallback != null) launchIntent(fallback) else LaunchOutcome.NoAppAvailable
    }

    /** Launches the first installed preferred package, else reports none. */
    private fun openPreferredOnly(packages: List<String>): LaunchOutcome =
        if (launchFirstInstalled(packages)) LaunchOutcome.Launched else LaunchOutcome.NoAppAvailable

    private fun openMessages(): LaunchOutcome {
        if (launchFirstInstalled(DefaultApps.messages)) return LaunchOutcome.Launched
        // Default SMS app via the platform "messaging" category.
        val selector = Intent.makeMainSelectorActivity(
            Intent.ACTION_MAIN,
            Intent.CATEGORY_APP_MESSAGING,
        )
        return launchIntent(selector)
    }

    private fun launchFirstInstalled(packages: List<String>): Boolean =
        packages.any { repository.launchApp(it) }

    private fun launchIntent(intent: Intent): LaunchOutcome =
        if (repository.startSafely(intent)) LaunchOutcome.Launched else LaunchOutcome.NoAppAvailable

    private fun geoIntent(): Intent =
        Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0"))

    private fun cameraIntent(): Intent =
        Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)

    private fun appCategory(category: String): Intent =
        Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, category)
}
