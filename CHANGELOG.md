# Changelog

## 0.2.0-rc.1 · 27 October 2026

- Replace the separate form-style panels with one immersive game window: illustrated seed cards above the world, directly actionable soil and defense lanes, and a compact bottom dock.
- Show crop growth/harvest rewards on the actual tiles, highlight selected seeds and lanes, and open land upgrades from locked soil.
- Put repair on the fence badge; open upgrades as a paused overlay with an explicit close control. Start night deliberately starts play; rebuild returns paused.
- Adapt the board to portrait/landscape, keep native accessible 48dp targets, retain seed/overlay after rotation, and allow scrolling at large font sizes.
- Add shared artwork/tile geometry tests, real pointer-input checks, portrait/landscape and 200% font checks and five screenshot views on API26/35.
- Package/schema1 and economy/combat rules remain compatible with rc.2; test signing and physical-phone limitations remain.

## 0.1.0-rc.2 · 27 October 2026

- Pause immediately when a system overlay removes window focus; returning focus never resumes play automatically.
- Await the completed Home command and observed UI state in emulator lifecycle checks; collect activity, screenshot and crash evidence even on failure.

- Bulk plant affordable empty plots without replacing crops; show exact count and total seed cost before tapping.
- Show weather/irrigation-adjusted harvest countdowns, ready-crop counts, XP toward the next level, upgrade benefits/coin shortfalls, and threats per lane.
- Retain the selected gameplay tab and seed after activity recreation. Save schema 1 is unchanged.
- Repair the emulator welcome-screen race: initialize accessibility before launch, wait up to 10 seconds for the real welcome action, and fail explicitly if unavailable. Exercise bulk planting/selling and rotation context through native controls.
- Previous source `975d790` passed build/lint/unit checks but failed both emulator jobs on a null accessibility window. No rc.1 release was published. rc.2 publication remains gated on the complete checks.

## 0.1.0-rc.1 · 26 October 2026

First Android test candidate: offline farming, crop sales/XP, building and rifle upgrades, three-lane night defense, four zombie varieties, weather, free fence recovery, help, relaxed mode, reduced motion, automatic dual checkpoints and save backup import/export. Original Canvas farm artwork and native accessible controls. Reproducible Gradle build and gated GitHub prerelease workflow. Device acceptance and production signing remain required before stable promotion.
