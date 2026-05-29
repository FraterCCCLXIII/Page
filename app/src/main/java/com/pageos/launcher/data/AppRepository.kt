package com.pageos.launcher.data

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.Collator

/**
 * Reads launchable apps from [PackageManager] and launches components/intents.
 *
 * This class holds no AI, no network, and no analytics. It is a thin, testable
 * wrapper around the platform package manager. Icon drawables are only resolved
 * when [loadIcons] is requested, keeping the default text-first path cheap.
 */
class AppRepository(private val context: Context) {

    private val packageManager: PackageManager get() = context.packageManager

    /**
     * Returns every launchable app, sorted alphabetically by label.
     *
     * @param loadIcons when true, each [AppInfo] carries its launcher icon.
     */
    suspend fun loadLaunchableApps(loadIcons: Boolean = false): List<AppInfo> =
        withContext(Dispatchers.IO) {
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolved = queryIntentActivitiesCompat(mainIntent)
            val ownPackage = context.packageName

            resolved
                .asSequence()
                .mapNotNull { info -> info.toAppInfo(loadIcons) }
                // Hide Page itself from its own app list.
                .filter { it.packageName != ownPackage }
                .sortedWith(compareBy(labelCollator) { it.label })
                .toList()
        }

    /**
     * Case-insensitive prefix/substring search over launchable app labels.
     */
    suspend fun searchApps(query: String, loadIcons: Boolean = false): List<AppInfo> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return emptyList()
        return loadLaunchableApps(loadIcons).filter { app ->
            app.label.contains(trimmed, ignoreCase = true)
        }
    }

    /** Returns true if the given package is installed and launchable. */
    fun isAppInstalled(packageName: String): Boolean =
        getLaunchIntent(packageName) != null

    /** Resolves the launch intent for [packageName], or null if unavailable. */
    fun getLaunchIntent(packageName: String): Intent? =
        packageManager.getLaunchIntentForPackage(packageName)

    /**
     * Launches a specific app by package name.
     *
     * @return true if an activity was started, false if no launch intent exists.
     */
    fun launchApp(packageName: String): Boolean {
        val intent = getLaunchIntent(packageName) ?: return false
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return startSafely(intent)
    }

    /** Launches a specific component (preferred for [AppInfo] rows). */
    fun launchComponent(app: AppInfo): Boolean {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            component = ComponentName(app.packageName, app.activityName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return startSafely(intent)
    }

    /**
     * Starts an arbitrary intent, guarding against missing handlers.
     *
     * @return true if started, false if no activity could handle the intent.
     */
    fun startSafely(intent: Intent): Boolean {
        return try {
            if (intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK == 0) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.w(TAG, "No activity could handle intent: ${intent.action}", e)
            false
        }
    }

    private fun ResolveInfo.toAppInfo(loadIcons: Boolean): AppInfo? {
        val activity = activityInfo ?: return null
        val label = loadLabel(packageManager).toString().ifBlank { activity.packageName }
        return AppInfo(
            label = label,
            packageName = activity.packageName,
            activityName = activity.name,
            icon = if (loadIcons) runCatching { loadIcon(packageManager) }.getOrNull() else null,
        )
    }

    @Suppress("DEPRECATION")
    private fun queryIntentActivitiesCompat(intent: Intent): List<ResolveInfo> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(
                intent,
                PackageManager.ResolveInfoFlags.of(0L),
            )
        } else {
            packageManager.queryIntentActivities(intent, 0)
        }

    private companion object {
        const val TAG = "AppRepository"
        val labelCollator: Collator = Collator.getInstance().apply {
            strength = Collator.PRIMARY
        }
    }
}
