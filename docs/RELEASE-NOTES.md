# 0.16.1-rc.1 — Right-side pond spacing

29 October 2026 (Asia/Manila). Based on published 0.16.0-rc.1, commit 307d78c.

Move the fishing pond to the right end below the south fence. Leave 16dp from the fence baseline and at least 12dp above the Plant button when the pond is visible. Artwork and the 56×48dp native touch target share the same position. Landscape reserves 8dp more minimum board height so the pond keeps its full touch target and fence gap in short windows. Command positions and farm layout rules are retained.

Only pond placement changes. Fishing timing, catch rewards/cap, planting, enemies, economy and save schema 12 remain unchanged. VersionCode 22 and the retained signing identity support install-over updates from 0.12+ same-key builds.

Export a `.btfs` backup before updating. Install Release.apk on Android 8.0+. Keep the previous APK and pre-update backup for rollback; older disposable-key builds require save transfer.

Required candidate checks: both JVM variants, Android builds/lint/package/audio audits, API26/35 native portrait/landscape/200% font and all settlement layouts, actual pond touch/fishing, visible pond-to-Plant spacing, retained certificate/exact compiled-content verification and install-over checkpoint retention. Results are recorded in docs/TESTING.md and release evidence. Physical-phone endurance, accessibility and human playtesting remain NOT TESTED; this is a prerelease.
