# Beyond the Fence

A small farm, a warm home, and something moving past the fence. An offline Android farming and survival game with original vector artwork, quick sessions, and no accounts, ads, purchases or internet permission.

**Current test candidate: 0.1.0-rc.2.** Android 8.0+ (API 26), phones and tablets. Native Java/Canvas; no runtime dependencies. This is a new project; no previous app, signing identity or known-good release existed. Baseline rules: [Denberg28/Agents_md v1.1](https://github.com/Denberg28/Agents_md/blob/main/AGENTS.md). The initial rules commit is `1e9f9a0de2f926124d00c8378204c76669db3900`.

## Play

1. Tap **Play**. In Farm, choose seeds and tap an empty plot. Tap ripe crops or Harvest all to sell them. Plant empty plots fills only affordable empty plots; existing crops stay intact. Each growing plot shows active seconds to harvest, adjusted for weather and irrigation.
2. In Build, improve your rifle, fence and irrigation; expand from six to nine plots. Corn unlocks at level 2 and pumpkins at level 3.
3. Daylight lasts 75 active seconds. Prepare for night or start it early in Defend. Auto-fire covers all three lanes. Tap a lane to prioritize it and fire a bonus focus shot every three seconds.
4. Survive for a dawn reward. Walkers, runners, brutes and spitters unlock across nights 1–4. Weather changes each dawn: clear, rain (faster crops/slower zombies), fog (visual haze), heat (slower crops).
5. If your fence falls, rebuild free. Keep crops and upgrades; lose at most 30 coins. A starting rifle can survive the first night even with zero coins.

Pause any time. Leaving the app pauses immediately; reopening requires Play. Crops and attacks never advance while closed. The selected gameplay tab and seed choice survive rotation. Options includes help, relaxed mode (half incoming damage), reduced motion, backup export/import, and a confirmed new-farm action. Native labeled buttons support TalkBack; color is accompanied by text. Scrollable controls support large fonts. English only in this MVP.

## Install / upgrade / rollback

Download the **Preview.apk** from [GitHub Releases](https://github.com/Denberg28/Beyond-the-Fence/releases), allow installation from your browser/file manager, then open it. No permissions are requested. The release APK is unsigned build output for future production signing and cannot be installed as-is; use Preview.apk for testing.

Preview uses Android's **debug test signing**, not a production identity. No signing key is committed. A clean CI runner may produce a different debug certificate. **Export a `.btfs` backup before any upgrade or uninstall.** If Android reports a signing conflict, export, uninstall, install the new preview, then import the backup. Save files contain no account or personal data. Uninstall clears local saves; Android cloud backup is disabled. Imports validate schema, bounds and CRC before replacement. Invalid imports leave current progress intact. Saves from newer schemas are not supported. This first version has no previous binary to roll back to; retain the APK and exported backup for future rollback.

Production promotion requires a persistent private signing key configured through CI secrets, an upgrade test with the same certificate, and the device checks in `docs/TESTING.md`. Never publish a private key as a release asset.

## Build and test

Use JDK 17, Android SDK platform 35, build-tools 35.0.0, Gradle 8.9 (checksum-pinned wrapper), Android Gradle Plugin 8.7.3. Install Android Studio's command line SDK, accept the licenses, set `ANDROID_HOME` or add `sdk.dir=/absolute/sdk/path` to untracked `local.properties`.

```sh
sdkmanager 'platforms;android-35' 'build-tools;35.0.0' 'platform-tools'
./gradlew testDebugUnitTest assembleDebug assembleDebugAndroidTest assembleRelease lintDebug lintRelease
python3 scripts/check_package.py "$ANDROID_HOME/build-tools/35.0.0/aapt" app/build/outputs/apk/debug/app-debug.apk
```

Outputs: `app/build/outputs/apk/debug/app-debug.apk` (test-signed preview), `app/build/outputs/apk/release/app-release-unsigned.apk`, and `app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk`. JVM test reports are under `app/build/reports/tests/testDebugUnitTest/`. First dependency resolution requires internet; the installed game does not.

CI builds and checks each push/PR, then runs native UI/lifecycle/persistence instrumentation on Android 26 and 35 emulators. A successful authorized main-branch run creates a **prerelease**, never stable, with APKs, source ZIP, SHA-256 checksums, build provenance and test evidence. Existing version tags are never overwritten. See `docs/SPEC.md`, `docs/ARCHITECTURE.md`, `docs/TESTING.md`, and `docs/KNOWN_ISSUES.md`.
