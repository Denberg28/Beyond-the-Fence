Beyond the Fence **0.2.0-rc.1 · in-world controls**

- Play on one immersive board: choose illustrated seed cards, tap soil to plant, and tap golden ripe tiles to harvest.
- Tap defense lanes in the world to focus the rifle; selected lanes show a gold outline/checkmark, threat counts and recharge time.
- Compact resource/pause/options HUD and bottom Build/Plant all/Harvest/Start night dock. Tap the fence badge to repair.
- Buy upgrades in a paused overlay; locked soil opens expansion without spending. Closing upgrades stays paused. Start night explicitly starts play; free rebuilding stays paused.
- Responsive portrait/landscape layouts, accessible native buttons, shared artwork/touch geometry, retained seed/overlay after rotation and scroll fallback for large fonts.
- Existing crops, economy, weather, enemies, upgrades and schema1 saves remain compatible with rc.2.

**Install Preview.apk** on Android8.0+. Unsigned.apk needs production signing and does not install directly. This preview uses debug test signing: export a `.btfs` save before replacing an installed version. A signing mismatch requires export→uninstall→install→import. Persistent production signing is not configured.

Candidate gates: full builds, JVM suites, lint, package/signature checks, native pointer planting/harvesting/seed/lane/upgrade checks, portrait/landscape recreation, 200% system font, lifecycle pause and checkpoint corruption recovery on Android26/35 emulators. Source/provenance/checksums/test evidence accompany this release. Run-specific observed results are in VERIFICATION.txt. Physical-phone endurance, TalkBack, measured performance and production signing/upgrade checks remain BLOCKED; this is a prerelease.

No internet permission, accounts, ads, purchases, cloud sync, audio or background crop growth. Backups remain schema1. For rollback retain rc.2 and an exported backup; a downgrade/signature conflict may require uninstall/reinstall before import.
