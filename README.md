# Beyond the Fence

A small farm, a warm home, and something moving past the fence. An offline Android farming and survival game with original vector artwork, quick sessions, and no accounts, ads, purchases or internet permission.

**Current test candidate: 0.3.0-rc.1.** Android 8.0+ (API 26), phones and tablets. Native Java/Canvas; no runtime dependencies. This is a new project; no previous app, signing identity or known-good release existed. Baseline rules: [Denberg28/Agents_md v1.1](https://github.com/Denberg28/Agents_md/blob/main/AGENTS.md). The initial rules commit is `1e9f9a0de2f926124d00c8378204c76669db3900`.

## Play · farm fortress

The game is a top-down fortress defense with original, chunky arcade artwork. Crops, a mill, a market and barracks sit inside the fence. Zombies approach from north, east, south and west. Blue mercenaries patrol the perimeter and shoot nearby targets; the watchtower provides baseline automatic defense.

1. Open **Seeds**, select a crop, then tap soil inside the walls. Harvest ripe tiles or use Harvest to earn **coins and timber**. Start with80 coins,24 timber and6 plots; corn/pumpkins unlock with XP.
2. Tap **N/E/S/W** on the map to open fence construction. East/west start as gaps. Spend coins/timber to build each border, then upgrade it through three tiers. Stronger walls reduce incoming damage. Repair in Fence restores shared fortress strength.
3. Tap an interior **Mill/Shop/Camp** plot to open construction. The timber mill produces4 timber every12 active seconds; the market adds25% to crop sale value; barracks unlock recruitment. All purchases show costs before confirmation by tapping their purchase button.
4. Open **Squad** to hire up to6 mercenaries. They roam the fence even during daylight and shoot zombies within range at night. No recurring wages or guard deaths in this candidate. Rally N/E/S/W focuses the watchtower; patrols continue their circuit.
5. **Build → Gear** upgrades the watchtower, maximum fortress strength, irrigation and farm size; it also offers Plant all. Day lasts75 active seconds. Tap Night to begin the wave early. Survival gives dawn rewards; defeat offers free recovery while retaining crops, buildings, guards and wall tiers.

Build/Squad panels pause simulation. Closing them stays paused; tap Play to resume. Background/focus loss also pauses. Immersive system bars reappear by edge swipe. Native accessible controls match the artwork and have >=48dp targets. Large fonts/short windows can scroll. Seed choice, build panel page and selected border survive rotation. English only; player balance and physical accessibility still need handset validation.

## Install / upgrade / rollback

Download **Preview.apk** from [GitHub Releases](https://github.com/Denberg28/Beyond-the-Fence/releases). Android8.0+; no permissions, accounts, ads or network access. Unsigned.apk is future production-signing output and is not installable directly.

Preview uses debug test signing. A fresh runner can use a different certificate. **Export your current `.btfs` save before upgrading or uninstalling.** A signing conflict requires export→uninstall→install→import. Uninstall removes local data. Production signing continuity remains unconfigured.

Save schema2 persists timber, wall tiers, buildings, enemy approach offsets, production timing and every guard's patrol/fire state. Schema1 saves from0.2 migrate on load/import without losing old crops, resources or upgrades; migrated farms receive a complete perimeter matching their existing fence investment and24 initial timber. New saves use schema2 and cannot be read by0.2. Retain your pre-upgrade schema1 export and0.2.0-rc.2 APK for downgrade; Android may require uninstall/reinstall for lower version codes or different certificates. Never import a schema2 save into an older release.

Atomic alternating local checkpoints and validated backup import/export remain. Corrupt imports leave the farm intact; damaged checkpoints recover from the other slot. Keep on-device backups for offline use. No simulation happens while backgrounded or closed.

## Build and test

Use JDK 17, Android SDK platform 35, build-tools 35.0.0, Gradle 8.9 (checksum-pinned wrapper), Android Gradle Plugin 8.7.3. Install Android Studio's command line SDK, accept the licenses, set `ANDROID_HOME` or add `sdk.dir=/absolute/sdk/path` to untracked `local.properties`.

```sh
sdkmanager 'platforms;android-35' 'build-tools;35.0.0' 'platform-tools'
./gradlew testDebugUnitTest assembleDebug assembleDebugAndroidTest assembleRelease lintDebug lintRelease
python3 scripts/check_package.py "$ANDROID_HOME/build-tools/35.0.0/aapt" app/build/outputs/apk/debug/app-debug.apk
```

Outputs: `app/build/outputs/apk/debug/app-debug.apk` (test-signed preview), `app/build/outputs/apk/release/app-release-unsigned.apk`, and `app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk`. JVM test reports are under `app/build/reports/tests/testDebugUnitTest/`. First dependency resolution requires internet; the installed game does not.

CI builds and checks each push/PR, then runs native fortress construction/recruitment/pointer, portrait/landscape, 200% font, lifecycle/persistence instrumentation on Android 26 and 35 emulators. A successful authorized main-branch run creates a **prerelease**, never stable, with APKs, source ZIP, SHA-256 checksums, build provenance and test evidence. Existing version tags are never overwritten. See `docs/SPEC.md`, `docs/ARCHITECTURE.md`, `docs/TESTING.md`, and `docs/KNOWN_ISSUES.md`.
