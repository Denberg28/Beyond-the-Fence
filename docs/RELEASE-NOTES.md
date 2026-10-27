# Beyond the Fence 0.8.0-rc.1 — Quick commands and living soundscape

Android8.0+ test preview; package com.denberg.beyondthefence, versionCode10. Baseline verified0.7.0-rc.1/edb4941. Prerelease only.

The N/W/E/S buttons now form a pinned bottom-center compass cross. Each direction repairs only that fence by up to50HP for8 coins, with no timber, panel or pause. Intact fences cost nothing; gaps are built under Build → Fence. Material upgrades still use their displayed coin/timber prices. Merc + hires directly and Uniform ↑ buys the next uniform directly, keeping normal barracks/budget requirements and the3-person cap. The farm scrolls independently above the commands; all targets remain at least48dp.

Sunny original music plays during preparation, creepy music during raids. Zombie growls, nocturnal owl/cricket calls and actual mercenary gunshots use a bounded local effects pool. Independent Background music and Sound effects toggles persist across rotation/restart. Pausing, panels, focus loss and Home stop every voice; no stale shot replay. All6 original PCM assets are generated at build time from source, without downloads, samples, permissions or runtime synthesis.

Save schema6 is unchanged;0.7 backups remain compatible. Older migrations/refunds remain. Export your .btfs backup before upgrading: preview certificates can differ, requiring export/uninstall/install/import. Uninstall removes local data. Keep a previous export/APK for rollback; Android can reject lower version codes. Production signing remains unconfigured.

Required candidate gates: all JVM cases, Android debug/release/test builds, lint, package/signature audit (including6 distinct non-silent unclipped WAVs), exact installed-APK native tests on API26/35, and screenshot self-review. CI publishes only after automated gates pass; logs/reports/source/provenance/checksums accompany the artifact.

Physical-phone audio quality, speaker/headphone/Bluetooth mixing and volume comfort,30-minute endurance, TalkBack, measured performance and production signing/upgrade continuity remain BLOCKED for stable promotion. Emulators prove dispatch/playback state, not audible quality. HIL N/A: no equipment control.
