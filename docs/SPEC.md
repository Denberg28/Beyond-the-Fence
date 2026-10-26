# MVP specification · 0.1.0-rc.2

## Scope and assumptions

Confirmed destination: public `Denberg28/Beyond-the-Fence`, default branch main. Initial remote was empty. Push, commit and public release are authorized. New Android package `com.denberg.beyondthefence`, version code 2. Casual portrait-first sessions; layout can recreate on rotation. Native UI, original drawn art, no purchased assets/services and no game engine. Local-only single player; no dependency on TeleRC or other projects.

## User flow

Welcome → choose seeds → plant empty plots → grow during active time → harvest/sell → unlock crops with XP → buy buildings/weapon upgrades → defend three lanes at night → receive dawn reward → repeat. Fence failure → explicit free rebuild → new day with retained investment. Pause/options is accessible throughout. No game over purchases, waiting-for-energy requirement or mandatory manual aiming.

## MVP / non-goals

Includes 3 crops, 9 potential plots, 4 zombie types, 4 weather states, 6 weapon tiers, 5 fence tiers, 4 irrigation tiers, land expansion, level/XP progression, automatic combat with lane focus, repair, recovery, pause/resume, reduced motion, relaxed difficulty, versioned local saves, backup import/export and help. Maximum enemy population 45. Day duration 75s; night spawns across ~34s and resolves by 75s. Dawn clears surviving enemies at the time limit; surviving the fence is the objective.

Not included: free-roam world, multiplayer, crafting trees, character inventory, sound/music, monetization, cloud sync, push notifications, real-time/offline crop growth, controller support, Play Store publication, production signing or a claim of stable device readiness.

## Acceptance criteria

| ID | Required result | Check |
|---|---|---|
| A1 | Install/launch on supported API; no runtime permissions or network capability | Package audit; emulator API26/35 |
| A2 | Plant, grow, harvest once; correct seed debit/sale credit; no duplicate harvest | Unit + native UI |
| A3 | Upgrade only if affordable; balance never negative; tier limits respected | Unit |
| A4 | First night winnable with starting rifle and zero coins; autonomous harvest/upgrade strategy survives 20 days | Deterministic simulation |
| A5 | Four zombie variants occur; rain/heat/irrigation alter growth as specified | Unit |
| A6 | Defeat stops simulation; free recovery retains crops/upgrades and bounds loss | Unit + UI |
| A7 | Background/focus loss pauses; explicit Play required; no frame-stall catch-up damage | Unit + lifecycle instrumentation + device procedure |
| A8 | Valid save reproduces deterministic continuation; malformed, truncated, oversized or non-finite data rejected; old checkpoint recovered | Unit + device instrumentation |
| A9 | Farm/build/defend actions have native accessible labels; touch targets >=48dp; controls usable at 200% font | Source review + physical device |
| A10 | Preview APK <=5MiB, correct package/version, signature verifies, source/config/checksums included | Packaging/CI |
| A11 | Real phone: 30 min play, 20 background/resume cycles, interruption/rotation, local backup round trip; no crashes/data loss; >=25fps normal rendering, UI input p95 <100ms, no unbounded resource growth | Required before stable; BLOCKED without phone |

Balances are initial tuning values, not player-study results. Fun and long-term retention need playtesting. Starting economy: 80 coins; turnip cost/sale 4/12 (16s), corn 10/26 (29s), pumpkin 18/44 (43s). Crop harvest grants 4 XP, kills 3 XP, dawn 20 XP; level increases every 50 XP. Weapon costs 80+100/tier, fence 70+90/tier, irrigation 100+100/tier, expansion 200. Repair costs 8 coins for 40 health. Night reward: 20+3×day, capped day30. No finite resource can permanently lock the core loop.

## rc.2 focused scope and acceptance

Baseline: `975d790` (build/unit/lint passed; Android26/35 smoke failed on accessibility-window readiness; no published APK). No economy price, combat damage, schema or signing-identity change. Scope: reduce repetitive planting and clarify progression/readiness.

| ID | Pass condition | Check |
|---|---|---|
| Q1 | Bulk planting fills only empty unlocked plots up to the current budget; insufficient funds, locked seeds and defeat spend nothing | GameTest + native bulk flow |
| Q2 | Countdown uses the simulation weather/irrigation rate; ready count and bulk sale agree exactly once; schema1 continuation remains valid | GameTest |
| Q3 | Farm shows cost/count, harvest timing and XP; Build shows benefits/shortfalls; Defend shows per-lane threats; rotation retains current tab/seed | Compile/lint + native recreation + screenshots; physical accessibility still blocked |
| Q4 | Welcome action becomes available within10s; test failures block publication; final APK installs on API26/35 and passes lifecycle/checkpoint checks | Native instrumentation in CI |

Recovery and rollback: export schema1 `.btfs` before installing; retain any installed preview APK. Candidate uses debug test signing, so fresh CI certificates may require backup/uninstall/install/import. Keep rc.2 as prerelease until handset and signing gates pass.

## 0.2.0-rc.1 in-world UI scope

Baseline: published rc.2 at `545a5bb` passed18 JVM tests and28 checks on each API26/35 emulator. No gameplay/economy/save schema change is required for this UI update. VersionCode3, versionName0.2.0-rc.1.

| ID | Pass condition | Verification |
|---|---|---|
| U1 | Seed cards change the crop; pointer tap on actual soil plants/debits once; early harvest is rejected; ripe tile sells exactly once | Native injected pointer checks + unchanged economy suite |
| U2 | Plot/lane button bounds match the rendered world, do not overlap, stay inside the board and remain >=48dp | Shared geometry suite across densities/fonts/orientations + native bounds |
| U3 | Locked soil opens paused upgrades without debit; explicit upgrade purchase; closing stays paused; selection/overlay survive rotation | Native pointer/recreation checks |
| U4 | Tap world lane to focus; repair badge shares the existing repair rule; Start night explicitly starts play, rebuild returns paused | Native + unchanged simulation suite |
| U5 | Portrait and landscape remain usable; 200% font keeps essential actions reachable using scroll; existing background/focus pause and checkpoint recovery pass | API26/35 screenshots and native checks; physical TalkBack/performance still blocked |

Board: portrait stacks three defense lanes over nine soil tiles; landscape places farmland left of defense. Seed cards/HUD/dock are inside the same game window. No swipe/drag planting, plant-based weapons, copied artwork, prices or combat-rule changes. Rollback: rc.2 APK plus exported schema1 backup; downgrade/signing conflicts can require reinstall.
