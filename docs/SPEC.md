# Compact Farm and Roaming scope · 0.4.0-rc.1

Baseline: published0.3.0-rc.1 (`55006ab`),27 JVM tests and509 installed-app checks per API26/35 emulator passed. Confirmed public destination Denberg28/Beyond-the-Fence; commit/push/release authorized. Package unchanged, versionCode6. Native Java/Canvas, local single player, original vector sprites; no paid assets/service.

## Loop and bounded systems

Plant inside fortress → harvest coins/timber → build walls/mill/market/barracks → hire patrols → survive four-direction waves → reward/expand. Initial80 coins/24 timber; six soil plots, nine after expansion. North/south start tier1; east/west are buildable gaps. Wall tiers0..3 reduce contact damage: gap1.3×, wood1×, stone0.72×, reinforced0.5×. Shared strength starts100; global fence upgrades add50. Constructing/reinforcing a side also repairs up to20.

Wall costs20+12×tier coins and8+6×tier timber. Mill50c/10w, market80c/18w, barracks70c/12w. Mill generates4w per12 active seconds. Market adds floor(base sale/4). Harvest adds2/4/6 timber for turnip/corn/pumpkin. Barracks allows6 guards, recruitment55+25×current guards coins and6 timber. No wages or guard deaths in this iteration.

A mercenary travels clockwise at0.14 sides/s, shoots within0.28 normalized world units every0.9s, damage9+2×weapon tier. Patrol motion, range-based target selection and shot timing are simulation state, not animation-only. Watchtower retains the starter rifle's rules. Max45 enemies; four approach sides and bounded offsets. Preparation max(30,75−5×min(9,player level−1)) active seconds; reinforcement interval max(1.2,4.8−0.4×min(9,player level−1)) seconds. Level is1+floor(XP/50); changing level immediately updates the visible countdown. Wave resolves by75 active seconds. Free recovery keeps farm/buildings/guards/walls. No time catch-up after interruptions.

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

Save schema3 appends up to10 roaming records (type0..3, clockwise progress[0,4), distance[.55,.95]). Daytime roster size is min(10,3+level). Scouts move0.045+0.008×type sides/s on outer rectangular rings and cause no daytime damage. Raid start promotes up toquota scouts at the exact current ring position, clears the roster and waits one reinforcement interval before spawning additional attackers. Dawn/recovery replenish the roster. No extra rewards for conversion. Schema1/2 migrate; schema3 cannot be read by older apps.

Retain a backup before installing; retain a schema1 export before installing. Schema1 migration completes all four walls at min(3,1+old fence upgrade), grants24 timber, no buildings/guards; existing enemies map to the corresponding first three perimeter sides. Current production, patrol, enemy and roaming positions persist in schema3.

Stable remains blocked on handset30min play,20 interruption cycles, measured>=25fps normal mode/p95 input<100ms, TalkBack/200% font usability, and production-certificate upgrade tests. Physical control/HIL is N/A: no external equipment. Simulator tests do not establish subjective fun or field performance.

## Additional acceptance for0.4

R1: Compact farm leaves>=56dp side terrain and>=48dp top/bottom terrain in supported320dp+ layouts; native actions remain separate and>=48dp in both orientations and200% fonts.
R2: Roamers move continuously around corners, remain outside the fence and bounded at10; they cause no daytime damage or rewards.
R3: Starting a raid converts positions exactly, never duplicates units/rewards, respects45-enemy quota and consumes the roaming roster.
R4: At level1,30s of preparation has no raid; at level10,30s starts a raid. Reinforcement interval decreases with level and remains>=1.2s. UI displays level/countdown.
R5: Released schema2 fixture preserves farm/guards/resources; schema3 resumes roaming and raid timing exactly. Invalid/non-finite/oversized roamer state is rejected.
R6: Exact APK native tests show roaming, automatically start the level10 raid, preserve paused rendering/lifecycle/save recovery and capture six views. All previous gates remain.
