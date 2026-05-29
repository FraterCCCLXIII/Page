package com.pageos.launcher.launcher

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.pageos.launcher.assistant.SystemSetting

/**
 * Opens Android's own settings panels via public Settings intents.
 *
 * Page never uses privileged or hidden APIs to toggle system state; it simply
 * hands the user to the appropriate system screen.
 */
object SystemSettingsLauncher {

    fun intentFor(context: Context, setting: SystemSetting): Intent {
        val action = when (setting) {
            SystemSetting.WIFI -> Settings.ACTION_WIFI_SETTINGS
            SystemSetting.BLUETOOTH -> Settings.ACTION_BLUETOOTH_SETTINGS
            // No stable public constant for the global notification screen;
            // this is the documented action string and is guarded by startSafely.
            SystemSetting.NOTIFICATIONS -> "android.settings.NOTIFICATION_SETTINGS"
            SystemSetting.BATTERY -> Settings.ACTION_BATTERY_SAVER_SETTINGS
            SystemSetting.ACCESSIBILITY -> Settings.ACTION_ACCESSIBILITY_SETTINGS
            SystemSetting.APP_DETAILS -> Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        }
        return Intent(action).apply {
            if (setting == SystemSetting.APP_DETAILS) {
                data = Uri.fromParts("package", context.packageName, null)
            }
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    /** Opens the device's default launcher chooser so Page can be selected. */
    fun defaultLauncherIntent(): Intent =
        Intent(Settings.ACTION_HOME_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
}
