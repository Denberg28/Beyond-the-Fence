Beyond the Fence **0.5.0-rc.1 · Breach and Rebuild**

- Zombies visibly damage individual fence sides. At zero health the side becomes a breach and lets attackers inside.
- Intruders can ruin crops and destroy the mill, market and barracks. Ruined crops earn no harvest coins, timber or XP. Replant soil and rebuild lost buildings; their benefits stop while destroyed.
- Sell your harvest to fund defenses: buy10 timber for12 coins in Fence, repair a side for8 coins/4 timber (+50HP), or repair a building for10 coins/5 timber (+60HP). Barracks and recruitment keep their original costs. Hired guards survive a destroyed barracks, but new hiring requires rebuilding it.
- Fence health bars, cracks, breach warnings, scorched soil, rubble and damage flashes make attacks visible. Repairing a breach blocks new entry; zombies already inside still need to be killed.
- Free defeat recovery restores the homestead and invested fences, with enough seed coins to reach12 when needed. Destroyed crops/buildings remain lost. Existing gear, surviving assets and mercenaries remain.

Install **Preview.apk** (Android8.0+). This is a test-signed prerelease. Export your farm before upgrading/uninstalling; certificate conflicts may require export→uninstall→install→import. Schema1–3 saves migrate to schema4 with intact individual asset health and preserved resources/progression. New schema4 saves cannot open in0.4 or earlier; keep your pre-upgrade backup and previous APK for rollback.

Candidate gates include all JVM economy/damage/combat/save/migration/geometry tests, builds/lint/package/signature audit and exact-artifact Android26/35 pointer, breach/destruction, harvest-funded repair/recruitment, rotation/font, pause/lifecycle and checkpoint recovery checks. Source, hashes, test results and screenshots accompany the release. Physical-phone endurance, TalkBack, measured performance and production signing remain unvalidated. No free pathfinding, guard deaths, network, purchases or ads; balance remains initial tuning.
