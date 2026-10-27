# TAG: Zombie Survival Fantasy 0.13.0-rc.1

Android8.0+ preview, versionCode16. Renamed from Beyond the Fence; package com.denberg.beyondthefence and pinned signing identity are retained for upgrades.

- Reincarnation welcome replaces startup instructions. Optional How to play remains in Options.
- Visible Assign buttons chooses potion or magic independently in every quick slot.
- Equipment tab: nine slots, five rarities, Equip best and a bounded45-relic collection. Scarce ordinary/boss drops; optional capped survival bonuses.
- Optional arena after every tenth completed boss night: accept/decline, four random champions, automatic combat, manual Strike, real shared consumables, pause/retreat,45s timeout and a55% equipment reward chance on victory. No farm damage or duplicate attempt rewards.
- Offline adaptive director varies future reinforcement spacing within narrow bounds and can be disabled. Enemy statistics, quotas, boss schedule and loot remain fixed.
- Day100 opens a portal to a fresh-world farm with relics and assignments retained. Later worlds reuse the same campaign.
- Schema9 reads1–8; equipment/loot RNG, arena continuation, director state and worlds are saved with strict validation. Export before updating; schema9 exports cannot downgrade to0.12.

Required candidate checks: JVM regressions, builds/lint/package audit, API26/35 installed-app suites and private-key signature/content/upgrade checks. Stable remains blocked on physical-phone endurance, accessibility, audio/performance and human balance playtesting. See docs/TESTING.md for observed evidence. No claim of maximum satisfaction or100-day human playtesting.

Observed local checks: all93 JVM tests, debug/release/test-APK builds, debug/release lint, package/audio/version audit and pinned private signature/content comparison PASS. Installed-app API26/35 and actual install-r checks are NOT TESTED on this source. This locally signed development build has not been published to GitHub; publication requires the source push approval and unchanged required native gates.

Latest local packaging: a separately signed Release.apk is available with debugging disabled, alongside the prior development preview. All93 tests pass in each debug/release variant; both variants pass package/audio audits. Certificate/content/alignment checks pass. No new gameplay changes; native installation and phone validation remain pending, and no public GitHub release was published.

Authorized delivery: publish the verified signed Preview.apk and non-debuggable Release.apk, tests/source/checksums and API26/35 evidence as a GitHub prerelease after required gates pass. Both signed APK variants must match a fresh source build and the pinned certificate. Emulator checks run the complete preview suite plus an actual preview-to-release install-r and real checkpoint migration test. Phone validation is still pending; stable is not claimed.
