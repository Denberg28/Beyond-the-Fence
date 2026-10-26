Beyond the Fence **0.1.0-rc.2 Android test candidate**: an offline farm-building and zombie-defense game.

- Plant turnips, corn and pumpkins individually or fill affordable empty plots in one tap; sell harvests and level up.
- See harvest countdowns, ready counts, XP progress, upgrade benefits/coin shortfalls, and lane threat counts. Selected tab/seed survive rotation.
- Fix first-launch automation readiness and exercise bulk actions through installed-app checks; schema1 backups remain compatible.
- Upgrade the rifle, fence, irrigation and farm size.
- Survive three-lane nights against walkers, runners, brutes and spitters; choose a lane for focus shots while auto-fire protects the farm.
- Changing weather, relaxed mode, reduced motion and pause-friendly play.
- Two automatic local checkpoints and manual save backup import/export. No accounts, ads, purchases or network permission.

**Install the Preview.apk** (Android 8.0+). The Unsigned.apk is for future production signing and does not install directly. Source, checksums, build provenance, unit results and emulator evidence accompany the candidate.

**Test signing only:** clean CI runners may sign previews differently. Export a `.btfs` backup before upgrading/uninstalling; signing conflicts require backup→uninstall→install→import. No production signing identity has been established.

**Validation:** release publication requires unit checks, debug/release/test-APK builds, lint, package/signature checks, and installed-APK smoke tests on Android26/35 emulators. See VERIFICATION.txt and the linked CI run for observed evidence. Physical-phone endurance, TalkBack/large-font usability, measured performance and production upgrade/signing checks remain BLOCKED. This is not a stable release.

Local progress advances only during active play. Backgrounding pauses; tap Play to resume. No audio/music, free-roam map or cloud sync in this MVP. Balance is initial tuning and needs player feedback.

Rollback: retain this APK and export a backup. No earlier GitHub APK was published; retain any preview already installed. Read README.md and docs/TESTING.md in the source archive for setup, recovery and device acceptance.
