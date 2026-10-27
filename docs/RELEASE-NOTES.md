# Beyond the Fence 0.10.0-rc.1 — Settlements, heroes and supplies

Android8.0+ test preview; package com.denberg.beyondthefence, versionCode12. Baseline0.9.0-rc.1/29eeea5:63 JVM tests,923 native checks/API26 and35. Prerelease only.

Unaffordable upgrades remain tappable and briefly show their full coin/timber requirement inside the button for1.8 seconds, without charging, opening a panel or pausing. Selection/no-selection/max rules remain; successful purchase or changed direction clears stale feedback. Compass and mercenary controls are transparent, with extra padding above the bottom actions and two colored bottle slots.

Survive five days to change settlement: Farm1–5, Village6–10, Castle11–15, then repeat. The perimeter becomes trapezoidal or chamfered and interior buildings/plots reorder; scenery changes with it. Fences, guards, scouts, heroes and enemy targeting use the same boundary. Assets, resources, crop growth, health and ruins remain intact. Chapter changes occur at dawn, not during active raids.

Steel gains4 reinforcement ranks (+30HP/rank and stronger absorption). Mill gains3 production tiers; Barracks gains3 mercenary drill tiers and3 hero training tiers. Recruit1 hero for360 coins/20 timber in the Barracks popup, alongside up to3 mercenaries. Heroes fight, heal during preparation, take damage and can die; replacement costs the normal hire price, while training investment remains.

The Shop sells35-coin Elixirs and45-coin Enhancers, maximum9 each. Left Elixir slot heals35 farm HP/60 living mercenary HP/90 hero HP and spends nothing if all are healthy. Right Enhancer grants20 active seconds of40% stronger damage and20% faster firing; it cannot stack. It also improves hero fire. Timers stop when paused/backgrounded and clear on defeat recovery. Items never revive dead actors.

Dedicated Mill/Shop/Barracks popups expose their purchases. Original32-second scores add piano, plucked strings and sustained chords by day; cello patterns, dark strings, sparse piano and heartbeat percussion at night. These are original compositions using broad cinematic suspense conventions, without copied themes or external samples. Existing gun/zombie/owl/cricket effects, independent mutes and focus/pause cleanup remain.

Schema7 reads schemas1–6, preserving investments/damage and adding empty new inventory/upgrades/hero state. It persists new hero/stock/timer/rank state. Export .btfs before updating; new saves cannot be read by0.9 or older. Preview certificates may differ, requiring export/uninstall/install/import; uninstall erases local data. Keep the previous APK and pre-upgrade export for rollback. Production signing continuity is unconfigured.

Required candidate gates: all JVM tests, Android builds/lint, package/signature/version/zero-permission/size/six-WAV audits, full installed exact-APK tests on API26/35 and screenshot self-review. Native checks include inline prices/no debit, hero recruitment/training, purchases/use of both item slots, mill upgrades and preserved assets in Village/Castle scenes. Full existing economy/damage/save/font/orientation/audio/lifecycle gates remain.

Physical-phone listening/mixing/routing,30-minute endurance with20 interruptions, TalkBack200% font, measured performance and production certificate/upgrade continuity remain BLOCKED for stable. Emulator dispatch/state checks do not prove audible quality. No physical-control HIL applies.
