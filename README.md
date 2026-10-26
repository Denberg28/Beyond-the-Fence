# Beyond the Fence

A small farm, a warm home, and something moving past the fence. An offline Android farming and survival game with original vector artwork, quick sessions, and no accounts, ads, purchases or internet permission.

**Current test candidate: 0.2.0-rc.1.** Android 8.0+ (API 26), phones and tablets. Native Java/Canvas; no runtime dependencies. This is a new project; no previous app, signing identity or known-good release existed. Baseline rules: [Denberg28/Agents_md v1.1](https://github.com/Denberg28/Agents_md/blob/main/AGENTS.md). The initial rules commit is `1e9f9a0de2f926124d00c8378204c76669db3900`.

## Play

Everything happens in one game window. Seed cards sit above the world; the resource/fence/pause badges sit along the top and the action dock along the bottom. No Farm/Build/Defend tab switching is required.

1. Tap an unlocked **seed card**, then tap an empty **soil tile in the world** to plant. Growing tiles show remaining active seconds and a growth bar. Golden ripe tiles sell their crop when tapped. Plant all/Harvest in the dock handle multiple plots.
2. Tap **Build** to open upgrades over the paused world. Improve the rifle, fence, irrigation and land. Locked soil opens the same overlay without spending coins; buy land explicitly. Close the overlay, then tap Play when ready.
3. Tap the **fence badge** to repair for 8 coins (up to +40 strength). Its label shows current strength; it is disabled when full or unaffordable.
4. Day lasts75 active seconds. Tap **Start night** to begin early and explicitly resume play. Your rifle auto-fires. Tap the **top, middle or bottom defense lane directly** for focus fire (3s recharge). Selected lanes have a gold outline/checkmark and native threat counts.
5. Weather changes at dawn. Survive for a reward; if the fence falls, the dock offers a free rebuild while preserving crops and upgrades. Rebuild returns paused.

The world adapts between portrait (defense above farmland) and landscape (farmland beside defense). Native tiles share exact bounds with the artwork and keep >=48dp targets. Swipe from a screen edge to reveal Android system controls. Large fonts/short screens can scroll the game window and upgrade overlay instead of shrinking targets. Native labels support TalkBack. Selected seeds and an open upgrade overlay survive rotation. Backgrounding/focus loss pauses; returning never starts play automatically.

Options includes help, relaxed mode (half incoming damage), reduced motion, save import/export, and confirmed new-farm reset. Local progress advances only during play. English only; no audio/music in this candidate.

## Install / upgrade / rollback

Download the **Preview.apk** from [GitHub Releases](https://github.com/Denberg28/Beyond-the-Fence/releases), allow installation from your browser/file manager, then open it. No permissions are requested. The release APK is unsigned build output for future production signing and cannot be installed as-is; use Preview.apk for testing.

Preview uses Android's **debug test signing**, not a production identity. No signing key is committed. A clean CI runner may produce a different debug certificate. **Export a `.btfs` backup before any upgrade or uninstall.** If Android reports a signing conflict, export, uninstall, install the new preview, then import the backup. Save files contain no account or personal data. Uninstall clears local saves; Android cloud backup is disabled. Imports validate schema, bounds and CRC before replacement. Invalid imports leave current progress intact. Saves from newer schemas are not supported. Retain the published rc.2 APK and export a schema1 backup for rollback. Android can reject a lower versionCode; backup/uninstall/install/import may be required.

Production promotion requires a persistent private signing key configured through CI secrets, an upgrade test with the same certificate, and the device checks in `docs/TESTING.md`. Never publish a private key as a release asset.

## Build and test

Use JDK 17, Android SDK platform 35, build-tools 35.0.0, Gradle 8.9 (checksum-pinned wrapper), Android Gradle Plugin 8.7.3. Install Android Studio's command line SDK, accept the licenses, set `ANDROID_HOME` or add `sdk.dir=/absolute/sdk/path` to untracked `local.properties`.

```sh
sdkmanager 'platforms;android-35' 'build-tools;35.0.0' 'platform-tools'
./gradlew testDebugUnitTest assembleDebug assembleDebugAndroidTest assembleRelease lintDebug lintRelease
python3 scripts/check_package.py "$ANDROID_HOME/build-tools/35.0.0/aapt" app/build/outputs/apk/debug/app-debug.apk
```

Outputs: `app/build/outputs/apk/debug/app-debug.apk` (test-signed preview), `app/build/outputs/apk/release/app-release-unsigned.apk`, and `app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk`. JVM test reports are under `app/build/reports/tests/testDebugUnitTest/`. First dependency resolution requires internet; the installed game does not.

CI builds and checks each push/PR, then runs native pointer/tile, portrait/landscape, 200% font, lifecycle/persistence instrumentation on Android 26 and 35 emulators. A successful authorized main-branch run creates a **prerelease**, never stable, with APKs, source ZIP, SHA-256 checksums, build provenance and test evidence. Existing version tags are never overwritten. See `docs/SPEC.md`, `docs/ARCHITECTURE.md`, `docs/TESTING.md`, and `docs/KNOWN_ISSUES.md`.
