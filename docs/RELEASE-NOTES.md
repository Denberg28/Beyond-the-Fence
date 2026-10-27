# Beyond the Fence 0.7.0-rc.1 — Survival pressure

Android8.0+ test preview; package com.denberg.beyondthefence, versionCode9. Builds on verified0.6.0-rc.1. This is a prerelease for playtesting, not a stable promotion.

## Gameplay

No player levels or XP rewards. Survive one escalating day at a time; corn/pumpkins unlock on days2/3. A maximum of3 short-range mercenaries defend the farm. Six uniform tiers visibly change their gear and improve damage, armor, maximum health and firing cadence. Breached zombies can kill guards; surviving guards heal during preparation. Hire replacements at the barracks.

Walkers approach steadily, Runners rush at twice their speed, Brutes soak shots and smash fences, and Spitters attack fences at range. Every day raises health, speed, damage and horde density. Raids end only after every enemy is killed; the old75-second automatic clear is removed. Wooden defenses can fail on Day3. Stone and uniform investment can survive the same seeded assault. Later hordes can overwhelm even an unattended elite squad.

Fence hearts removed; existing bars, cracks and numeric HP report damage. Added original woodland, tracks, rocks and camp debris, plus an original120BPM local action loop. Options can mute music; playback stops during pause, panels, backgrounding and audio-focus loss.

## Compatibility and recovery

Export your current .btfs save before upgrading. Preview certificates can differ across builds: signing conflicts require export/uninstall/install/import. Uninstall erases local data. Production signing remains unconfigured.

Schema6 reads schemas1–5. Legacy squads keep their first3 guards and refund excess slots once:130/155/180 coins plus6 timber each. Existing investments and damage remain. Old training becomes uniform tier; XP is retained only as a compatibility field. New backups cannot downgrade to0.6 or earlier; keep a prior export/APK for rollback.

Defeat retains ruined crops/buildings, retries the same day and restores the core/invested fences; grants one volunteer only when the crew is empty and seed aid up to12 coins. Recovery is a retry, not a dawn victory or difficulty skip.

## Verification tier

Required automated candidate gates: JVM behavior/migration/corruption tests, debug/release builds, lint, zero-permission package/signature/size checks and native installed-APK tests on API26/35. The workflow publishes only after these pass. Exact logs and screenshots accompany the release.

Physical-phone endurance, actual speaker/headphone listening, TalkBack and measured frame/thermal/latency tests remain BLOCKED. Emulator audio tests prove playback state and lifecycle, not perceived sound quality. Production signing/upgrade continuity remains BLOCKED. Physical equipment control is N/A.
