Beyond the Fence **0.4.0-rc.1 · Roaming Zombies**

- Smaller central farm with more visible wilderness around all four borders; crop/building/fence actions retain48dp touch targets.
-4 zombies roam outside at player level1, increasing to a maximum10. They cannot harm the farm during preparation. When the raid starts, they turn into actual enemies at their current positions and count toward the wave quota.
- More frequent attacks as player level rises: preparation75→30 active seconds, reinforcements4.8→1.2 seconds across levels1→10. Player level and a visible Raid in countdown explain the pacing; tap Raid in to start early.
- Existing fences, timber buildings, mercenary patrols, farming/gear upgrades, weather, local saves, pause and recovery remain.

Install **Preview.apk** on Android8.0+. This is a test-signed prerelease, not a stable release. Export your current farm before upgrading/uninstalling. Fresh CI signing certificates may require export→uninstall→install→import.

Schema1/2 backups migrate to schema3, preserving existing farm resources, construction and combat. Schema3 includes wandering zombies and cannot be read by0.3 or older versions. Keep a pre-upgrade backup and the0.3 APK for rollback. An older daytime save adopts the level-based countdown when resumed.

Release gates: JVM economy/combat/roaming/migration/geometry checks, debug/release/test builds, lint, identity/signature audit, and exact-artifact API26/35 native construction/recruitment/roaming/automatic-raid/lifecycle/font/rotation/checkpoint tests. Observed results, checksums, source and screenshots accompany this release. Physical-phone endurance, TalkBack, measured performance and production signing remain unvalidated. Balance needs player feedback; no cloud, ads, purchases or network permission.
