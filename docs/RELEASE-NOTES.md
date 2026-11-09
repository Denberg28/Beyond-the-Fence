# 0.18.2-rc.1 — Dispersed crew patrols

**Fix:** Mercenaries no longer simply move at identical speeds around the fence in a compact group. New hires enter the least-covered perimeter sector without consuming enemy or loot randomness. When a rally is cancelled, the squad walks naturally toward spaced patrol sectors with slight deterministic wandering. Rally commands still group the crew on the selected side, and regular guard damage, range, cooldown, recruitment limit, health and prices are unchanged.

**Mechanics review:** Validated relevant code paths for hire/upgrade/repair, rally and resume, attack range and targeting, enemy spawns, day/night transitions and save-state patrol progress. Regression tests specifically cover three-guard spacing, hero interaction, unchanged RNG/economy, rally recovery, nonteleporting movement and deterministic save/reload. Wider survival/campaign and Android UI suites remain mandatory release gates.

The game still uses save schema 12 and the same application ID. This release only changes crew placement/movement, adds tests and bumps the APK version to 0.18.2-rc.1 (versionCode 26).

**IMPORTANT:** Automated Preview.apk is CI debug-key signed and **cannot install over an existing privately signed Release.apk** without an Android signature error. Do not uninstall a working game to try the preview. Export your .btfs backup and preserve your signed installation. The unsigned Release.apk is not an installable APK. A stable signed install-over upgrade requires the matching private signing certificate, verified source/payload, and physical Android phone install-over plus save-retention/playtest checks.

Android 8.0+ (API26). Verify unit tests, debug/release builds, lint, package audit and API26/35 emulator test status in the associated GitHub Actions run before installing. Physical phone and same-key upgrade validation are **NOT TESTED**.
