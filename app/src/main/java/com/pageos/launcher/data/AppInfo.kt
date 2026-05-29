package com.pageos.launcher.data

import android.graphics.drawable.Drawable

/**
 * A launchable application installed on the device.
 *
 * [icon] is loaded lazily/optionally because Page is text-first by default and
 * only resolves icons when the user opts into showing them.
 */
data class AppInfo(
    val label: String,
    val packageName: String,
    val activityName: String,
    val icon: Drawable? = null,
) {
    /** Stable identity for a launchable component. */
    val componentKey: String get() = "$packageName/$activityName"
}
