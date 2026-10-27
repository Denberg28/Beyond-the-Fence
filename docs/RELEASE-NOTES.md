# Beyond the Fence 0.12.0-rc.1 — Century survival

Android8.0+ test preview; com.denberg.beyondthefence, versionCode15. Baseline0.11.1/608a451:75 JVM/1312 native checks per emulator. Prerelease.

Daytime zombies shuffle slowly and some lie dormant in six-second phases; at night they approach with individual uneven gaits. Ghoul, Ironhide and Wraith unlock on days16/31/46. One Grave Warden arrives midway through every fifth night, including Day100, with larger art/crown and a real top health bar. Boss state survives saves without duplicates. The raid ends only after every enemy is defeated.

Progression extends through Day100, then remains bounded for endless play: up to80 enemies,0.6s reinforcement floor,20 uniform/fortify/Steel/drill/hero ranks and10 mill/irrigation tiers. Chapters open additional ranks over time; they change real combat/health/output. Three mercenaries plus one hero remain the cap. Regular kills pay1coin, boss salvage10+day/5 (max30).

Shop adds Arc bolt30c (single nearest threat) and Starfall65c (area0.32 radius). Damage scales with day and hero training; bosses resist40%. No live targets means no charge. Three quick slots share saved inventory,9 per potion; hold a slot or tap an empty slot to assign any of four potions. Upper-left slot replaces Merc+; hire through Barracks. Merc↑ remains. Actual spell rings identify the cast area.

A larger Survived-days heading counts completed nights, with full-width resource/status rows and three equal HUD buttons. Transparent dock/slot symmetry stays. New original32s Bard at Dawn lute/recorder/frame-drum and The Hunt haunting strings/bells replace phase scores; gun/zombie/owl/cricket effects and independent mutes/pause/focus cleanup remain.

Schema8 reads1–7, preserving existing resources, crops, damage, hero and consumables, adding empty attack inventory/default equipped slots. New exports cannot downgrade to0.11. Keep the old APK and export before updating.

Published0.12 APKs start one privately retained signing identity, with certificate pinned in source. Future higher-version builds signed with this identity can install as updates and retain progress. The earlier disposable preview keys were not saved; this new key cannot update those installations directly. Moving from0.11 or earlier still requires ONE export/install/import transfer. Do not uninstall before exporting. Emulator install-r tests re-sign an old baseline to test same-key migration; this does not bypass Android's real certificate requirement.

Candidate gates:81 JVM, Android build/lint/package/signature/version/API/zero-permission/size/six-WAV audit, full exact signed-APK API26/35 tests plus actual install-r/schema7→8 checkpoint retention and screenshot self-review. Signing payloads must match a fresh source build and fixed certificate. Private key/password never appear in Git, CI cache, logs or public releases.

Physical-phone listening/mixing/routing,30-minute endurance/20 interruptions, TalkBack/200% font and measured performance remain BLOCKED for stable. No claim of100-day human balance playtesting; HIL N/A.
