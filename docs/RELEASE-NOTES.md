# TAG: Zombie Survival Fantasy 0.13.0-rc.2

Android8.0+ preview, versionCode17. Renamed from Beyond the Fence; package com.denberg.beyondthefence and pinned signing identity are retained for upgrades.

- Reincarnation welcome replaces startup instructions. Optional How to play remains in Options.
- Visible Assign buttons chooses potion or magic independently in every quick slot.
- Equipment tab: nine slots, five rarities, Equip best and a bounded45-relic collection. Scarce ordinary/boss drops; optional capped survival bonuses.
- Optional arena after every tenth completed boss night: accept/decline, four random champions, automatic combat, manual Strike, real shared consumables, pause/retreat,45s timeout and a55% equipment reward chance on victory. No farm damage or duplicate attempt rewards.
- Offline adaptive director varies future reinforcement spacing within narrow bounds and can be disabled. Enemy statistics, quotas, boss schedule and loot remain fixed.
- Day100 opens a portal to a fresh-world farm with relics and assignments retained. Later worlds reuse the same campaign.
- Schema9 reads1–8; equipment/loot RNG, arena continuation, director state and worlds are saved with strict validation. Export before updating; schema9 exports cannot downgrade to0.12.

Arena and portal prompts use scrollable text with fixed action buttons, including system-bar clearance at large fonts. Deferring the welcome story does not block later invitations.

Validation passed: 93 unit tests in each build variant; builds, lint, package/audio audits; pinned signing certificate and exact compiled-content comparison; full installed-app suites on API26 and35; actual non-debuggable release installation and checkpoint retention.

Install Release.apk on Android8.0 or newer. Export a save backup before updating. The retained signing identity supports same-key updates; older disposable-key previews require one-time save transfer.

Prerelease: physical-phone endurance, accessibility, audio/performance and human balance playtesting remain pending.

Evidence: https://github.com/Denberg28/Beyond-the-Fence/actions/runs/40630736666


Release APK SHA-256: `0a0df3e9c249954e43581934be14726902788b4367002ea313fe59abc81ac759`.
Verified source: `9c4e00d80a0646413c06c2ff5dbeeebbd7145c1c`; delivery/tag commit: `245a91a62c8e8a0600c0c19fe24ad8b988dc962a`.
