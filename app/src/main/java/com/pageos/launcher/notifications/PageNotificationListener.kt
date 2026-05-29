package com.pageos.launcher.notifications

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.text.TextUtils
import android.util.Log

/**
 * Notification listener **stub**.
 *
 * Intentionally passive: Page does not block, dismiss, or modify notifications
 * yet. This class only establishes the binding and the data seam for a future
 * "notification digest" (Phase 2). It keeps nothing off-device.
 *
 * The user must explicitly grant access in system settings; see
 * [isEnabled] and [settingsIntent].
 */
class PageNotificationListener : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "Notification listener connected (passive digest stub).")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        // TODO(phase2): collect into an in-memory digest instead of logging.
        // Deliberately a no-op today: no blocking, no storage, no network.
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // TODO(phase2): update the digest when a notification is cleared.
    }

    companion object {
        private const val TAG = "PageNotifications"

        /**
         * Whether the user has granted Page notification-listener access.
         * Reads the system's enabled-listeners setting; requires no permission.
         */
        fun isEnabled(context: Context): Boolean {
            val flat = Settings.Secure.getString(
                context.contentResolver,
                "enabled_notification_listeners",
            ) ?: return false
            if (TextUtils.isEmpty(flat)) return false

            val expected = ComponentName(context, PageNotificationListener::class.java)
            return flat.split(":").any { entry ->
                ComponentName.unflattenFromString(entry) == expected
            }
        }

        /** Intent that opens the system "Notification access" settings screen. */
        fun settingsIntent(): Intent =
            Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
}
