# Beyond the Fence

A small farm, a warm home, and something moving past the fence. An offline Android farming and survival game with original vector artwork, quick sessions, and no accounts, ads, purchases or internet permission.

**Current test candidate: 0.7.0-rc.1.** Android 8.0+ (API 26), phones and tablets. Native Java/Canvas; no runtime dependencies. Builds on the verified0.6.0-rc.1 mercenary preview; production signing is not configured. Baseline rules: [Denberg28/Agents_md v1.1](https://github.com/Denberg28/Agents_md/blob/main/AGENTS.md). The initial rules commit is `1e9f9a0de2f926124d00c8378204c76669db3900`.

## Play · farm fortress

The game is a top-down fortress defense with original, chunky arcade artwork. Crops, a mill, a market and barracks sit inside the fence. The farm occupies a compact central area, leaving visible terrain on every side. Zombies wander outside during preparation, then approach from north, east, south and west during raids. Uniformed mercenaries patrol the perimeter and shoot nearby targets; mercenaries are the only fighters. One volunteer guard starts with each new farm.

1. Open **Seeds**, select a crop, then tap soil inside the walls. Harvest ripe tiles or use Harvest to earn **coins and timber**. Start with80 coins,24 timber and6 plots; corn/pumpkins become available on days2/3; there are no player levels.
2. Tap **N/E/S/W** on the map to open fence construction. East/west start as gaps. Spend coins/timber to build each border, then upgrade **Wood → Stone → Steel**. Durability bars, cracks and numeric HP show remaining fence strength; hearts have been removed. Each fence has independent health. Zombies chip it down and enter when it reaches zero; stronger tiers absorb more damage. Tap a damaged border to repair it for8 coins and4 timber (+50HP). Fence also sells10 timber for12 coins. Repair homestead restores the separate farm-core health.
3. Tap an interior **Mill/Shop/Camp** plot to open construction. The timber mill produces4 timber every12 active seconds; the market adds25% to crop sale value; barracks unlock recruitment. All purchases show costs before confirmation by tapping their purchase button.
4. Open **Crew / Squad** to hire up to3 mercenaries. They roam the fence even during daylight and shoot zombies within range at night. No recurring wages. Breached zombies can injure and kill mercenaries. Surviving guards heal1.5HP per active preparation second. Rally N/E/S/W moves guards to defend that border; Resume perimeter patrol releases the order. Rally never deals an instant shot.
5. **Build → Gear** upgrades crew uniforms (Workwear → Scout green → Desert kit → Urban armor → Ranger kit → Black ops), fortress strength, irrigation and farm size. Uniforms visibly change armor, colors and insignia, add3 damage and20 maximum health per tier, improve firing cadence/range and reduce injuries. Each day raises the threat: preparation75→35s, reinforcement3.2→0.85s, quota10→45. Runners rush at twice Walker speed; Brutes are slow, durable fence smashers; Spitters attack from outside the fence. **Every zombie must be killed to end the raid**; there is no75-second despawn. Rally the short-range crew to threatened borders. Intruders destroy crops and buildings and can wipe out your farm. Defeat offers free recovery at the same day with one volunteer if the squad was lost, keeping surviving assets and upgrades; ruined crops/buildings stay lost. Recovery grants enough seed coins to reach12 if needed.
6. An original120BPM action score plays during active raids. **Options → Action music** mutes it and remembers your choice. Pause, panels, focus loss and backgrounding stop playback. Woodland silhouettes, worn approach tracks, rocks and abandoned camp debris frame the farm. All scenery and music are original, generated locally from source.

Build/Squad panels pause simulation. Closing them stays paused; tap Play to resume. Background/focus loss also pauses. Immersive system bars reappear by edge swipe. Native accessible controls match the artwork and have >=48dp targets. Large fonts/short windows can scroll. Seed choice, build panel page and selected border survive rotation. English only; player balance and physical accessibility still need handset validation.

## Install / upgrade / rollback

Download **Preview.apk** from [GitHub Releases](https://github.com/Denberg28/Beyond-the-Fence/releases). Android8.0+; no permissions, accounts, ads or network access. Unsigned.apk is future production-signing output and is not installable directly.

Preview uses debug test signing. A fresh runner can use a different certificate. **Export your current `.btfs` save before upgrading or uninstalling.** A signing conflict requires export→uninstall→install→import. Uninstall removes local data. Production signing continuity remains unconfigured.

Save schema6 adds mercenary health. Schema1–5 backups migrate; older combat upgrades become uniforms. Legacy squads above3 retain their first3 guards and receive each excess recruit's original hire cost plus6 timber once (slots4–6:130/155/180 coins). Existing crops, resources, assets and damage remain. Legacy XP is read for compatibility but earns nothing and affects no gameplay. Crop availability and difficulty now depend on day. Schema6 cannot be opened by0.6 or older; keep a pre-upgrade export and previous APK for rollback.

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

Zombie designs are original Canvas artwork: ragged walkers, lean runners, armored brutes and bloated spitters, with eyes, teeth, torn clothing and simulation-timed movement. Reduced motion keeps their pose static. Fence-material kit coins buy supplies; timber covers framing. No separate stone/steel inventory is needed.
