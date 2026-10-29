# 0.16.1-rc.1 — Right-side pond spacing

29 October 2026 (Asia/Manila). Based on published 0.16.0-rc.1, commit 307d78c.

Move the fishing pond to the right end below the south fence. Leave 16dp from the fence baseline and at least 12dp above the Plant button when the pond is visible. Artwork and the 56×48dp native touch target share the same position. Landscape reserves 8dp more minimum board height so the pond keeps its full touch target and fence gap in short windows. Command positions and farm layout rules are retained.

Only pond placement changes. Fishing timing, catch rewards/cap, planting, enemies, economy and save schema 12 remain unchanged. VersionCode 22 and the retained signing identity support install-over updates from 0.12+ same-key builds.

Export a `.btfs` backup before updating. Install Release.apk on Android 8.0+. Keep the previous APK and pre-update backup for rollback; older disposable-key builds require save transfer.

Required candidate checks: both JVM variants, Android builds/lint/package/audio audits, API26/35 native portrait/landscape/200% font and all settlement layouts, actual pond touch/fishing, visible pond-to-Plant spacing, retained certificate/exact compiled-content verification and install-over checkpoint retention. Results are recorded in docs/TESTING.md and release evidence. Physical-phone endurance, accessibility and human playtesting remain NOT TESTED; this is a prerelease.

---

# 0.16.0-rc.1 — Quick planting and a separate pond

Published 29 October 2026: https://github.com/Denberg28/Beyond-the-Fence/releases/tag/v0.16.0-rc.1 . All automated candidate and signed-upgrade gates passed.

Plant now behaves like Harvest: one tap fills all affordable empty unlocked beds with the selected seed, without a chooser, toast or pause. It preserves growing/ripe crops and debits only planted seeds. Individual beds still support planting and harvesting. Seasonal/day requirements and the six-bed starting farm plus 200-coin expansion remain unchanged.

Swipe down to hide Seeds, the bottom command buttons, construction/equipment panels and ordinary popups. Show controls restores the command dock. Horizontal seed scrolling and vertical content scrolling retain priority; scroll a panel to the top before pulling it down. Buttons and Close/Back remain available. Required recovery/progression choices and arena combat retain their explicit actions.

Fishing now lives in a separate visible pond below the south fence. All nine farm positions draw ordinary soil; the bottom three open through normal Farm expansion. The three-catch daily reward cap, timed reel, separate randomness and equipment odds are unchanged. Swiping the fishing popup closes it, cancels the cast and keeps the farm paused.

Wolves stay upright rather than rotating a side-view drawing through 360 degrees. They mirror toward travel/targets, and diagonal pairs of articulated legs animate a trot. Pause and Reduced motion retain static poses. No enemy stats or damage change.

Save schema 12 reads schemas 1–11. A schema11 farm that has crops in former floating beds, or has earned its three-catch quota, retains those beds as permanent ordinary farmland on upgrade. Crop positions, growth, health, resources, equipment and progression remain intact. New fishing catches never unlock farmland. Older apps reject schema12 exports: export before updating and keep the previous APK and pre-update backup for rollback.

Published checks: 117 JVM cases per build variant; builds, lint and package/audio/signature audits; 2,305 installed-app checks on each API26/35; visual wolf/pond self-review; same-key preview and non-debuggable release upgrades with checkpoint retention. Physical-phone performance, accessibility, endurance and human balance validation remain pending. This is a prerelease.

---

## 0.15.0-rc.1 — 29 October 2026 (published prerelease)

Published: https://github.com/Denberg28/Beyond-the-Fence/releases/tag/v0.15.0-rc.1 . Signed delivery run40783160472 passed116 tests per variant,2231 installed checks and both4-check upgrade stages on each API26/35. Retains package/signing identity; physical-phone validation remains pending.

- Plant shortcut: one, exact selection, or all affordable empty beds; uniforms stay in Crew.
- Entire bottom water row converts to three floating beds after the daily fishing quota, retaining crops across dawn. Expansion grants permanent access; old crops stay intact.
- Loss-free ripple timing, farm pause, three saved daily reward catches,6–10 coin fish and scarce low-tier equipment; unlimited reward-free practice.
- Six new crops with day unlocks, ten-day seasons and rare environmental planting gates; original economy and new yield ceilings tested.
- Shorter welcome/help and gentle farming/breather messages; schema 11 migration and regression coverage.

# 0.14.0-rc.2 — Medieval frontier and fantasy bestiary (published prerelease)

Published 29 October 2026 (Asia/Manila): https://github.com/Denberg28/Beyond-the-Fence/releases/tag/v0.14.0-rc.2 . Delivery run 40737650198 passed 106 cases per variant,1974 native cases plus both 4-check upgrade stages on each API 26/35. Signed Release.apk retains the pinned certificate. Physical-phone/human balance validation remains pending.

- Raised timber-edged kitchen gardens, masonry cornerstones and furrows replace flat plots. Palisade / Stone wall / Ironbound tiers gain pointed stakes, coursed masonry, crenellations and iron bands; HP bars, costs, boundaries and touch targets remain.
- Add Kobold skirmishers (Day 2), Dire wolves (Day 3), Orc raiders (Day 6) and Goblin hexers (Day 12) alongside all undead. Distinct silhouettes, stats and weighted reinforcement selection; hexers attack from outside intact walls. Boss ID 7, quotas, salvage and relic odds remain. Ashfall also strengthens Orc damage 20%.
- Illustrate the arena vanguard and all four champions with original armor, capes, swords/shields, bow/quiver, bone anatomy and witch staff/orbs. Stone gate, heraldry, perspective floor and clock-driven attack cues; reduced motion keeps poses static. Portraits preserve aspect ratio.
- Illustrated Bestiary is available from Crew and Options. Help now uses the focused tab names.
- Schema 10 reads 1–9; a prior schema 9 active-raid fixture protects migration. VersionCode 19, same package/certificate pin, no new permission/dependency. Prior apps cannot read schema 10; retain a pre-update export.

# 0.14.0-rc.1 — Focused building and Ashen March (development)

- Reorder build tabs Fence→Farm→Mill→Crew→Supplies→Equipment; group actions by purpose and move fortification to Fence, materials to Mill and consumables/assignments to Supplies.
- Shared timber frame, parchment surface, gold trim and serif section headings align popups with the fantasy setting.
- First authored World 2 slice: Ashen March scenery, four chapter names, ashfall tactics and Cinder Regent boss variant; portal retains relics/settings and shows a paused arrival.
- Save schema 9/package identity/signing pin unchanged. VersionCode 18. No new dependency/permission. Worlds 3+ currently reuse Ashen March.
- Candidate work; do not infer release or device-validation status from this entry.

# 0.13.0-rc.2 — TAG fantasy survival release

Publish the renamed game with interchangeable potion/magic buttons, nine equipment slots/five rarities/Equip best, optional tenth-day champion arenas, bounded offline director and reincarnation/Day 100 portal lore. Fixed action areas keep adventure choices above system bars, including at 200% font. Deferred welcome does not block invitations. VersionCode 17, schema 9, retained signing identity.93 unit tests per variant,1485 native checks and both actual update stages per API 26/35 passed; physical-phone validation remains pending.

# 0.13.0-rc.1 — Local development build

Equipment collection/nine slots/equip best, interchangeable quick assignments, optional tenth-day champion arenas, bounded offline director, reincarnation welcome and Day 100 new-world portal. Schema 9 reads 1–8; package/certificate retained, versionCode 16. See docs/RELEASE-NOTES.md for behavior and limits.

# Changelog

## 0.12.0-rc.1 — A century beyond the fence

- Slower, intermittently dormant daylight scouts; individual night shuffling rhythms and three later enemy variants.
- Grave Warden boss every fifth night; persistent one-per-raid state, distinctive art and a real health bar. Bounded progression/upgrades through Day 100 and beyond.
- Arc bolt/Starfall single-target/area potions. Three assignable quick slots; upper-left replaces Merc +, recruitment stays in Barracks. Hold or tap empty slots to assign owned inventory.
- New original Bard at Dawn lute/recorder music and The Hunt night score. Symmetric full-width HUD rows and larger survived-day heading.
- Schema 8 migrates 1–7, retaining resources/damage/hero/crops and adding empty attack inventory/default slots. Bosses do not replay after reload.
- Persistent private signing identity begins here; verified payload/source/certificate pipeline plus same-key install-r progress-retention tests. Earlier disposable-key previews still require one-time export/import.

## 0.11.1-rc.1 — Compact caption fit

- Screenshot review of 0.11.0 caught awkward Harvest/Barracks word wrapping at 200% font. Keep short captions whole with bounded effective text sizes, retaining full system scaling for panel body/purchase text. Added native caption-fit checks; all 0.11 features retained.

## 0.11.0-rc.1 — First light and clear controls

- Transparent bottom command/action padding reveals original terrain, changing with day/night. Mirror-sized mercenary commands and item slots, aligned bottom actions and consistent gaps replace uneven button heights.
- One Fence / Mill / Shop / Barracks tab strip maps directly to building pages. Switches clear stale action/price content, reset scroll to top and keep title/selection synchronized. Map shortcuts and rotation use the same page state.
- Zombie salvage is 1 coin per kill (previously 2), once per removed enemy, without variant multipliers. Existing harvest/dawn income and prices remain.
- New original 32 s “First Light” morning arrangement: felt keys, mallet arpeggios, warm bass and breathy lead. Night track/effects and audio lifecycle unchanged.
- Schema 7 unchanged;0.10 saves/imports remain compatible. Added actual kill/reload/cap tests and native tab/scroll/symmetry/backdrop checks.

## 0.10.0-rc.1 — Settlements, heroes and supplies

- Unaffordable upgrade taps briefly show required coins/timber inside the same button; no panel, pause or charge.
- Five-day Farm → Village → Castle chapters change the physical perimeter, interior placement and scenery while retaining assets. Renderer and combat share geometry.
- Added 4 Steel reinforcement ranks,3 mill tiers,3 mercenary drill tiers and 3 hero training tiers. Barracks recruits 1 hero separately from the 3 mercenary cap.
- Shop sells Elixirs and timed Enhancers, with stocked left/right bottle slots and bounded inventory/effect timers.
- Transparent crew controls and additional separation above bottom actions. Dedicated building pages include the new related purchases.
- Original 32-second day/night scores with richer melodic/harmonic/percussion layers. Existing effects and mute/lifecycle rules retained.
- Schema 7 migrates schemas 1–6 and persists all new inventory/hero/upgrade/effect state; new saves cannot downgrade to 0.9.

## 0.9.0-rc.1 — Compact compass and building features

- Transparent N/W/E/S buttons in a smaller 144 dp cross, retaining 48 dp targets and a selected-side outline.
- Removed the persistent instruction banner and Fix 8 c label; replaced the center with ↑ to build/upgrade the selected fence without a panel or pause. No selection/insufficient funds/max tier disables the action. Direction taps retain coin repairs.
- Mill opens timber features; Shop opens market/crop sales/planting/farm upgrades; Camp opens construction, recruitment/rally/patrol/mercenary upgrades. Building context survives rotation.
- Renamed Uniform ↑ to Merc ↑ and shortened both crew shortcut labels. Accessibility descriptions retain prices and health.
- Save schema 6 and all existing soundscape/survival behavior retained. Added installed-APK pixel, context, selection/upgrade and Shop transaction checks.

## 0.8.0-rc.1 — Quick commands and living soundscape

- Moved N/W/E/S into a pinned bottom-center compass cross. Tap to repair 50 HP for 8 coins without a panel, pause or timber; intact sides never charge.
- Added direct Merc + hiring and Uniform ↑ upgrades beside the cross; normal prerequisites, prices and 3-person cap remain.
- Replaced the raid-only action loop with sunny daytime and creepy nighttime music. Added actual guard-shot sounds, zombie growls, owl hoots and crickets.
- Independent persistent music/effect toggles; bounded six-voice SoundPool, no background/focus-loss playback or replay of stale shots.
- Save schema 6 unchanged and compatible with 0.7. Expanded geometry, coin-only repair, sound scheduling and native direct-control/audio tests.

## 0.7.0-rc.1 — Survival pressure

- Removed player levels/XP progression and all fence hearts; day controls escalation and crop availability.
- Fixed map-wide guard coverage: short-range three-person crew, with six visible uniform tiers, armor, injury, death and preparation healing.
- Faster, stronger zombie variants and denser daily hordes. Surviving enemies no longer vanish at 75 s; defeat retries the same day.
- Added original procedural woodland/road/debris scenery and a generated 120 BPM action score with persistent mute and lifecycle/audio-focus handling.
- Save schema 6 migrates old squads with one-time excess-hire refunds; preserves damage and investments.
- Added deterministic Day 3 breach/wipeout versus upgraded survival, Day 12 threat, casualty, healing, migration and native audio/cap/uniform checks.

## 0.6.0-rc.1 · 27 October 2026

- Remove towers, structure auto-fire and instant focus shots. Mercenaries are the only attackers; rally physically moves them to a border, and Patrol releases the order.
- Start with one volunteer guard; keep first-raid/zero-coin recovery viable. Repurpose rifle upgrades into training for every mercenary and rebalance guard damage/range/cadence. Existing squads survive migration; guardless old farms receive one volunteer once.
- Name Wood/Stone/Steel material upgrades and draw different fence textures. Show three filled/partial/empty heart icons beside each fence, driven by actual remaining HP.
- Redesign all four zombie types with larger distinct silhouettes, eyes/teeth, torn clothes, runner limbs, brute armor and spitter belly/drool. Animate from simulation time, with pause/reduced-motion behavior.
- Save schema 5 preserves rally orders, training, damage, crops and economy; schemas 1–4 migrate without duplicating volunteers or erasing asset losses.
- Add no-hidden-shooter, volunteer survival, actual training shots, material/hearts, rally continuity and released-schema 4 migration tests. Native checks verify heart pixels follow full/half/zero life, material purchases, non-damaging rally and the zombie/material scene.

## 0.5.0-rc.1 · 27 October 2026

- Give each fence independent health, damage bars/cracks and visible breaches. Zombies enter broken or unbuilt borders; repaired fences stop new entrants while existing intruders keep fighting.
- Let intruders destroy crops and buildings. Ruined crops give no harvest rewards and can be replanted; ruined buildings lose their benefit and require rebuilding. Existing mercenaries survive a destroyed barracks, but hiring stops until rebuilt.
- Add timber bundles (10 timber/12 coins), side repairs (+50 HP/8 coins/4 timber) and building repairs (+60 HP/10 coins/5 timber). Harvest money/materials pay for defenses and recruitment.
- Avoid repeated native text layout when active labels have not changed, especially at large fonts on older Android.
- Add damage/ruin indicators, per-side health labels, repair/rebuild actions and attack hit flashes. Keep the compact farm, roaming scouts, level-based raids and paused construction controls.
- Recovery restores homestead/invested fences and provides a minimum 12 seed coins if needed; destroyed crops/buildings stay lost.
- Save schema 4 preserves damage, ruins, losses and intruder progress; migrate schemas 1–3 without removing previous progress. Add damage/economy/migration regressions and installed-app breach→replant→harvest→materials→repair→hire checks.

## 0.4.0-rc.1 · 27 October 2026

- Make the central farm compact, with visible roaming terrain on all four sides. Keep all 16 native map actions at least 48 dp; adapt to orientation/fonts and inset sprites from screen edges.
- Add 4–10 persistent wandering zombies outside the fence during preparation. They become real attackers at their current positions when a raid starts, counted against the existing quota.
- Scale preparation from 75 s at player level 1 to 30 s at level 10+, and reinforcement spacing from 4.8 s to 1.2 s. Show player level and Raid in countdown.
- Save schema 3 preserves roaming positions and migrates schema 1/2 farms. Keep a pre-upgrade backup for rollback.
- Add roaming, frequency, conversion, corner continuity, migration, deterministic continuation and native automatic-raid checks. Existing build/lint/package/API 26/API 35 gates remain blocking.

## 0.3.0-rc.1 · 27 October 2026

- Replace split farm/defense lanes with a top-down fortress: enclosed interior farming and building sites, four buildable/upgradeable borders, corner towers and original arcade-style sprites.
- Add timber harvests, a producing timber mill, a market bonus and barracks. Explicit two-resource construction purchases do not affect crops or occupied plots.
- Recruit up to 6 persistent blue mercenaries. Their actual perimeter positions determine which nearby zombies they can shoot; four-direction waves use spatial approach offsets.
- Add Fence/Farm/Squad/Gear panels, compact seed picker and native map actions with shared geometry and >=48 dp targets.
- Save schema 2 persists construction, production and patrol combat; schema 1 migration preserves old progression. Export a schema 1 backup before upgrading because new saves do not downgrade to 0.2.
- Stop refreshing and redrawing static paused scenes, reducing idle work and avoiding Android 8 input starvation in construction panels. Bound native-test idle waits to 10 seconds and verify paused scenes stay static.
- Extend economy, spatial-combat, migration, bounds and installed-app fortress interaction checks. Candidate gates/physical-phone and test-signing limitations remain.

## 0.2.0-rc.2 · 27 October 2026

- Fix rain/fog Paint alpha carrying into the next scene background; retain readable, opaque daytime scenery.
- Improve disabled seed/upgrade/repair text contrast against both light and dark control backgrounds.
- Add installed-app two-frame rain/fog opacity regression checks. rc.1 passed 19 JVM tests and 278 checks on each API 26/35 emulator; screenshot review identified this visual follow-up.

## 0.2.0-rc.1 · 27 October 2026

- Replace the separate form-style panels with one immersive game window: illustrated seed cards above the world, directly actionable soil and defense lanes, and a compact bottom dock.
- Show crop growth/harvest rewards on the actual tiles, highlight selected seeds and lanes, and open land upgrades from locked soil.
- Put repair on the fence badge; open upgrades as a paused overlay with an explicit close control. Start night deliberately starts play; rebuild returns paused.
- Adapt the board to portrait/landscape, keep native accessible 48 dp targets, retain seed/overlay after rotation, and allow scrolling at large font sizes.
- Add shared artwork/tile geometry tests, real pointer-input checks, portrait/landscape and 200% font checks and five screenshot views on API 26/35.
- Package/schema 1 and economy/combat rules remain compatible with rc.2; test signing and physical-phone limitations remain.

## 0.1.0-rc.2 · 27 October 2026

- Pause immediately when a system overlay removes window focus; returning focus never resumes play automatically.
- Await the completed Home command and observed UI state in emulator lifecycle checks; collect activity, screenshot and crash evidence even on failure.

- Bulk plant affordable empty plots without replacing crops; show exact count and total seed cost before tapping.
- Show weather/irrigation-adjusted harvest countdowns, ready-crop counts, XP toward the next level, upgrade benefits/coin shortfalls, and threats per lane.
- Retain the selected gameplay tab and seed after activity recreation. Save schema 1 is unchanged.
- Repair the emulator welcome-screen race: initialize accessibility before launch, wait up to 10 seconds for the real welcome action, and fail explicitly if unavailable. Exercise bulk planting/selling and rotation context through native controls.
- Previous source `975 d 790` passed build/lint/unit checks but failed both emulator jobs on a null accessibility window. No rc.1 release was published. rc.2 publication remains gated on the complete checks.

## 0.1.0-rc.1 · 26 October 2026

First Android test candidate: offline farming, crop sales/XP, building and rifle upgrades, three-lane night defense, four zombie varieties, weather, free fence recovery, help, relaxed mode, reduced motion, automatic dual checkpoints and save backup import/export. Original Canvas farm artwork and native accessible controls. Reproducible Gradle build and gated GitHub prerelease workflow. Device acceptance and production signing remain required before stable promotion.
