# Page

**A calm, private, text-first Android launcher.**

Page replaces the Android home screen with a quiet, intentional interface built
around actions instead of grids of icons. It is the first step toward a
Light Phone–style Android experience that you assemble from open-source apps you
already trust — no custom ROM required.

> Status: early MVP (v0.1.0). Launcher, app search, default actions, and
> settings shortcuts work today. See the [roadmap](#roadmap) for what's next.

---

## What Page is

- **A launcher, not a ROM.** Install it like any app and set it as your default
  home screen. Nothing is rooted, flashed, or replaced.
- **Text-first.** Large, readable type. No app icons by default (you can turn
  them on in settings). Calm spacing, monochrome dark theme, no clutter.
- **Privacy-first.** No cloud, no analytics, no tracking, no ads, and **no
  Google Play Services dependency**. Page requests no runtime or network
  permissions; it is fully offline.
- **Intentional.** The home screen asks one question — *What do you need?* — and
  offers a short list of primary actions plus a single search/command field.

## Privacy guarantees

Page is built so the code can be audited quickly:

- **Zero network permission.** There is no `INTERNET` permission in the manifest.
- **No telemetry / no ads / no Play Services.** None are present as
  dependencies.
- **Narrow package visibility.** Instead of `QUERY_ALL_PACKAGES`, Page declares a
  small `<queries>` block to enumerate launchable apps and resolve the handful of
  intents it uses.
- **Local-only settings.** Preferences live on-device via Jetpack DataStore.
- **Optional, passive notification access.** The notification listener is a stub
  that does nothing intrusive; you must explicitly grant it in system settings.

---

## MVP scope

1. Android launcher app (selectable as the default home screen)
2. Text-first home screen
3. Minimal, alphabetical app list
4. Search / command bar (app search + simple command keywords)
5. Basic settings screen
6. Notification digest **concept stub** (passive listener + data models)
7. Primary actions that open your chosen companion apps, with safe fallbacks

### Primary actions

`Phone` · `Messages` · `Maps` · `Books` · `Notes` · `Music` · `Camera` ·
`Calendar` · `Files` · `Settings`

Each action prefers a privacy-respecting companion app (configurable defaults in
`launcher/DefaultApps.kt`) and falls back to a generic Android intent so it works
even on a stock device. If nothing can handle an action, Page shows a clean
message: *"No app installed for this action."*

---

## Build

### Requirements

- Android Studio (Ladybug or newer) with a bundled JDK 17+ (JDK 21 recommended)
- Android SDK with API 35 installed
- Min SDK: **Android 10 (API 29)** · Target SDK: **API 35**

### From Android Studio

1. Open the project folder.
2. Let Gradle sync (it uses the version catalog in `gradle/libs.versions.toml`).
3. Run the `app` configuration on a device or emulator.

### From the command line

```bash
# Build a debug APK
./gradlew :app:assembleDebug

# Install on a connected device/emulator
./gradlew :app:installDebug
```

The APK lands in `app/build/outputs/apk/debug/`.

> The debug build uses the application id `com.pageos.launcher.debug` so it can
> coexist with a release install.

---

## Installing the "bundle" (Page + companion apps)

A key thing to understand: **Android does not let a launcher silently install
other apps.** There is no APK that contains other installable apps, and a normal
app can't push packages onto the system. So a "bundle" is achieved one of three
ways, depending on how turnkey you need it:

### 1. Side-load everything onto your own device (works today)

Use the provisioning script to install Page plus every companion from their
official open-source sources (F-Droid + GitHub releases) over ADB:

```bash
# Connect a device/emulator (adb on PATH), then:
./scripts/install-bundle.sh                 # Page + all companions
./scripts/install-bundle.sh --no-launcher   # companions only
./scripts/install-bundle.sh --launcher-only # just build + install Page
SERIAL=emulator-5554 ./scripts/install-bundle.sh   # target a specific device
```

This is ideal for setting up your own phone, demos, or CI/emulators. It needs
`adb`, `curl`, `python3`, and `gh`. No Google Play / Play Services involved.

### 2. In-app first-run setup (the product approach — planned)

The right end-user experience is an onboarding screen that detects which
companions are missing and deep-links you to install each one (via F-Droid).
This keeps Page a single, honest install while still guiding you to a complete
setup. Tracked on the [roadmap](#roadmap) (Phase 2).

### 3. Turnkey / zero-touch (Phase 4)

For a true preinstalled bundle on a fresh device you need elevated provisioning:

- **Device-owner provisioning** (QR / NFC / `dpm`): a managed-setup flow can
  auto-install a defined app set. This is the "Light Phone-style" turnkey route.
- **Your own F-Droid repo**: publish Page + a curated app list so users add one
  repo and install the set from a trusted source.
- **ROM-level preinstall** (e.g. a LineageOS addon): out of scope for the
  launcher, but possible later.

## Set Page as your default launcher

1. Install and open Page once.
2. Open **Settings → Page → Set Page as default launcher** (or Android
   **Settings → Apps → Default apps → Home app**).
3. Choose **Page**.

To go back to your previous launcher, repeat and pick the other launcher.

---

## Recommended companion apps

Page is a hub for focused, open-source apps. None are bundled; install the ones
you want (F-Droid is a great source):

| Action     | App                       | Package (verify before relying on it)     |
| ---------- | ------------------------- | ----------------------------------------- |
| Messages   | QUIK SMS                  | `dev.octoshrimpy.quik`                     |
| Maps       | Organic Maps              | `app.organicmaps`                         |
| Books      | KOReader / Readest        | `org.koreader.launcher` / `com.bilingify.readest` |
| Notes      | Markor                    | `net.gsantner.markor`                     |
| Music      | Auxio                     | `org.oxycblt.auxio`                       |
| Camera     | Open Camera               | `net.sourceforge.opencamera`              |
| Calendar   | Etar                      | `ws.xsoh.etar`                            |
| Files      | Material Files            | `me.zhanghai.android.files`               |
| Passwords  | KeePassDX                 | —                                         |
| 2FA        | Aegis Authenticator       | —                                         |
| Email      | Thunderbird for Android   | —                                         |

> Open Camera is published on SourceForge, not GitHub, so it is not in the
> companion forks list. Page still opens it via `net.sourceforge.opencamera`
> (or any camera app as a fallback).

Forked upstreams of these companion apps live under the same account for easy
contribution and pinning.

---

## Project structure

```
page/
├── app/
│   └── src/main/
│       ├── AndroidManifest.xml          # launcher intent filters, queries, no perms
│       ├── java/com/pageos/launcher/
│       │   ├── MainActivity.kt           # thin entry → PageLauncherActivity
│       │   ├── PageLauncherActivity.kt   # HOME activity, hosts the UI
│       │   ├── data/                     # AppInfo, AppRepository, PagePreferences
│       │   ├── launcher/                 # DefaultApps, intent + system-settings handlers
│       │   ├── assistant/                # PageAction + deterministic IntentParser
│       │   ├── notifications/            # passive listener stub + digest models
│       │   └── ui/                       # theme, components, screens, ViewModel
│       └── res/                          # monochrome theme, strings, adaptive icon
├── gradle/libs.versions.toml             # version catalog
├── settings.gradle.kts
└── build.gradle.kts
```

### Architecture notes

- **Deterministic assistant seam.** `IntentParser` maps typed text to a
  `PageAction`. It is intentionally rule-based (not AI). Phase 3 can swap in an
  on-device intent router behind the same signature without touching the UI.
- **Single UI source of truth.** Both activities render the shared `PageApp`
  composable, so there is no duplicated screen logic.
- **Themeable by tokens.** Colors, type, and spacing live in `ui/theme` as tokens
  (light tokens are already defined for a future light mode).

---

## Roadmap

### Phase 1 — MVP (current)
- Launcher home screen
- App search
- Default actions with fallbacks
- Settings shortcuts

### Phase 2
- Favorites editor
- Hidden apps
- Notification digest (build the real digest from the listener)
- Focus mode
- Better default-app selection UI

### Phase 3
- Local AI intent router (on-device)
- Voice command support
- On-device model integration
- Offline assistant

### Phase 4
- Device owner mode
- Kiosk / deep focus mode
- Optional LineageOS integration

---

## Contributing

Page is open source under the [GPL-3.0](LICENSE). Issues and pull requests are
welcome. Please keep the privacy guarantees above intact: **no cloud AI, no
analytics, no tracking, no ads, and no Google Play Services dependency.**

Look for `TODO(verify)` and `TODO(phase2)` comments for good first tasks.
