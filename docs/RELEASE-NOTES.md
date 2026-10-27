# Beyond the Fence 0.11.1-rc.1 — First light and clear controls

Android8.0+ test preview; package com.denberg.beyondthefence, versionCode14. Baseline0.10/2cdb19a:72 JVM tests,1182 native checks/API26 and35. Prerelease only.

Compact dock and tab captions scale within their available space so whole words fit at200% font; larger body/purchase text retains the system scale.

Bottom command/action padding is transparent over original day/night terrain. Mercenary buttons and bottle slots have mirrored sizes and baselines; bottom actions share equal heights with consistent spacing, including large fonts. No extra frame animation or runtime dependency.

One stable Fence / Mill / Shop / Barracks tab strip replaces the previous mixed general/building navigation. Each map building opens its matching page. Switching resets scroll to the top, replaces old actions/price hints, and synchronizes title and selected tab. Rotation preserves the page; panels pause gameplay and closing stays paused. Mill owns timber/production/fortify; Shop owns crops/irrigation/land/items; Barracks owns crew/uniform/drills/hero.

Every defeated zombie pays1 coin once, reduced from the existing2-coin reward. No variant multipliers; harvest and dawn income/prices remain. New actual guard-kill/reload/cap cases protect against duplicate income.

New original32-second “First Light” morning music uses felt keys, mallet arpeggios, warm bass and a breathy lead. The night score and four effects remain. Independent mutes, focus loss, background and pause cleanup remain intact. No copied themes/samples.

Save schema7 unchanged;0.10 imports remain compatible. Export your .btfs before upgrading or uninstalling. Preview certificates may differ; a conflict can require export/uninstall/install/import. Uninstall erases local saves. Keep the previous APK/export for rollback. Production signing continuity is not configured.

Candidate gates:75 JVM tests, Android builds/lint, package/version/signature/zero-permission/size/six-WAV audits, exact-APK API26/35 native regressions, plus screenshot self-review. Native checks cover repeated tab switches after scrolling, title/selected-tab/content consistency, transparent bottom terrain, mirrored slots/commands and equal action heights in portrait/landscape/200% font.

Physical-phone listening/mixing/routing,30-minute endurance with20 interruptions, TalkBack, measured performance and production certificate/upgrade continuity remain BLOCKED for stable. Emulator checks do not establish audible quality. HIL N/A.
