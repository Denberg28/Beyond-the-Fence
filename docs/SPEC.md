# Farm Fortress scope · 0.3.0-rc.1

Baseline: published0.2.0-rc.2 (`66d3839`),19 JVM tests and280 checks per API26/35 emulator passed. Confirmed public destination Denberg28/Beyond-the-Fence; commit/push/release authorized. Package unchanged, versionCode5. Native Java/Canvas, local single player, original vector sprites; no paid assets/service.

## Loop and bounded systems

Plant inside fortress → harvest coins/timber → build walls/mill/market/barracks → hire patrols → survive four-direction waves → reward/expand. Initial80 coins/24 timber; six soil plots, nine after expansion. North/south start tier1; east/west are buildable gaps. Wall tiers0..3 reduce contact damage: gap1.3×, wood1×, stone0.72×, reinforced0.5×. Shared strength starts100; global fence upgrades add50. Constructing/reinforcing a side also repairs up to20.

Wall costs20+12×tier coins and8+6×tier timber. Mill50c/10w, market80c/18w, barracks70c/12w. Mill generates4w per12 active seconds. Market adds floor(base sale/4). Harvest adds2/4/6 timber for turnip/corn/pumpkin. Barracks allows6 guards, recruitment55+25×current guards coins and6 timber. No wages or guard deaths in this iteration.

A mercenary travels clockwise at0.14 sides/s, shoots within0.28 normalized world units every0.9s, damage9+2×weapon tier. Patrol motion, range-based target selection and shot timing are simulation state, not animation-only. Watchtower retains the starter rifle's rules. Max45 enemies; four approach sides and bounded offsets. Day75 active seconds; wave resolves by75. Free recovery keeps farm/buildings/guards/walls. No time catch-up after interruptions.

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

Save schema2 is one-way relative to0.2; retain a schema1 export before installing. Schema1 migration completes all four walls at min(3,1+old fence upgrade), grants24 timber, no buildings/guards; existing enemies map to the corresponding first three perimeter sides. Current production and positions persist in schema2.

Stable remains blocked on handset30min play,20 interruption cycles, measured>=25fps normal mode/p95 input<100ms, TalkBack/200% font usability, and production-certificate upgrade tests. Physical control/HIL is N/A: no external equipment. Simulator tests do not establish subjective fun or field performance.
