# 0.20.0-rc.1 — Dread at dusk

The final12 seconds before each automatic raid now darken the courtyard gradually. Torchlight grows against the cold terrain, the daytime score blends into the night score, and a lower growl warns when dusk and night begin. Nightfall has a brief nonblocking warning. Existing enemies stay visible and scouts retain their positions as they become attackers. Choosing Raid now still starts immediately.

Raid boundaries now refresh and checkpoint immediately. Victory pauses at dawn so players can repair and replant before pressing Play or deliberately starting another raid. Arena outcomes and defeat also checkpoint promptly; stale adventure callbacks cannot pause a fresh farm. Paused rain now uses simulation time, keeping the scene frozen across redraws. Background/focus loss retains explicit resume.

Original pixel-grid undead, guards, hero, kobolds, orcs, dire wolves and hexers replace rounded characters. Broken armour, torn cloaks, narrow lit eyes and grasping hands establish a darker medieval tone. Guards use crossbow artwork and a short string/latch effect with the existing ranged combat stats. Arena/large boss artwork uses cached low-resolution stages scaled without filtering. Existing boss silhouettes and attacks are retained.

VersionCode28, Android8.0+; schema12, package and original signing certificate retained. No changes to enemy HP/speed/damage, wave quota, rewards, prices, crop rules, loot odds, guard range/cadence or saves. Dusk/lighting/audio state derives from existing clocks and never consumes RNG. No strobing or camera shake; reduced-motion/focus/pause behavior remains required QA.

Candidate gates: JVM simulations/audio-envelope regressions; debug/release builds, lint/package/audio checks; full API26/35 native regression with dusk/nightfall/dawn, actual phase checkpoints, dawn pause, character lineup and existing boss/arena/rotation/large-text/recovery cases; retained-key signing and actual release install-over/save retention. Source and signed delivery must both pass before publication. Observed results are retained with the release.

Physical-phone readability, TalkBack, measured performance, audio comfort, install-over and human sense of dread/playtesting remain NOT TESTED. This is a prerelease. Export .btfs before updating; install the original-certificate Release.apk over the existing signed game. Keep the old APK and pre-update save for rollback; Android blocks lower versionCodes.
