#!/usr/bin/env bash
#
# install-bundle.sh — provision a device/emulator with the Page "bundle":
# the Page launcher plus all recommended privacy-respecting companion apps.
#
# IMPORTANT: Android does not allow a launcher to silently install other apps.
# This is a *side-loading* helper for your own device (or CI/emulator). It pulls
# each app from its official open-source source and installs over ADB:
#   - F-Droid     (most companions)
#   - GitHub releases (KOReader, Readest, QUIK)
#   - Local build (the Page launcher itself)
#
# Usage:
#   ./scripts/install-bundle.sh                 # Page + all companions
#   ./scripts/install-bundle.sh --no-launcher   # companions only
#   ./scripts/install-bundle.sh --launcher-only # just build+install Page
#   SERIAL=emulator-5554 ./scripts/install-bundle.sh   # target a device
#
# Requirements: adb (platform-tools), curl, python3, gh (GitHub CLI, for
# release downloads). No Google Play / Play Services required.

set -uo pipefail

# --- config ----------------------------------------------------------------
REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
WORKDIR="$(mktemp -d)"
trap 'rm -rf "$WORKDIR"' EXIT

ADB_SERIAL_ARGS=()
[ -n "${SERIAL:-}" ] && ADB_SERIAL_ARGS=(-s "$SERIAL")

INSTALL_LAUNCHER=1
INSTALL_COMPANIONS=1
for arg in "$@"; do
  case "$arg" in
    --no-launcher) INSTALL_LAUNCHER=0 ;;
    --launcher-only) INSTALL_COMPANIONS=0 ;;
    -h|--help) sed -n '2,30p' "$0"; exit 0 ;;
    *) echo "Unknown option: $arg" >&2; exit 2 ;;
  esac
done

# F-Droid apps (installed via the F-Droid index API).
FDROID_APPS=(
  app.organicmaps             # Organic Maps
  net.gsantner.markor         # Markor (notes)
  org.oxycblt.auxio           # Auxio (music)
  net.sourceforge.opencamera  # Open Camera
  ws.xsoh.etar                # Etar (calendar)
  me.zhanghai.android.files   # Material Files
  com.kunzisoft.keepass.libre # KeePassDX (passwords)
  com.beemdevelopment.aegis   # Aegis (2FA)
  net.thunderbird.android     # Thunderbird (email)
)

# GitHub-release apps: "repo|asset-regex". Not on the main F-Droid repo.
# KOReader's asset depends on device ABI; resolved at runtime below.
GITHUB_APPS=(
  "readest/readest|Readest_.*_universal\.apk"
  "quik-sms/quik|QUIK-.*-fdroid\.apk"
)

# --- helpers ---------------------------------------------------------------
ok=(); failed=()

adb_() { adb "${ADB_SERIAL_ARGS[@]}" "$@"; }

require() { command -v "$1" >/dev/null 2>&1 || { echo "Missing required tool: $1" >&2; exit 1; }; }

install_apk() {
  local label="$1" apk="$2"
  echo "  installing $label ..."
  # -r reinstall, -g grant runtime perms, -d allow downgrade.
  if adb_ install -r -g -d "$apk" >/dev/null 2>&1; then
    ok+=("$label")
  else
    # Retry without incremental (older devices / large APKs).
    if adb_ install -r --no-incremental "$apk" >/dev/null 2>&1; then
      ok+=("$label")
    else
      echo "  !! failed: $label" >&2
      failed+=("$label")
    fi
  fi
}

fdroid_install() {
  local id="$1"
  local meta vc url
  meta="$(curl -fsSL "https://f-droid.org/api/v1/packages/$id" 2>/dev/null)" || {
    echo "  !! $id not found on F-Droid" >&2; failed+=("$id"); return; }
  vc="$(printf '%s' "$meta" | python3 -c "import sys,json;print(json.load(sys.stdin)['suggestedVersionCode'])" 2>/dev/null)"
  [ -z "$vc" ] && { failed+=("$id"); return; }
  url="https://f-droid.org/repo/${id}_${vc}.apk"
  if curl -fsSL "$url" -o "$WORKDIR/$id.apk"; then
    install_apk "$id (F-Droid)" "$WORKDIR/$id.apk"
  else
    echo "  !! download failed: $url" >&2; failed+=("$id")
  fi
}

github_install() {
  local spec="$1"; local repo="${spec%%|*}"; local pat="${spec##*|}"
  local asset
  asset="$(gh release view --repo "$repo" --json assets -q '.assets[].name' 2>/dev/null \
            | grep -iE "$pat" | head -1)"
  if [ -z "$asset" ]; then
    echo "  !! no asset matching /$pat/ in $repo" >&2; failed+=("$repo"); return
  fi
  if gh release download --repo "$repo" --pattern "$asset" --dir "$WORKDIR" --clobber >/dev/null 2>&1; then
    install_apk "$repo ($asset)" "$WORKDIR/$asset"
  else
    echo "  !! release download failed: $repo/$asset" >&2; failed+=("$repo")
  fi
}

koreader_install() {
  # KOReader ships per-ABI APKs on GitHub releases.
  local abi variant
  abi="$(adb_ shell getprop ro.product.cpu.abi 2>/dev/null | tr -d '\r')"
  case "$abi" in
    arm64-v8a) variant="arm64" ;;
    x86_64|x86) variant="x86" ;;
    armeabi-v7a|armeabi) variant="arm" ;;
    *) variant="arm64" ;;
  esac
  github_install "koreader/koreader|koreader-android-${variant}-.*\.apk"
}

# --- preflight --------------------------------------------------------------
require adb; require curl; require python3
[ "$INSTALL_COMPANIONS" -eq 1 ] && require gh

if [ -z "$(adb_ get-state 2>/dev/null)" ]; then
  echo "No device/emulator connected. Start one and run 'adb devices'." >&2
  exit 1
fi
echo "Target device: $(adb_ shell getprop ro.product.model 2>/dev/null | tr -d '\r') (ABI $(adb_ shell getprop ro.product.cpu.abi 2>/dev/null | tr -d '\r'))"

# --- Page launcher ----------------------------------------------------------
if [ "$INSTALL_LAUNCHER" -eq 1 ]; then
  echo "Building Page debug APK ..."
  ( cd "$REPO_ROOT" && ./gradlew :app:assembleDebug -q ) || { echo "Gradle build failed" >&2; exit 1; }
  PAGE_APK="$(ls -t "$REPO_ROOT"/app/build/outputs/apk/debug/*.apk 2>/dev/null | head -1)"
  [ -n "$PAGE_APK" ] && install_apk "Page launcher" "$PAGE_APK" || { echo "Page APK not found" >&2; failed+=("Page"); }
fi

# --- companions -------------------------------------------------------------
if [ "$INSTALL_COMPANIONS" -eq 1 ]; then
  echo "Installing companions from F-Droid ..."
  for id in "${FDROID_APPS[@]}"; do fdroid_install "$id"; done
  echo "Installing companions from GitHub releases ..."
  for spec in "${GITHUB_APPS[@]}"; do github_install "$spec"; done
  koreader_install
fi

# --- summary ----------------------------------------------------------------
echo
echo "Installed (${#ok[@]}): ${ok[*]:-none}"
[ "${#failed[@]}" -gt 0 ] && echo "Failed (${#failed[@]}): ${failed[*]}"
echo
echo "Next: open Settings on the device and set Page as the default Home app,"
echo "or run: adb ${ADB_SERIAL_ARGS[*]} shell cmd shortcut ... (or use Page's own"
echo "'Set Page as default launcher' button)."
