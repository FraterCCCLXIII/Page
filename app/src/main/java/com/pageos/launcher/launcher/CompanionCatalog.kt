package com.pageos.launcher.launcher

/** Where a companion app is installed from (no Google Play / Play Services). */
enum class CompanionSource(val displayName: String) {
    FDROID("F-Droid"),
    GITHUB("GitHub"),
}

/**
 * A recommended privacy-respecting companion app, with everything the first-run
 * setup needs: the package to check for, what it's for, and where to install it.
 */
data class CompanionApp(
    val packageName: String,
    val name: String,
    /** Short purpose label, e.g. "Maps", "Notes". */
    val purpose: String,
    val source: CompanionSource,
    /** A public, http(s) install page (F-Droid package page or GitHub releases). */
    val installUrl: String,
    /**
     * Essential companions complete Page's core actions and drive whether the
     * first-run setup appears. Non-essential ones are simply recommended.
     */
    val essential: Boolean,
)

/**
 * The curated set of companion apps Page integrates with. Package names mirror
 * [DefaultApps]; install URLs point at official open-source sources.
 *
 * F-Droid package pages are intercepted by the F-Droid app when installed and
 * otherwise open in a browser; GitHub links go to the latest release.
 */
object CompanionCatalog {

    private const val FDROID = "https://f-droid.org/packages/"

    val all: List<CompanionApp> = listOf(
        // --- Essential: complete Page's primary actions --------------------
        CompanionApp(
            packageName = DefaultApps.ORGANIC_MAPS,
            name = "Organic Maps",
            purpose = "Maps",
            source = CompanionSource.FDROID,
            installUrl = FDROID + DefaultApps.ORGANIC_MAPS,
            essential = true,
        ),
        CompanionApp(
            packageName = DefaultApps.MARKOR,
            name = "Markor",
            purpose = "Notes",
            source = CompanionSource.FDROID,
            installUrl = FDROID + DefaultApps.MARKOR,
            essential = true,
        ),
        CompanionApp(
            packageName = DefaultApps.AUXIO,
            name = "Auxio",
            purpose = "Music",
            source = CompanionSource.FDROID,
            installUrl = FDROID + DefaultApps.AUXIO,
            essential = true,
        ),
        CompanionApp(
            packageName = DefaultApps.OPEN_CAMERA,
            name = "Open Camera",
            purpose = "Camera",
            source = CompanionSource.FDROID,
            installUrl = FDROID + DefaultApps.OPEN_CAMERA,
            essential = true,
        ),
        CompanionApp(
            packageName = DefaultApps.ETAR,
            name = "Etar",
            purpose = "Calendar",
            source = CompanionSource.FDROID,
            installUrl = FDROID + DefaultApps.ETAR,
            essential = true,
        ),
        CompanionApp(
            packageName = DefaultApps.MATERIAL_FILES,
            name = "Material Files",
            purpose = "Files",
            source = CompanionSource.FDROID,
            installUrl = FDROID + DefaultApps.MATERIAL_FILES,
            essential = true,
        ),
        CompanionApp(
            packageName = DefaultApps.KOREADER,
            name = "KOReader",
            purpose = "Books",
            source = CompanionSource.GITHUB,
            installUrl = "https://github.com/koreader/koreader/releases/latest",
            essential = true,
        ),
        CompanionApp(
            packageName = DefaultApps.QUIK_SMS,
            name = "QUIK SMS",
            purpose = "Messages",
            source = CompanionSource.GITHUB,
            installUrl = "https://github.com/quik-sms/quik/releases/latest",
            essential = true,
        ),

        // --- Recommended: optional upgrades --------------------------------
        CompanionApp(
            packageName = DefaultApps.READEST,
            name = "Readest",
            purpose = "Books (alt)",
            source = CompanionSource.GITHUB,
            installUrl = "https://github.com/readest/readest/releases/latest",
            essential = false,
        ),
        CompanionApp(
            // F-Droid publishes the libre build under this id.
            packageName = "com.kunzisoft.keepass.libre",
            name = "KeePassDX",
            purpose = "Passwords",
            source = CompanionSource.FDROID,
            installUrl = FDROID + "com.kunzisoft.keepass.libre",
            essential = false,
        ),
        CompanionApp(
            packageName = "com.beemdevelopment.aegis",
            name = "Aegis",
            purpose = "2FA",
            source = CompanionSource.FDROID,
            installUrl = FDROID + "com.beemdevelopment.aegis",
            essential = false,
        ),
        CompanionApp(
            packageName = "net.thunderbird.android",
            name = "Thunderbird",
            purpose = "Email",
            source = CompanionSource.FDROID,
            installUrl = FDROID + "net.thunderbird.android",
            essential = false,
        ),
    )

    val essential: List<CompanionApp> = all.filter { it.essential }
    val recommended: List<CompanionApp> = all.filterNot { it.essential }

    /** Distinct packages we check for "installed" status. */
    val allPackages: List<String> = all.map { it.packageName }.distinct()
}
