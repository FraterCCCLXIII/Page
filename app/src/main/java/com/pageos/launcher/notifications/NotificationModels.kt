package com.pageos.launcher.notifications

/**
 * A single notification reduced to the minimum Page cares about.
 *
 * Page never stores notification bodies off-device and never transmits them.
 * These models exist so a future "digest" can summarize what is waiting without
 * the constant interruption of individual pop-ups.
 */
data class NotificationItem(
    val key: String,
    val packageName: String,
    val appLabel: String,
    val title: String?,
    val summary: String?,
    val postedAtMillis: Long,
)

/**
 * A grouped, count-first summary of pending notifications.
 *
 * Phase 2 will build this from the live notification stream; for now it is a
 * data shape that the UI and tests can rely on.
 */
data class NotificationDigest(
    val items: List<NotificationItem> = emptyList(),
    val generatedAtMillis: Long = 0L,
) {
    val totalCount: Int get() = items.size

    /** App label -> number of pending notifications, for a calm overview. */
    val countsByApp: Map<String, Int>
        get() = items.groupingBy { it.appLabel }.eachCount()

    val isEmpty: Boolean get() = items.isEmpty()

    companion object {
        val EMPTY = NotificationDigest()
    }
}
