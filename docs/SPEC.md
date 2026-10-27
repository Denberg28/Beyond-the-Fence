# 0.11 acceptance — First light and clear controls

Baseline0.10.0-rc.1/2cdb19a3301a8f976a3f1ce8c530b569d4dfb48a:72 JVM cases,1182 native checks per API26/35. Scope follows the latest phone screenshot: terrain under transparent bottom padding, symmetric controls, minimal zombie income, consistent building tab transitions and new morning music.

| Criterion | Evidence / expected result |
| --- | --- |
| U1 Terrain/dock |Bottom containers have transparent backgrounds; original terrain extends under padding and changes at night. Mirrored command/slot dimensions and equal action heights at normal/200% fonts and landscape; native bounds/pixels/screenshots |
| U2 Page transitions |Stable Fence/Mill/Shop/Barracks tabs match map shortcuts/title/selection. Reset new page scroll to0 and remove old actions/price hint. Repeated switching from scrolled bottom; native pointer/rotation tests |
| U3 Small reward |All four zombie variants credit1 coin once; live enemies credit0, replay/reload cannot duplicate;10-million coin cap retained; actual guard kill tests. Max10c Day1/45c later per successful quota, versus12c base turnip sale and existing dawn reward |
| U4 Morning |Original32s First Light replaces sunny asset; night/four effects unchanged. Exact format/duration, deterministic generation, non-silent/unclipped and quiet boundaries; package audit/audio native regression |
| U5 Regression |Schema7 unchanged, all75 JVM plus full Android build/lint/package/API26/35 installed-app suite. Handset listening/endurance/accessibility/performance/signing remain blocked for stable |

## Historical acceptance records

# 0.10 acceptance — Settlements, heroes and supplies

Baseline0.9/29eeea5:63 JVM cases,923 installed-app checks on API26/35. User scope: inline coin requirements, five-day changing maps/fences, expanded upgrades, cinematic scores, spaced transparent controls, consumables with side slots and hero recruitment in building popups.

| Criterion | Required evidence |
| --- | --- |
| E1 Cost feedback |Unaffordable fence/merc upgrades show full coins/timber inside button for1.8s; zero debit, no popup/pause; native real touches |
| E2 Chapters |Farm1–5/Village6–10/Castle11–15/repeat. Trapezoid/chamfer boundary shared by visible fence and actual guard/enemy ranges. Tiles re-order, remain48dp/inside; assets preserved at dawn/save; ChapterTest/native |
| E3 Upgrades |4 Steel reinforcement ranks,3 Mill/Drill/Hero tiers; exact atomic costs, bounded caps, real mitigation/production/combat changes; ChapterTest/native |
| E4 Hero |One paid hero separate from max3 mercenaries; prerequisite Barracks, firing/injury/death/training/save behavior; ChapterTest/native |
| E5 Supplies |Market prerequisite;35c Elixir/45c Enhancer,9-stock cap, left/right slots. Heal only living/injured actors;20 active seconds nonstacking40% damage/20% cadence boost; no paused/background catch-up; ChapterTest/native |
| E6 UI/audio |Transparent controls, additional bottom separation, dedicated popups; original32s day/night arrangements; six distinct unclipped audited WAVs; native/layout/screenshots |
| E7 Compatibility |Schema7 reads1–6, exact continuation/new-state validation and corruption tests; all prior Game/lifecycle/sound gates remain |

Default prices: Steel rank120+60×current rank coins,18+8×rank timber. Mill90+80×tier coins/12+6×tier timber. Drill140+100×tier coins/8 timber. Hero recruit360c/20 timber; training220+150×tier coins/12 timber. No player XP levels return. Physical-device stable gates remain blocked.

## Prior design and verification records

# 0.9 acceptance — Compact compass and building features

Baseline0.8.0-rc.1/3c8251f8124b043863a6091e3e2349b36938cbbb:63 JVM cases and824 native checks/API26 and35. Scope follows the supplied phone screenshot: transparent compass, no persistent instructions/Fix8c label, center selected-side upgrade, building-specific features, Merc ↑ name.

| Criterion | Expected/check |
| --- | --- |
| C1 Compact compass |144dp cross, transparent rest backgrounds,48dp targets, N/W/E/S geometry; selected-side outline and accessibility selected state; actual bitmap alpha/native layout |
| C2 Center ↑ |Initially disabled. Direction tap selects and retains coin repair. Center builds/upgrades only selected side with exact coin/timber debit, no panel/pause. Caps/affordability disable; native pointer transactions |
| C3 Building features |Mill has timber/construction; Shop has market/crop sale/plant/irrigation/land; Camp has construction/crew/rally/patrol/training. Context survives rotation; native pointer/features |
| C4 Minimal text |No visible instruction banner or Fix8c text; Merc +/Merc ↑ labels; prices/health remain accessible, action feedback announced; native visibility/label checks and screenshots |
| C5 Regression |Same schema6,63 JVM cases, full builds/lint/package/audio and API26/35 lifecycle/damage/sound/mute/font/checkpoint gates;12 reviewed scenes per API |

Construction and upgrades keep their existing prices. Direction repairs keep8 coins/up to50HP/0 timber and spend nothing on intact fences. A damaged-side selection can therefore incur the existing8-coin repair before a separate center upgrade; there is no second hidden repair charge in the center action. No selection survives new-farm/import reset. Stable handset/audio/accessibility/performance/identity gates remain blocked.

## Prior design and verification records

# 0.8 acceptance — Quick commands and soundscape

Baseline0.7.0-rc.1/edb4941bc30eca38b5db93e6d3db10b1eefcd332:58 JVM cases,787 native checks/API26 and35. New scope: bottom-center cross, direct coin repairs, direct hire/uniform, daytime/nighttime music and environmental/combat effects.

| Criterion | Expected/check |
| --- | --- |
| Q1 Cross | N above W/E, S below, centered and pinned under the independently scrolling farm; four>=48dp native targets; normal/landscape/200% font native bounds |
| Q2 Immediate repair |8 coins restores up to50HP on chosen built side,0 timber, no panel/pause change; intact/gap/insufficient/defeat spends nothing; all four sides isolated in JVM/native checks |
| Q3 Crew shortcuts |Merc + hires under usual camp/coin/timber/cap rules; Uniform ↑ buys one tier at its shown price; no panel; real-pointer native checks |
| Q4 Phase music |Sunny prep and creepy raid loops switch with actual phase; independently persistent music/effects toggles; native playback/mute/rotation/restart |
| Q5 Sound cues |Actual shots produce one bounded burst, nearby zombies growl, owl/cricket only at night; pause suppresses elapsed/catch-up/stale shots; pure AudioCues and real SoundPool dispatch checks |
| Q6 Lifecycle/package |Pause/panels/focus/Home stop tracks and effects; all players/pool/focus released on destruction;6 distinct audited local WAVs, no permissions,<=5MiB; same required API26/35 gates |

Save schema6 unchanged; shot counter is transient presentation-only state, never a combat input or stored field. New field repairs cost coins alone in both shortcut and Build panel. Gaps/material upgrades still require construction. Physical listening/endurance/TalkBack/performance/production identity remain blocked.

## Historical verification/design records

# Current acceptance — Survival pressure (0.7)

Baseline:0.6.0-rc.1, commit147ebd7a2740b89ed5c9a0ce456b2fa76ec0315f. Reported defect: Day3 zombies could not damage fences. Source diagnosis: guard range.85 covered nearly the whole map, zombie approach took15–40s and75s raid timeout deleted survivors.

| Criterion | Expected result | Check |
| --- | --- | --- |
| S1 Day-driven survival | XP changes no timing/unlocks; days shorten prep75→35s and reinforcements3.2→0.85s | RoamingTest |
| S2 Threat and agency | Same Day3 seed wipes wood/workwear with3 guards; stone/desert kit survives with damage | SurvivalTest |
| S3 Opening economy | Fresh80c/24timber farm can fence gaps, farm, build barracks/hire and survive Day1 with real timed growth | GameTest |
| S4 Real wipeout | Zombies breach, destroy crops/buildings/core and kill nearby crew; defeat freezes simulation and retries same day | DamageTest, SurvivalTest, native breach |
| S5 Squad |3 maximum; uniforms change rendered gear, damage, armor, health/range/cadence; prep heals only survivors | SurvivalTest, native Squad/Gear |
| S6 No despawn | A surviving zombie persists past75s and save/reload; dawn requires full quota defeated | SurvivalTest |
| S7 UI | No levels/hearts/towers; bars/HP and distinct zombie silhouettes retained; background woodland/tracks/debris; native48dp controls, rotation/200% font | native scene/pixel/layout, reviewed screenshots |
| S8 Audio | Original local loop plays only active raids; mute persists; focus/Home/pause/panels stop audio, release on destruction | native playback/lifecycle; handset listening BLOCKED |
| S9 Migration | v1–5 read; >3 squad refund once; merc health/casualties deterministic v6 roundtrip; corruption rejected | fixtures, JVM/native checkpoints |

The old AFK/no-spend first-night and20-day invulnerability expectations are superseded by S2–S4. Recovery solvency is checked across20 defeats. No existing economy/persistence/lifecycle gate is waived.

## Fences and Mercenaries scope · 0.6.0-rc.1

Baseline: published0.5.0-rc.1 (`b4d5cfb`),43 JVM tests and660 installed-app checks per API26/35 emulator passed. Confirmed public destination Denberg28/Beyond-the-Fence; commit/push/release authorized. Package unchanged, versionCode8. Native Java/Canvas, local single player, original vector sprites; no paid assets/service.

## Loop and bounded systems

Plant inside fortress → harvest coins/timber → build walls/mill/market/barracks → hire patrols → survive four-direction waves → reward/expand. Initial80 coins/24 timber; six soil plots, nine after expansion. North/south start tier1; east/west are buildable gaps. Wall tiers0..3 reduce contact damage: gap1.3×, wood1×, stone0.72×, reinforced0.5×. The separate homestead core starts100; global fortify upgrades add50. Individual fence health is80+40×(tier−1)+15×fortify for built sides. Gaps have0 health. Constructing/reinforcing a side also repairs up to20.

Wall costs20+12×tier coins and8+6×tier timber. Mill50c/10w, market80c/18w, barracks70c/12w. Mill generates4w per12 active seconds. Market adds floor(base sale/4). Harvest adds2/4/6 timber for turnip/corn/pumpkin. Barracks allows6 guards, recruitment55+25×current guards coins and6 timber. No wages or guard deaths in this iteration.

A mercenary travels clockwise at0.14 sides/s, or uses shortest perimeter travel at0.35 sides/s when rallied. Range0.85 normalized units, damage16+4×training, interval max(0.5,0.85−0.06×training) seconds. New farms start with one volunteer. Patrol motion, range-based target selection and shot timing are simulation state, not animation-only. Mercenaries alone reduce enemy HP; structures and rally commands cause no damage. Max45 enemies; four approach sides and bounded offsets. Preparation max(30,75−5×min(9,player level−1)) active seconds; reinforcement interval max(1.2,4.8−0.4×min(9,player level−1)) seconds. Level is1+floor(XP/50); changing level immediately updates the visible countdown. Wave resolves by75 active seconds. Free recovery retains surviving assets, guards, gear and wall investments, but ruined assets stay lost. Invested fence health is restored; seed aid brings coins to12 if lower. No time catch-up after interruptions.

## Required acceptance

| ID | Result | Evidence |
|---|---|---|
| F1 | All farm/building action bounds are inside the fence;16 native map targets remain separate and >=48dp | Geometry suite; API26/35 native bounds/labels |
| F2 | Explicit wall/building/recruit purchase debits both resources atomically, respects limits and cannot duplicate | FortressTest; native pointer flows |
| F3 | Crops produce coins/timber; mill timing and market sales agree with rules; no background simulation | JVM + Android lifecycle |
| F4 | Guards move around the perimeter, damage nearby targets and do not damage distant ones; all four attack sides appear | Spatial simulation tests; screenshots |
| F5 | Save2 exactly resumes new timing/units; schema1 fixture migrates preserving progression; corruption/fuzz rejection remains | Codec/economy suites + atomic checkpoint instrumentation |
| F6 | Native seed/harvest/wall/building/hire/panel/rotation/font flows pass on actual installed APKs | API26/35 instrumentation and screenshots |
| F7 | Build/lint/identity/signature/checksums/source provenance and gated GitHub prerelease pass | CI artifacts |

Save schema3 introduced up to10 roaming records (type0..3, clockwise progress[0,4), distance[.55,.95]). Daytime roster size is min(10,3+level). Scouts move0.045+0.008×type sides/s on outer rectangular rings and cause no daytime damage. Raid start promotes up toquota scouts at the exact current ring position, clears the roster and waits one reinforcement interval before spawning additional attackers. Dawn/recovery replenish the roster. No extra rewards for conversion. Schema1/2/3 migrate to schema4; new saves cannot be read by older apps.

Retain a backup before installing; retain a schema1 export before installing. Schema1 migration completes all four walls at min(3,1+old fence upgrade), grants24 timber, no buildings/guards; existing enemies map to the corresponding first three perimeter sides. Current production, patrol, enemy and roaming positions persist in schema4.

Stable remains blocked on handset30min play,20 interruption cycles, measured>=25fps normal mode/p95 input<100ms, TalkBack/200% font usability, and production-certificate upgrade tests. Physical control/HIL is N/A: no external equipment. Simulator tests do not establish subjective fun or field performance.

## Additional acceptance for0.4

R1: Compact farm leaves>=56dp side terrain and>=48dp top/bottom terrain in supported320dp+ layouts; native actions remain separate and>=48dp in both orientations and200% fonts.
R2: Roamers move continuously around corners, remain outside the fence and bounded at10; they cause no daytime damage or rewards.
R3: Starting a raid converts positions exactly, never duplicates units/rewards, respects45-enemy quota and consumes the roaming roster.
R4: At level1,30s of preparation has no raid; at level10,30s starts a raid. Reinforcement interval decreases with level and remains>=1.2s. UI displays level/countdown.
R5: Released schema2 fixture preserves farm/guards/resources; schema3 resumes roaming and raid timing exactly. Invalid/non-finite/oversized roamer state is rejected.
R6: Exact APK native tests show roaming, automatically start the level10 raid, preserve paused rendering/lifecycle/save recovery and capture six views. All previous gates remain.

## Damage and economy acceptance for0.5

D1: Only the attacked fence loses health until breached. Damage6HP/s (brute10), tier2/3 multipliers0.72/0.5, relaxed0.5. Spitters attack fences at distance0.26, then advance through a broken border. Each built side at0HP retains its tier/investment but allows entry.
D2: Intruders advance inward at0.55/s, then hit every1.2s for10 damage (runner8, brute18; relaxed0.5). They target side-ordered live crops, then live buildings, then homestead. Existing target−1 stays on the core; lost asset targets are revalidated before damage.
D3: Crops40HP/buildings100HP. Destroying a crop clears type/growth/HP, marks scorched soil and awards no sale/timber/XP. Destroyed buildings become ruins, disable benefits, and can be rebuilt at original cost; mill production clock resets. Existing hired guards survive barracks loss, but recruiting remains blocked.
D4: Timber bundle10w/12c; fence repair+50HP/8c/4w; building repair+60HP/10c/5w. Costs are atomic, caps/no-op checks reject without spending, and repaired walls cannot eject an existing intruder. Normal harvest must fund material purchase, defense construction/repair and recruitment.
D5: Core defeat stops ticks. Recovery restores core/invested wall health, retains ruins and lost crops, keeps remaining upgrades/guards, applies the existing coin penalty and brings coins to12 if lower. Replanting charges normal seed cost and resets soil damage.
D6: Schema4 appends fence/crop/building health, scorched/ruin flags, loss counters and per-enemy breached/advance/cooldown/target. Counts/ranges/finite values/asset relationships are checked. Schema1–3 receive intact independent health without changes to old resources/progression; schema4 supports surviving mercenaries with a ruined barracks.
D7: JVM tests cover real damage/breach/loss and no duplicate credit, repair/entry isolation, material/recruitment economy, destroyed building benefits, recovery, released schema3 migration, deterministic breach continuation and invalid fields. Exact APK tests drive an actual raid frame loop to breach/destroy, then pointer-replant/harvest/buy timber/repair/hire with precise resource debits and breach/rebuilt screenshots. All prior release gates remain.

## Required0.6 acceptance

M1: Remove all tower rendering, structure-origin firing, instant focus damage and watchtower UI. No mercenaries means no enemy damage even with every upgrade/structure; guards fire only in raids and within their range.
M2: Wood/Stone/Steel retain80/120/160 base HP and1/0.72/0.5 damage multipliers, plus15HP per fortify tier. Coin price buys the material kit; timber is framing. Each border has three vector hearts from clamp(HP/maxHP,0,1), including partial hearts; full/half/zero life must produce decreasing filled pixel area.
M3: Rally sets side0..3 and moves guards along the shortest perimeter path, spreading multiple guards across the side. Patrol sets−1 and resumes clockwise travel. Orders do not inflict damage or reset cooldowns. Training0..5 costs80+100×tier coins and benefits all guards.
M4: One volunteer starts a new farm and one is supplied on recovery only if no guards remain. First raid and zero-coin gameplay must remain viable. The20-day strategy now builds barracks and hires guards as well as training/fortifying/farming.
M5: Schema5 retains the v4 record layout with a changed patrol/rally field domain and training interpretation. Schemas1–4 preserve assets/damage/resources, reset obsolete focus orders, and grant one volunteer only to an empty squad. Reloading schema5 never duplicates volunteers. Legacy firing-clock bytes remain unused compatibility placeholders.
M6: Walkers/runners/brutes/spitters have visibly different original silhouettes and details; animated gait derives from simulation, freezes while paused, and is disabled by reduced motion. A24dp world inset prevents larger sprites/health bars clipping.
M7: Native exact-APK checks retain breach/replant/harvest/material/repair/hire flows, add Wood→Stone→Steel purchases and non-damaging rally/patrol assertions, pixel-check hearts, and capture the material/variant scene. All build/lint/codec/lifecycle/API26/API35 gates remain blocking.
