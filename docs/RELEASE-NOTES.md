Beyond the Fence **0.3.0-rc.1 · Farm Fortress**

A top-down castle-defense direction with original colorful arcade artwork.

- Crops and three constructible buildings inside the perimeter: timber mill, market and barracks.
- Harvest coins and timber; build missing fence sides, reinforce tiers and repair fortress strength.
- Hire up to6 blue mercenaries. They patrol the actual fence boundary and fire only at spatially nearby zombies.
- Zombies approach from north/east/south/west. A watchtower provides automatic baseline defense; rally a border from Squad.
- Tap map plots and wall markers; manage Fence/Farm/Squad/Gear through a paused panel. Compact seed picker and portrait/landscape layout, with native accessible controls and large-font scroll fallback.
- Day/night, weather, crop/weapon upgrades, free defeat recovery, local checkpoints and backups remain.
- Paused farms and construction panels stop continuous scene and control refreshes, reducing unnecessary work. Native regression checks cover static paused rendering and bounded UI waits.

**Install Preview.apk** (Android8.0+). This is a debug-test-signed prerelease; Unsigned.apk requires signing. Export your existing save before replacing/uninstalling. Fresh CI certificates may require export→uninstall→install→import.

**Save upgrade:** schema1 from0.2 is migrated, retaining coins, crops, progression and upgrades. Migrated farms get a complete matching perimeter and24 timber. Schema2 includes buildings/guards and cannot be loaded by older apps. Keep a pre-upgrade schema1 export and0.2 APK for rollback.

Validation: builds, lint, package/signature checks, JVM economy/migration/spatial patrol tests and exact-artifact API26/35 native pointer/construction/recruitment/lifecycle/rotation/font/checkpoint checks gate publication. Check VERIFICATION.txt for observed results and screenshots. Physical-phone endurance, measured performance, TalkBack and production signing remain BLOCKED. Economy values need player feedback.

Limits: one farm;6 immortal/no-upkeep guards; whole-fortress shared health modified by each attacked side's wall tier; fixed building sites rather than free placement; no audio, cloud sync, multiplayer or offline progress. No copied game assets or new runtime libraries.
