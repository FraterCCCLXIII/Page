# Contributing to Page

Thanks for your interest in Page — a calm, private, text-first Android launcher.
Contributions of all sizes are welcome, from typo fixes to new features.

## Guiding principles

Every change must preserve Page's core promises:

- **No cloud AI, no analytics, no tracking, no ads.**
- **No Google Play Services dependency.**
- **No network permission** unless a feature genuinely requires it and is opt-in.
- **Privacy-first by default**, text-first UI, calm and uncluttered.

If a change would weaken any of these, open an issue to discuss it first.

## Getting started

1. **Fork** this repo and clone your fork.
2. Open the project in **Android Studio** (Ladybug or newer, JDK 17+; JDK 21
   recommended).
3. Let Gradle sync (it uses the version catalog in `gradle/libs.versions.toml`).
4. Build/run on a device or emulator (min SDK 29 / Android 10).

```bash
./gradlew :app:assembleDebug   # build
./gradlew :app:lintDebug       # lint
./gradlew :app:installDebug    # install on a connected device
```

## Branching & commits

- Create a feature branch off `main`: `git checkout -b feature/short-name`.
- Keep commits focused; write clear, imperative commit subjects
  (e.g. "Add favorites editor").
- Reference issues in the body where relevant (`Fixes #123`).

## Pull requests

- Open PRs against `main`. Fill out the PR template.
- CI (build + lint) must pass before review.
- Keep PRs small and reviewable. Large features are easier to land in slices.

## Code style

- **Kotlin**, official style (`kotlin.code.style=official`).
- Follow **SOLID** and prefer composition/reuse over duplication.
- **Compose**: reuse the existing components in `ui/components` and the design
  tokens in `ui/theme` (color, type, spacing) — avoid hardcoded values.
- Components should be generic, accessible (content descriptions, keyboard
  navigable), and responsive by default.
- Add comments only for non-obvious intent, not to narrate code.

## Project layout

See the "Project structure" section in the [README](README.md). The deterministic
assistant lives in `assistant/` (no AI), launch/intents in `launcher/`, data in
`data/`, and the UI in `ui/`.

Good first tasks are marked with `TODO(verify)` and `TODO(phase2)` in the source,
and in the [roadmap](README.md#roadmap).

## Reporting bugs & requesting features

Use the issue templates. Include device, Android version, and steps to
reproduce for bugs.

## License

By contributing, you agree that your contributions are licensed under the
project's [GPL-3.0](LICENSE) license.
