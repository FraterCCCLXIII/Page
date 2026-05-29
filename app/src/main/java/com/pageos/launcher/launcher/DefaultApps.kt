package com.pageos.launcher.launcher

/**
 * Known package names for Page's recommended privacy-respecting companion apps.
 *
 * These are *preferences*, not requirements. [LauncherIntentHandler] tries the
 * preferred package first and falls back to a generic Android intent action so
 * Page works even when none of these are installed.
 *
 * TODO(verify): confirm package names marked PLACEHOLDER against F-Droid / the
 * Play Store before relying on them. Some apps publish under different IDs for
 * F-Droid vs. Play builds.
 */
object DefaultApps {

    // --- Verified, widely published FOSS packages ---------------------------
    const val ORGANIC_MAPS = "app.organicmaps"
    const val MARKOR = "net.gsantner.markor"
    const val AUXIO = "org.oxycblt.auxio"
    const val OPEN_CAMERA = "net.sourceforge.opencamera"
    const val ETAR = "ws.xsoh.etar"
    const val MATERIAL_FILES = "me.zhanghai.android.files"
    const val KOREADER = "org.koreader.launcher"

    // --- Needs verification -------------------------------------------------
    // TODO(verify): Readest Android package id not confirmed yet.
    const val READEST = "com.readest.app" // PLACEHOLDER
    // TODO(verify): QUIK SMS package id not confirmed yet.
    const val QUIK_SMS = "org.quik.sms" // PLACEHOLDER

    /**
     * Ordered preference lists per action. The handler launches the first
     * installed package; if none are installed it falls back to a system intent.
     */
    val maps = listOf(ORGANIC_MAPS)
    val books = listOf(KOREADER, READEST)
    val notes = listOf(MARKOR)
    val music = listOf(AUXIO)
    val camera = listOf(OPEN_CAMERA)
    val calendar = listOf(ETAR)
    val files = listOf(MATERIAL_FILES)
    val messages = listOf(QUIK_SMS)
}
