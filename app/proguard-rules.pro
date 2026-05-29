# Page launcher ProGuard / R8 rules.
# Keep the notification listener service entry point referenced from the manifest.
-keep class com.pageos.launcher.notifications.PageNotificationListener { *; }
