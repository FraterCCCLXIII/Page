package com.pageos.launcher.launcher

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings

/**
 * Helpers for becoming the device's default Home (launcher) app.
 *
 * Android never lets an app set itself as Home silently — the user must
 * confirm. We use the most direct consent flow available:
 *
 *  1. [requestIntent]: on API 29+ this is the system **ROLE_HOME** request,
 *     a one-tap "make Page your Home app?" dialog. If the role isn't
 *     requestable (some OEMs) or is already held, it falls back to the
 *     system Home-app settings screen.
 *  2. [isDefault]: reports whether Page currently holds the Home role.
 *
 * Launch [requestIntent] with an Activity Result launcher so the UI can
 * re-check [isDefault] when the user returns.
 */
object DefaultLauncher {

    /** True if Page is currently the device's default Home app. */
    fun isDefault(context: Context): Boolean {
        roleManager(context)?.let { rm ->
            if (rm.isRoleAvailable(RoleManager.ROLE_HOME)) {
                return rm.isRoleHeld(RoleManager.ROLE_HOME)
            }
        }
        // Fallback: resolve the HOME intent and compare the default handler.
        val home = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val resolved = context.packageManager
            .resolveActivity(home, PackageManager.MATCH_DEFAULT_ONLY)
        return resolved?.activityInfo?.packageName == context.packageName
    }

    /**
     * The best intent to prompt the user to make Page the default Home app.
     *
     * Prefers the ROLE_HOME request dialog; otherwise the Home-app settings
     * screen. Always launch via an Activity context / result launcher.
     */
    fun requestIntent(context: Context): Intent {
        roleManager(context)?.let { rm ->
            if (rm.isRoleAvailable(RoleManager.ROLE_HOME) &&
                !rm.isRoleHeld(RoleManager.ROLE_HOME)
            ) {
                return rm.createRequestRoleIntent(RoleManager.ROLE_HOME)
            }
        }
        return Intent(Settings.ACTION_HOME_SETTINGS)
    }

    private fun roleManager(context: Context): RoleManager? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.getSystemService(RoleManager::class.java)
        } else {
            null
        }
}
