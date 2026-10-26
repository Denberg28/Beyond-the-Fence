# Changelog

## 0.1.0-rc.2 · 27 October 2026

- Bulk plant affordable empty plots without replacing crops; show exact count and total seed cost before tapping.
- Show weather/irrigation-adjusted harvest countdowns, ready-crop counts, XP toward the next level, upgrade benefits/coin shortfalls, and threats per lane.
- Retain the selected gameplay tab and seed after activity recreation. Save schema 1 is unchanged.
- Repair the emulator welcome-screen race: initialize accessibility before launch, wait up to 10 seconds for the real welcome action, and fail explicitly if unavailable. Exercise bulk planting/selling and rotation context through native controls.
- Previous source `975d790` passed build/lint/unit checks but failed both emulator jobs on a null accessibility window. No rc.1 release was published. rc.2 publication remains gated on the complete checks.

## 0.1.0-rc.1 · 26 October 2026

First Android test candidate: offline farming, crop sales/XP, building and rifle upgrades, three-lane night defense, four zombie varieties, weather, free fence recovery, help, relaxed mode, reduced motion, automatic dual checkpoints and save backup import/export. Original Canvas farm artwork and native accessible controls. Reproducible Gradle build and gated GitHub prerelease workflow. Device acceptance and production signing remain required before stable promotion.
