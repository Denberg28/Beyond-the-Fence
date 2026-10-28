# 0.14 emulator infrastructure retry

Run40681367751 rebuilt corrected source9c25f47 successfully (100 cases in each variant, builds, lint and package audits), but failed before the arena. API26's failure screenshot/window dump shows **Messages has stopped**, and its AndroidRuntime log identifies com.google.android.apps.messaging:rcs. API35's capture shows **Pixel Launcher isn't responding** over the game welcome; last-anr.txt identifies com.google.android.apps.nexuslauncher. No game crash was recorded. Preserve this evidence and retry on fresh runners with game code, emulator configuration, assertions and deadlines unchanged. The retry's documentation commit only corrects README action routing and records this diagnosis; it does not alter runtime or tests.

# 0.14 animated-dialog regression correction

First PR run40680136637 passed all build/unit/lint/package gates, then both native suites failed at the first arena Fight action. There was no app crash/ANR; the failure screenshot showed a live battle seven seconds in. The generic dialog helper required an idle main queue after every click, including continuous animation. Retain the10s liveness deadline, but synchronize animated dialogs on a new rendered arena frame (using the same approach already used for live farms). Paused dialogs still must become idle. Add an explicit400ms paused-arena clock/render stability assertion. Stop invalidating the frozen farm behind an active arena; only the arena needs drawing. Rerun final-source build and both native suites, retaining the failed run evidence.

# 0.14 validation plan — 28 October 2026

Baseline main d4bd381 / released0.13.0-rc.2: local JDK17 + JUnit4.13.2,93 cases PASS. New source:100 JVM cases PASS, including seven WorldTest cases covering all100 days,20 once-only bosses, schema9 bounds/continuation, real ashfall movement/wall damage, relaxed mode, weather fronts, recovery, portal carry/reset and chapter boundaries.

Required source gates: debug/release unit tests, APK/test APK builds, both lint targets, versionCode18 /0.14.0-rc.1 package identity, zero permissions, six unchanged original WAVs and <=5MiB audit. Required integration: full exact-APK API26/35 native suites; focus-tab order and action classification; actual purchase/assignment transactions; market dependency link; pause, rotation and200% font; portal→paused Ashen arrival; new biome ground/geometry and Cinder Regent spawn. Screenshots add supplies, Ashen arrival, daytime world and boss to the existing scenes. Review every new UI scene before candidate promotion.

Signing certificate/schema9 remain unchanged. Source-only CI cannot publish: the retained0.13 signed payload points at older source and fails the exact-source publish condition. A future signed candidate must pass the existing exact compiled-content, pinned-certificate and installed upgrade gates. No local emulator/KVM or physical phone is available. Record final Android/CI results in the implementation PR; a pending check is not PASS. Physical-phone validation remains NOT TESTED; HIL N/A because this is an offline game.

Acceptance mapping: F1–F3 use native pointer/navigation/layout/screenshots; W1–W3 use WorldTest plus installed portal/arrival/raid checks; W4 is an explicit scope limitation in README/known issues. Self-review is not independent review. Rollback retains the prior release and pre-update export.

# Published0.13.0-rc.2 verification — 28 October 2026

Release: https://github.com/Denberg28/Beyond-the-Fence/releases/tag/v0.13.0-rc.2

Source9c4e00d80a0646413c06c2ff5dbeeebbd7145c1c passed run40603504318. Delivery245a91a62c8e8a0600c0c19fe24ad8b988dc962a passed run40630736666, attempt2. Public Release.apk SHA256: `0a0df3e9c249954e43581934be14726902788b4367002ea313fe59abc81ac759` (3134473 bytes).

| Gate | Observed result |
| --- | --- |
| Build/unit/lint | PASS:93 unit cases in each debug/release variant, builds/test APK and both lint targets |
| Packaging/security | PASS: API26/35 limits, title/versionCode17, zero permissions, audio/size audit, fixed certificate and exact compiled-content comparison |
| Installed behavior/regression | PASS:1485 checks on each API26/35 emulator; equipment, assignments, champion/decline/retreat, rift, focus/lifecycle,200% font and original farm/economy/save flows |
| Update delivery | PASS:4 old-to-preview install-r/checkpoint checks and4 preview-to-non-debuggable-release checks on each API26/35 emulator |
| Visual self-review | PASS: new welcome/equipment/arena/rift screens; fixed action area and strengthened system-bar margins resolve clipped Android8 choices |
| Hardware/field | NOT TESTED: phone endurance/audio/performance/TalkBack and human balance; stable remains blocked. HIL N/A: no external hardware |

Attempt1 API35 failed because a system process ANR for ImmersiveModeConfirmation covered the visible game welcome. Failure capture/dumpsys identified the system FocusEvent timeout; the app crash log was empty. API26 and signing/build gates passed. The failed job was rerun on a fresh runner with unchanged APKs, assertions and timeouts; it passed1485 native plus both4-check update stages. Publication remained blocked until this result. Failed-attempt logs remain under https://github.com/Denberg28/Beyond-the-Fence/actions/runs/40630736666/attempts/1 . Final evidence is in the release and https://github.com/Denberg28/Beyond-the-Fence/actions/runs/40630736666/attempts/2 .

The published APK retains the private certificate pin and compiled code from the verified source. Private keys remain outside GitHub. The source ZIP records the reviewed source commit; release notes/checksums/provenance and emulator evidence accompany the APK. New schema9 cannot downgrade to older apps; retain a pre-upgrade save/APK. Older disposable-key previews require one-time export/install/import; same-key updates retain checkpoints.

## Development history

# Fixed adventure action area

Source e7aad20/run40602283578 passed all source gates and1485 native checks per emulator, but screenshot review still found Android8.0 choices under the navigation bar. Replace adventure prompts with a bounded dialog, scrollable message and fixed native action area; use a horizontal action row in landscape. Strengthen the screen check to require48dp controls and reserve24dp top/64dp bottom system-bar margins. Repeat source/signed gates and visual review.

# 0.13.0-rc.2 visual regression

Source9714dc1/run40600732808 passed93 JVM tests in both variants and1469 installed-app checks on each API26/35 emulator. Screenshot self-review found clipped arena choices and portal title ellipsis at200% font despite actions being programmatically reachable. Shorten those prompts; add a native check for every action being visible and fully inside the physical screen, plus a welcome scene capture. Repeat required source and signed-APK gates before publication. Earlier local APKs were development builds, not a published GitHub release.

# Authorized GitHub delivery checks

User explicitly authorized pushing to GitHub and publishing after checks pass. Source publication uses the connected GitHub app because shell Git has no login. Required checks remain unchanged and now include93 unit tests in both variants, auditing optimized release resources, a pinned-signature Release.apk compiled-content comparison, and actual preview-to-release installation/checkpoint retention on both API26 and35. Signed payloads are created locally from passing CI artifacts; no private key enters GitHub.

# 0.13 verification — TAG fantasy survival

Local JDK17 compiler/JUnit4.13.2: **PASS93 tests** including all critical baseline farm/damage/economy/codec/audio/century tests and12 new fantasy tests. Statistical audit uses100,000 deterministic rolls/source to check scarcity and high-tier eligibility, without altering enemy RNG.100-day scheduler now asserts portal completion at100 rather than101; all20 bosses remain required. New tests cover all100 arena invitation days, decline/replay, all4 champions, actual win/loss/timeout/retreat/stock, farm isolation, live arena save continuation, invalid gear/state, director bounds and new-world carry/reset.

Required candidate gates: full debug/release builds, lint, strict package/version/API/permissions/size/audio audit; all prior API26/35 native flows plus equipment/assignment/arena/rift native interactions and screenshots; fixed-certificate compiled-content comparison and same-key installed checkpoint migration. These gates are pending until their recorded CI result below. No stable promotion until physical-phone endurance/interruptions/TalkBack/audio/performance and human balance checks pass. HIL N/A: no external hardware.

New schema9 strictly bounds equipment ownership/slot identity, collection, rarity, director values, world/arena day consistency, champion/result values, finite HP/clocks and active battle relationships. Schemas1–8 retain old progress and receive empty relics/default director. Legacy schema8 fixture and signed upgrade checks retain real resources/investments. Save corruption and failed imports must keep old checkpoints intact.

## Final local evidence — 28 October 2026

- **PASS — build verified:** JDK Temurin17.0.21+9, checksum-pinned Gradle8.9, AGP8.7.3, API35/build-tools35.0.0. Gradle `testDebugUnitTest assembleDebug assembleDebugAndroidTest assembleRelease lintDebug lintRelease` builds the game and installed-app test harness. All93 JUnit cases pass with zero failures/errors/skips. Debug/release lint report no issues.
- **PASS — build verified:** strict package audit checks versionCode16/version0.13.0-rc.1, unchanged applicationId/minSdk26/target35, zero permissions, portable DEX, <5MiB size, six distinct correctly formatted/unclipped audio assets and absence of embedded keys/native libraries.
- **PASS — reviewed/build verified:** private-key signing is checked against the existing public certificate pin. Signing changes no compiled APK entries. No key/password is copied into source, APK or delivery files. Schema7 and released-schema8 JVM migration preserve real progress; accepted arena battle continuation is deterministic.
- **PASS — simulator tested:** strongest relics still allow an unattended Day30 steel/three-guard farm to be overrun. The prior Day3 investment/danger and Day12 baseline-loss regressions remain. A single Day12 seeded maximum-relic farm can survive longer; equipment intentionally adds survival chance. These checks do not establish broad human balance.
- **NOT TESTED:** the new API26/35 installed-app suites, actual signed install-r upgrade and new scene screenshots. The test harness compiles, but it has not run for this source. No local emulator/hardware was available; cloud CI awaits the blocked source push. Publication to main was rejected by automatic approval review because it requires explicit user publication authorization. No branch push, public release or alternate publication was attempted after rejection.
- **NOT TESTED:** physical-phone endurance, audio, performance, TalkBack and human fun/balance. Stable remains blocked. HIL N/A because this game controls no external hardware.

The deliverable is a **locally signed development build**, not a gated published candidate. Required installed-app/upgrade gates remain intact. The source and verification harness are committed locally, ready for the authorized GitHub path once publication is approved.

## Local release-variant build

User requested the latest release build. Public main push was again rejected by automatic approval review; no workaround/publication was attempted. Re-ran `testDebugUnitTest testReleaseUnitTest assembleDebug assembleDebugAndroidTest assembleRelease lintDebug lintRelease`: PASS,93 tests in each variant, zero failures/errors/skips. Built a separate **Release.apk** with debugging disabled and signed it with the retained private identity. Certificate pin, unchanged compiled content after signing, ZIP integrity/alignment, Android package/version/API/title, zero permissions, audio and size checks PASS. Runtime source remains e9c6fa042843567c8d97ef91ae15b50e6d8fee0a; no gameplay changes in this packaging step.

Release APK SHA256: `0e709feab09a265e452343e937ae8f4de3c2509419a75e980585c85c8992c0bb`,3130377 bytes. The package audit now resolves audio through the compiled resource table because AAPT2 shortens release ZIP paths; it checks all six actual assets in both debug and optimized release packages. CI retains these gates and also audits the unsigned release's non-debuggable manifest.

This is an installable local release-variant build, not a stable/public release. Required exact-APK API26/35 installed-app/upgrade tests and physical phone validation remain NOT TESTED. Existing same-key package identity is preserved; export before updating, especially from older disposable-key previews.

## Earlier records

# 0.12 verification — Century survival

Baseline0.11.1/608a451 passed75 JVM tests and1312 native checks per API26/35. Local JDK17/JUnit:81 tests PASS. CampaignTest covers every day1–100, exactly20 boss events with live save/reload, all late ranks/effects/cost caps, stagger/dormancy/new types, single-vs-area/no-target potion accounting, equipment/save continuation, and a literal fixture generated by the released schema7 codec. Existing Day3 danger/Day12 unattended wipeout, resource/recovery/corruption/audio suites remain. Slower daytime starts can now prevent the first-night crop loss; the test still requires economy-funded survival and all10 kills. Reinforcement floor intentionally changes from0.85 to0.6 for later days. These acceptance changes are explicit, not disabled threat gates.

Required candidate gates:81 JVM, all Android builds/lint, version/API/zero-permission/<=5MiB/six-WAV audit, native API26/35 full regression and20 game-scene screenshots reviewed. New native cases buy both attacks, assign a slot through its actual dialog, damage the Day100 boss through the new upper-left slot, verify enlarged survived99-days header and retain symmetric layout. Existing lifecycle/font/material/save/audio gates remain.

Signing has two phases. Source run builds/tests with disposable CI signing and cannot publish. A privately signed preview/tests plus re-signed old0.11.1 emulator fixture are submitted as public APK blobs, with SHA256/public cert pin/source-run metadata. Delivery run freshly rebuilds source, compares every non-signature APK entry, verifies certificate pin/hashes/source run, repeats full native gates on the exact signed APK, and publishes that same artifact targeting its source commit. No private key/password is in Git/CI cache/artifacts. Local signing keeps compiled entries byte-identical. Delivery metadata is a child commit; Source.zip records the verified parent code commit.

Upgrade check: install the re-signed released0.11.1 fixture on a clean emulator; seed a released-v7 checkpoint (Day45,4321coins,876timber,137coreHP,111eastHP, crop0.75growth, hero200HP,3Elixirs/2Enhancers); install-r the signed new APK without clearing data; run upgradeOnly instrumentation and verify real loaded state and rewritten schema8 equipment. Then clear only the test farm and run full regression. Re-signing a test fixture DOES NOT make the user's old disposable-key installation compatible with the new key.

Stable remains BLOCKED for physical phone listening/mixing/routing,30-minute endurance/20 interruptions, TalkBack200% font and measured performance. Source/simulator checks are not handset validation. HIL N/A. Keep pre-upgrade export and APK; schema8 cannot downgrade to0.11.

Initial source b0c4aa3/run40592722412: build/lint/81 JVM/package PASS; both emulators FAIL on startup due to use of a framework ID with View.setTag. Replaced that cache with app-owned integer state. Required startup gates retained. Local signing also exposed apksigner consuming successive password-file lines when the same file is supplied twice; PKCS12 signing now supplies its store password once and uses the identical private-key password. No key was changed or leaked. Re-run full source and signed-delivery gates on corrected source.

## Earlier evidence

# 0.11.1 verification — First light and clear controls

Baseline:0.10.0-rc.1/2cdb19a3301a8f976a3f1ce8c530b569d4dfb48a passed72 JVM cases and1182 native checks on each API26/35. Local JDK17/JUnit4.13.2:75 tests PASS, including3 new RewardTest cases. Initial test fixtures used an invalid night roamer/spawn-clock combination; fixtures were corrected to valid gameplay state, retaining strict save validation.

Required before candidate publication: all Android builds/lint/unit reports, package/version/signature/zero-permission/size/six-WAV audits, exact-APK native suites on API26/35 and screenshot self-review. Native additions compare mirrored button/slot sizes, aligned actions, backdrop pixels, transparent containers and repeated building-tab switches from scrolled content; retain pointer transactions, font/rotation/pause/save/sound checks.17 game scenes/API, including new tabs screenshot. New First Light32s sunny track must differ; creepy/four effects must stay byte-identical to baseline. Package remains<=5MiB.

PASS/FAIL evidence must come from final CI source/artifacts; publication is gated on all automated candidate checks. Stable remains BLOCKED pending physical phone listening/mixing/routing,30-minute endurance/20 interruptions, TalkBack200% font, measured performance and production signing continuity. HIL N/A: this game controls no physical hardware. Schema7 is unchanged and0.10-compatible; export/retain the previous APK before installing a separately test-signed preview.

0.11.0/bb242b3 automated gates passed75 JVM tests and1289 native checks per API26/35 (run40511923526). Screenshot self-review found awkward whole-word wrapping in compact Harvest/Barracks labels at200% font.0.11.1 limits compact captions to18sp effective dock/16sp tabs, retains full-size body text, and adds whole-word/line-fit assertions. Repeat affected Android/native/package gates on final source. No gate was waived and the published0.11.0 artifacts are retained.

## Historical evidence

# 0.10 verification plan

Baseline0.9/29eeea5 passed63 JVM cases and923 native checks per API26/35. Local JDK17/JUnit suite now72 cases PASS before Android packaging. Final-source builds/lint/package and both exact-APK native suites remain blocking. Self-review is not independent review.

ChapterTest adds9 meaningful cases: five-day transitions/asset preservation; continuous distinct shared boundary coordinates; accessible tile containment/no overlap across templates/orientations/fonts; exact reinforcement spending/caps/mitigation; real mill/drill benefits; hero cap separation/fire/death/training; consumable purchase/heal/timed buff/no waste/bounds/deterministic continuation; invalid new state rejection; released-schema6 fixture migration with preserved damage/resources. Existing byte-mutation/truncation/fuzz and save/lifecycle suites remain.

Native adds actual insufficient-fund taps showing price without debit, additional rank availability, hero recruitment/drill/training, Mill production upgrade, Shop inventory and both field slots, chapter layout changes with retained assets. Sixteen scenes per API add Barracks/items/Village/Castle. Existing precise economy/damage, fonts/orientation, static pause, full sound dispatch/mute, Home/focus/checkpoint checks remain. Audit32s phase WAVs and all4 effects for exact format, no clipping, non-silence and quiet boundaries; preview remains<=5MiB/zero permissions. Compare source archive and published artifact hash to audited candidate.

Stable gates remain BLOCKED: physical phone30min/endurance20 interruptions, TalkBack, sound listening/mixing/route/volume, actual frame/thermal/latency measurements and production signing/upgrade continuity. No equipment-control HIL applies. Export pre-upgrade schema6 and keep0.9 APK for rollback; schema7 cannot downgrade.

## Prior design and verification records

# 0.9 verification plan

Baseline0.8.0-rc.1/3c8251f passed63 JVM tests and824 installed-app checks on each API26/35 emulator. No Game/SaveCodec/audio changes are needed; local JDK17/JUnit regression remains63 PASS. Final-source full Android builds/lint/package and both installed-APK suites remain blocking. Self-review is not independent review.

New native checks: four button interior pixels have zero alpha at rest; no instruction banner; Merc ↑ label;48dp center fits compass middle; no-selection/affordability/max disabling; actual direction selection and center Gap→Wood→Stone→Steel purchases with exact debits, independent sides and continued play; Mill/Camp feature routing and retained Mill context after rotation; Shop market purchase/bonus harvest/plant exact transactions.12 scenes per API include Shop and compact gameplay. Existing checkpoint/import corruption, fonts/orientation, no paused drawing, focus/Home, day/night/sound dispatch/mute and survival damage/economy tests remain.

Compare final source ZIP to workspace and published APK digest to audited candidate. Six generated audio files must remain reproducible. Record observed results in release verification artifacts, not assumptions. No existing check may be waived to publish. Physical phone30min/endurance20 interruptions/TalkBack200% font/audio routing+listening/measured performance/production certificate upgrade remain BLOCKED; HIL N/A. Schema6 unchanged; keep backup/previous APK before upgrading.

## Prior design and verification records

# 0.8 verification plan and evidence

Baseline0.7.0-rc.1/edb4941 passed58 JVM cases and787 checks on each API26/35 emulator. Local pure JDK17/JUnit run now63 cases PASS; Android build/lint and installed-artifact suites remain required on final source. Acceptance Q1–Q6 is in SPEC.md; self-review is not independent review.

New JVM cases: zero-timber coin repairs on each border, exact debit/cap, invalid/intact/gap/defeat/insufficient no charge; actual combat-shot cues with no repeats or combat mutation; day/night nocturnal restriction, bounded time and stale-event suppression. Existing construction/economy/migration/corruption/Day3 threat/Day12 wipeout/recovery checks stay active. Native checks move fence geometry to the bottom cross; use real pointers for no-panel coin repair, direct hires/uniforms; verify sunny/creepy playback and4 SoundPool effect dispatches from an actual running combat fixture. Pause/focus/Home must stop all streams, and both mute settings persist across orientation/restart. Same10 screenshots per API are self-reviewed.

Package audit checks identity/API/zero permissions/size/signature and6 distinct WAVs with exact length/format, non-silence, unclipped samples and quiet boundaries. Generated scores must match across clean builds. Published APK must match audited candidate checksum and packaged source must match workspace.

Candidate publication is blocked until full builds/lint/JVM/package/API26/API35 pass. Stable still BLOCKED: physical phone30-minute play/endurance with20 interruptions, speakers/headphones/Bluetooth/other-app audio/mute/volume listening, TalkBack200% font, measured frame/thermal/latency behavior, production certificate and real upgrade continuity. No physical-control HIL applies. Keep pre-upgrade export/APK for rollback; schema6 unchanged.

## Historical verification/design records

# 0.7 survival verification

Baseline: verified0.6.0-rc.1/147ebd7. Required candidate gates unchanged: all JVM tests, builds/lint/package audit, and installed exact-APK native suites on API26/35. Local JDK17 can run pure simulation tests; no local Android SDK is installed. CI is authoritative for Android build and native evidence. Self-review is not independent review.

Acceptance map: docs/SPEC.md S1–S9. New seeded tests require Day3 wood/workwear to suffer actual wall damage and lose; stone/desert kit survives the same seed. Later Day12 steel/black-ops must still be vulnerable unattended. The first day must be survivable using real timed harvest/fencing/recruitment. Repeat20 defeat/recovery cycles and save/checksum corruption checks protect solvency. New schema5 fixture is generated with the released source and tests one-time three-excess-guard refunds. v6 health/casualties/healing continue deterministically after reload.

Native suite retains pointer crop/economy/fence/build/repair, rotation, large font, background/pause/save recovery regressions; replaces heart pixels with actual durability-bar full/half/zero pixels. Adds actual MediaPlayer state during raid and stopped on focus/Home, real Options mute and persisted mute across rotation/restart, three-guard cap, uniform purchases, survival scene screenshot. Emulator output cannot establish audible quality. Score package audit checks WAV format,8-second length, non-silence and no clipping.10 screenshots per device are reviewed for clipping/visibility.

PASS evidence is recorded by the run producing the released APK. Before publication pending items must not be presented as passed. Phone:30-minute survival session including at least20 background/foreground/focus interruptions, speaker/headphone music/mute/other-app audio tests, TalkBack at200% font, frame timing/thermal/latency and production signing/upgrade remain BLOCKED. HIL N/A: no physical equipment commanded. Stable promotion requires these applicable device gates.

## Verification and promotion gates

Scope: first Android game, no physical machinery/firmware/protocol interfaces. Initial baseline is the rules-only commit; there are no existing client compatibility regressions to preserve. Self-review is not independent review. Test candidate only until required handset checks pass.

## Automated checks

Run `./gradlew testDebugUnitTest assembleDebug assembleDebugAndroidTest assembleRelease lintDebug lintRelease`, then `python3 scripts/check_package.py "$ANDROID_HOME/build-tools/35.0.0/aapt" app/build/outputs/apk/debug/app-debug.apk` and `apksigner verify --verbose --print-certs …/app-debug.apk`. Unit XML/HTML reports are generated by Gradle.

On an Android 26 or 35 emulator, put the candidate Preview and Tests APKs in `artifacts/` using the release filenames and run `bash scripts/emulator-smoke.sh`. It installs both APKs, clears **the test farm only**, drives native planting/harvesting/upgrading/night/recovery controls, backgrounds/resumes, restarts the actual activity, validates atomic checkpoint recovery, captures a sample screen and fails on instrumentation failure/crash. Do not run `pm clear` against a farm you wish to keep.

JVM coverage includes crop sale/unlocks, affordability/caps, weather/irrigation, prepared first-night survival and zero-coin recovery, defeat/recovery, relaxed damage, delta-time bounds, rally movement without instant damage, exact deterministic save continuation, all single-byte save mutations, every truncation length, valid-CRC invalid values, 3,000 malformed inputs, all enemy variants and 20 defeat/recovery cycles. This establishes mechanical viability, not subjective fun or real-player balance.

CI checks exact APK identity/version/API range, <=5MiB, no permissions, no native libraries/private keys, signature verification and clean source. SDK/Gradle/AGP/JDK versions and source SHA are packaged in BUILD-INFO.txt. Unit XML, emulator evidence and workflow logs link test evidence to the source and artifacts. CI only releases after build and both emulator jobs succeed. A required failure blocks publication. Main pushes do not replace an existing version.

## Gate record

The authoritative run-specific results ship in VERIFICATION.txt with the release, unit XML and emulator-evidence ZIP. The initial local setup has no emulator/KVM or attached Android phone. Update this table only with observed evidence.

| Gate | Status before final run | Evidence / requirement |
|---|---|---|
| A Source/build/static | NOT TESTED | Gradle debug/release compile, test APK compile, lint, package/signature audit |
| B Mechanics/persistence | NOT TESTED | GameTest JVM suite; instrumentation checkpoint test |
| C Regression | NOT TESTED | Critical MVP flows tested against first implementation; no previous app |
| D Android integration | NOT TESTED | API26/35 installed-APK native UI, real Activity lifecycle, disk checkpoints |
| E Physical control/HIL | N/A | Game sends no external hardware commands |
| F Candidate delivery | NOT TESTED | Version, checksums, exact-source archive, provenance, GitHub prerelease |
| Stable: handset acceptance | BLOCKED | No physical phone accessible in this environment |
| Stable: signing/upgrade | BLOCKED | Production private key/upgrade continuity not configured |

## Runnable handset acceptance (required for stable)

Configuration: report phone model, Android version/API, screen resolution/density, font/display scale, app version, APK SHA-256 and signing certificate fingerprint. Export any existing farm first. Install Preview.apk; enable airplane mode and turn Wi-Fi off. Record outcomes and logs, not just screenshots.

1. Fresh launch: no permission prompts; complete help, plant each unlocked crop, wait for harvest, compare debit/credit with SPEC.md. Buy an upgrade when affordable and try when not affordable. Reach day2/3 crops, expand land. Expected: no duplicate rewards, negative balance or silent failed action.
2. Play >=30 min, through several weather states and all four zombie types. Exercise all four rally borders, repair and free defeat/recovery. Expected: no crashes, softlocks or lost upgrades; night population <=45 and night ends only after every enemy is killed. Turning relaxed mode on halves fence damage; it does not alter coin prices.
3. Repeat Home→wait10s→reopen→Play 20 times, include lock screen, recent-app switching, a notification/phone interruption and rotation. Record game elapsed before/after the pause. Expected: no simulation while backgrounded, explicit resume, no surprise attacks or stuck controls. Repeat rotation in night and day.
4. After a saved mutation, wait for “Saved on this phone”; force-stop/reopen 10 times. Expected: exact last acknowledged checkpoint restored, paused. Kill immediately after a mutation; allowed loss is last checkpoint interval plus pending IO, never an unreadable farm or unexplained reset.
5. Options→Export to on-device Documents. Record file size/hash. Advance play, then Import exported file. Expected: confirmation, farm restored and paused; cancelled import leaves farm unchanged. Try a truncated or arbitrary file; expect rejection with current farm intact. Export before any uninstall. Uninstall/reinstall and import; expect recovery of progress.
6. Enable TalkBack; operate Farm, Build, Defend and Options without touching artwork. Verify logical focus, plot numbering, affordability/recovery announcements and no color-only distinction. At 200% font and increased display scale, all controls remain available by scrolling; test portrait and landscape. Expected touch targets >=48dp; no clipped essential action. Reduced motion removes moving rain, bobbing and shot flashes.
7. Collect frame timing with `adb shell dumpsys gfxinfo com.denberg.beyondthefence reset`, play normal mode with maximum enemies for >=60s, then collect `adb shell dumpsys gfxinfo com.denberg.beyondthefence framestats`. Use Perfetto/input trace for p95 input latency. Expect >=25fps normal mode, p95 input response <100ms, and stable memory across 20 lifecycle cycles. Reduced-motion mode intentionally draws at ~10Hz. These targets are NOT measured by source inspection or JVM tests.
8. With backup available, test local storage exhaustion/denial in a disposable emulator, then a successful retry. Expected visible save failure, no UI freeze, and previous valid checkpoint recoverable. Test one checkpoint corruption and both checkpoint corruption with `run-as` on a debug build; one recovers visibly, both block automatic overwrite until explicit import/reset.
9. For stable signing: sign two versioned builds with the same privately held certificate, install version1, plant/upgrade/export, install version2 using `adb install -r`. Expect successful upgrade and unchanged saves/settings. Test rollback only with compatible schema, keeping a backup; Android may reject a lower versionCode.

Retain PASS/FAIL/BLOCKED observations, timestamps, logs, build hashes and screenshots where useful. Missing observations remain BLOCKED. Known critical/high defects block candidate and stable delivery. For a serious regression, retain backup, stop promotion, reinstall the retained compatible candidate, import compatible backup, fix and rerun affected gates.

## rc.2 verification record

Baseline `975d790`: CI run40336041901 build PASS; API26 and35 integration FAIL at SmokeInstrumentation.java:56 (null active accessibility window), release skipped. Root cause: first window query occurred before the automation connection observed an active window. rc.2 connects automation before launch and bounds welcome discovery to10s; missing/disabled actions still fail the gate. No check is skipped.

New JVM cases cover budget-limited bulk planting, locked seeds/land, occupied plots, defeat, exact seed debits, adjusted countdown/readiness and schema1 round-trip with single harvest credit. Native checks cover bulk planting/selling and retained tab after recreation. Full final-source CI and exact-artifact API26/35 smoke remain required for candidate publication; run-specific observed results accompany the release. Local Gradle resolution is BLOCKED by network access; local SDK Java compile is an additional check only. Handset acceptance/production signing remain BLOCKED.

### rc.2 lifecycle follow-up

Source `700a314`, run `40385561662`: build, 18 JVM tests, lint and package/signature audit PASS. Both API26 and35 installs succeeded but instrumentation FAIL at the background-pause assertion; release was correctly skipped. The test closed Home command output immediately and assumed completion after500ms, without observing actual window state. Follow-up drains command output and polls main-thread focus/pause conditions for at most10s. App also explicitly pauses/checkpoints on window-focus loss, covering interruption before/without `onPause`. Native regression now requires running state before interruption, focus-loss pause, no automatic focus-return resume, actual Home pause, no elapsed-time advancement and foreground return still paused. Focus callback checks are simulated; Home/background checks use actual Android transitions. Failure evidence now always includes activity state, final screen and crash logs. Final-source CI results remain authoritative; no failure is waived.

## 0.2 in-world controls verification

Automated candidate acceptance adds BoardGeometryTest over 2 orientations ×3 densities ×3 font scales ×2 spare-height configurations, asserting all12 native actions are separate, >=48dp and within artwork. Native instrumentation injects real pointer down/up events into rendered views to plant, reject early harvest, bulk plant/sell, harvest one ripe tile, select seeds, focus lanes, open locked land, buy/close upgrades and recover. It validates portrait/landscape recreation, retained seed/overlay, actual200% system font and reachability via scrolling, background/focus pause, restart and dual-slot corruption recovery. Native bounds/labels are checked in each layout. The test farm is cleared and emulator font scale is restored on exit.

Evidence includes farm/night/landscape/upgrades-landscape/large-font screenshots and always-on failure diagnostics. Gates remain build/lint/package/JVM and both exact-artifact emulator runs. Final results ship in VERIFICATION.txt; do not treat an uncompleted run as passed. Physical TalkBack, cutout/gesture behavior, frame timing/input latency, endurance and production identity upgrades remain BLOCKED. Self-review/emulators are not handset validation.

UI run40391295624 (`88872da`): build/lint/package and19 JVM tests PASS; emulator gate FAIL (API26: instrumentation pointer delivery rejected during immersive transition; API35: welcome obscured/unavailable). Follow-up uses Android UiAutomation input injection with explicit screen coordinates, acknowledges the actual OS immersive hint through its native accessibility button, and captures the failure screen/window before instrumentation finishes. No app permission or acceptance assertion is removed. Android system coaching is outside the farm and must not be mistaken for a game control.

Source `2ef2c4c`, run40392368621: PASS19 JVM cases, full builds/lint/package/signature checks and278 installed-app checks on each API26/35 emulator, then published0.2.0-rc.1. Screenshot self-review identified rain/fog Paint alpha leaking into the next background frame, reducing label contrast. rc.2 resets background alpha and adds a two-frame software rendering assertion on the installed FarmView for rain and fog; each new frame must have opaque sky pixels. Disabled controls now preserve readable labels. New final-source checks must pass before rc.2 publication; rc.1 assets are preserved.

## Farm Fortress 0.3 acceptance

Source baseline66d3839 was build/emulator verified. New acceptance is in SPEC.md F1–F7. Add FortressTest for coin+timber atomic spending, construction/recruit caps, crop/mill/market production, actual spatial patrol targeting/movement, wall mitigation/recovery, four-sided waves, a literal released-format schema1 migration fixture, schema2 deterministic continuation and invalid new fields. Existing fuzz/truncation/mutation/20-day mechanics tests remain. Geometry now covers16 targets and requires all12 interior crops/buildings to be inside the perimeter.

Exact APK native checks plant/harvest resources, build an east fence, construct mill and barracks using repeated harvest income, hire a moving guard, rally west, run a night, preserve buildings/guards through interruptions/recovery/rotation/restart, change seed, exercise200% font and checkpoint corruption. Five screenshots document the new fortress. Test setup manipulates crop maturity to exercise flows quickly, not to claim real-time player balance. All build/lint/JVM/API26/API35 gates remain blocking; observed final results ship in VERIFICATION.txt.

Handset validation additionally checks30min castle play with all3 guards and45 enemies, upgrade/recovery/save continuity, TalkBack labels for map markers, gesture/cutout behavior and actual performance. Export schema1 before upgrading and test schema1 import plus schema2 export/reimport. Old versions cannot read schema2; downgrade needs the retained schema1 backup. No production signing or physical-phone pass is implied by emulator success.

Fortress source0b7906b passed27 JVM cases, debug/release/test builds, lint and package/signature gates. During UI self-review, Plant all in Gear retained its former main-board-only guard; remove that guard for the new paused-panel action. Native resource/planting assertions remain unchanged, and the final patched source must pass the full gates before publication.

CI supersession: a newer main commit cancels older candidate workflows for the same branch, preventing obsolete source from blocking the corrected build. The final source still runs every build/unit/lint/package and both emulator gates. The native instrumentation command has a300s limit; timeout fails the pipeline and collects screen/activity/ANR/touch diagnostics. This does not waive a check or promote a timed-out run.

## Fortress release continuation · 27 October 2026

Baseline87034c7, run40399922927: build/lint/package/signature and27 JVM tests PASS; API35 installed-app integration PASS400 checks; API26 timed out after300s while touching Gear in the paused construction panel. No ANR or crash was recorded. Touch logs show increasing delays between actions. MainActivity refreshed native labels/layout and invalidated the full world every frame even while paused.

The paused loop now skips simulation, refresh and drawing; individual UI actions and checkpoint callbacks still refresh their own state. The light100ms scheduler retains bounded resume latency. Native checks require a paused board and paused construction panel to remain static over600ms after transient effects settle. Main-queue idle waits now fail explicitly after10s instead of hanging indefinitely. All existing pointer/action, rotation,200% font, focus/background, restart and checkpoint gates remain required on both APIs. Final run results must be observed before candidate publication.

Follow-up run40446692762: source67e9236 passed build/lint/package/signature and JVM gates. Both emulators reached the new panel rendering assertion, which failed because a ViewTreeObserver draw listener observes the whole window, including native panel effects, rather than the farm's cached hardware layer. The regression now counts actual FarmView.onDraw calls. It retains the same600ms interval, paused-state requirement and at-most-one-scene-draw limit; no existing gameplay assertion is removed. Both API suites must pass again on final source.

##0.4 compact farm and roaming acceptance

Baseline55006ab is the published0.3.0-rc.1 preview:27 JVM tests and509 installed-app checks on each API26/35 emulator PASS. Scope/R1–R6 and formulas are in SPEC.md. User requested a smaller farm, visible wandering zombies and attack frequency per level. Assumption: player XP level controls frequency; day still controls wave quota/variants/HP. No assets/dependencies/services added.

JVM coverage adds harmless moving scouts, position-preserving raid conversion, no double conversion, first reinforcement delay, level-based automatic raid thresholds, bounded population/frequency, continuous corners, released-schema2 migration, schema3 deterministic continuation and invalid state rejection. Geometry covers surrounding terrain plus existing16 separate>=48dp actions across orientations/densities/fonts. Native coverage adds actual roaming model advancement with unchanged resources/health, Raid in label, compact map bounds, visible roaming screenshot and an automatic level10 raid driven by Play and actual Android frame ticks. Existing construction/recruitment/pointer, static-pause, focus/Home,200% font, rotation, restart and dual-checkpoint checks remain.

Local JDK17/JUnit4.13.2 regression:33 tests PASS before final source packaging; rerun after edits. Final-source Android build/lint/package and both emulator runs remain required. GitHub VERIFICATION.txt, source SHA, unit XML and emulator-evidence ZIP record observed final results. Handset/endurance/TalkBack/performance/production-signing gates remain BLOCKED; HIL N/A (offline game). Rollback requires retained pre-upgrade schema1/2 backup and matching previous APK.

##0.5 damage, destruction and harvest-funded defenses

Baseline02efd34 is the published0.4.0-rc.1:33 JVM tests and531 native checks per API26/35 PASS. User scope: real fence damage, zombies destroying farms, harvest income buying fence materials and mercenaries. Assumptions: destroyed crops/buildings need replanting/rebuilding; wall tiers and existing guards are retained; materials may be bought without first building the market, preventing a prerequisite loop. D1–D7 in SPEC.md define acceptance and prices.

Legacy damage regressions now assert per-side wall loss rather than shared-core loss: relaxed mode must halve actual wall damage, stronger walls must reduce that loss while protecting core health, and core-defeat tests explicitly place an intruder inside. Recovery seed-aid expectation changes to12 coins. These reflect the requested new damage contract; assertions are replaced with stronger damage/entry checks, not skipped. Existing20-day viability and first-night/zero-coin recovery remain.

DamageTest adds10 cases: contact→wall breach→entry, crop destruction without harvest/double credit, repair preventing new entry without ejecting intruders, atomic/capped timber/repair spending, real harvest-funded construction/recruitment, destroyed barracks guard retention/save compatibility, mill/market loss and reconstruction/repair, recovery retaining ruins with seed aid, active breach deterministic save continuation/invalid states, and a literal released-schema3 migration fixture. Local JDK17/JUnit4.13.2 run:43 tests PASS before final packaging.

Native acceptance adds a disposable raid fixture with a weak fence and crop, then uses actual Android frame ticks to observe breach/destruction. Real pointer actions replant, sell a surviving crop, buy materials, repair the breached side and hire a guard, asserting exact debits and a still-present intruder. Capture breach/rebuilt views in addition to the existing six scenes. Full final-source builds/lint/package/signatures and both exact-artifact emulator suites remain required. Release VERIFICATION.txt/CI/unit XML/source SHA/evidence ZIP supply observed results. Physical-phone/endurance/TalkBack/performance/production-signing remain BLOCKED; HIL N/A.

Damage source8f96fc7, run40465430059: build/lint/package/signature and43 JVM tests PASS. API35 PASS594 native checks; API26 FAIL at the existing10s main-queue-idle bound after tapping Play for a level10 raid at200% font. Publication correctly skipped. The active250ms refresh repeatedly called TextView.setText for every control even when characters were unchanged, triggering unnecessary layout/accessibility work during animation. MainActivity now updates text only when changed, while action handlers still refresh immediately and health/countdown changes remain live. The existing idle bound, real-pointer and all gameplay assertions remain unchanged; both emulator gates must pass again on final source.

Screenshot self-review of the successful API35 run also found two-line wall health markers clipped vertically at200% font. Large-font map markers now show a single compass letter with the health bar/color; full numeric health remains in accessibility labels and repair actions. Native checks require all four single-line markers to fit their48dp targets at200% font. The compact HUD uses Crew to avoid splitting Squad across letters, and building labels receive more usable width. Final-source API26/35 runs remain blocking.

Source2ccf5aa run40466514074: build/lint/package and43 JVM tests PASS; both emulator gates FAIL, release skipped. API26 failed initial game-focus discovery: failure screenshot/window dump confirm Android's ImmersiveModeConfirmation held focus after the welcome action, with no crash. API35 reached the active raid but global main-queue-idle discovery failed before tapping Pause; continuous rendering makes global idle an invalid synchronization condition for animated scenes. Text-change caching is retained as a product optimization.

The harness now acknowledges the actual OS Got it hint while polling UI readiness, not just before the welcome/pointer sequence. Paused screens retain the same10s idle requirement and static-rendering assertions. Running scenes instead require a new FarmView render within the same10s bound; pointers also require real window focus, layout/visibility and delivered down/up events, followed by the existing exact gameplay/state assertions. Screenshots require an Android Choreographer frame and bitmap capture, allowing animation to remain live. This replaces an unsuitable internal idle condition with observable animation/input readiness; no gameplay, loss/resource, layout, lifecycle, persistence or release gate is removed. Final-source build and both complete emulator runs remain required.

##0.6 fences, hearts and mercenary-only defense

Baselineb4d5cfb is the published0.5 preview:43 JVM tests and660 native checks per API26/35 PASS. User scope: remove towers, rely on fence materials/upgrades/mercenaries, show fence hearts, improve zombie art. Defaults: one volunteer for new or guardless migrating farms, existing rifle investment becomes mercenary training, and Wood→Stone→Steel are purchased material kits with timber frames. M1–M7 in SPEC.md define bounded behavior and acceptance.

GuardDefenseTest adds6 cases: no hidden structure/rally damage without guards; volunteer first-raid/zero-coin viability; training changes actual shots/cadence; material/repair heart fractions; deterministic rally/patrol continuation; released-schema4 migration retaining damage/training and granting only one volunteer. Prior turret-focus tests now assert guard movement with no instant shot. Recruiting tests account for a starter guard; isolated destructive/spatial fixtures explicitly clear volunteers. The20-day strategy includes barracks/recruitment to match the requested defense model. All old economy, loss, fuzz, damage, recovery, geometry and lifecycle coverage remains. Local final-source JDK17/JUnit4.13.2:49 tests PASS before Android packaging.

Native coverage adds software-rendered fence-heart pixel counts at full/half/zero life, material-kit purchases with exact coin/timber debits, non-damaging rally/patrol commands, and a scene with all four zombie types plus Wood/Stone/Steel/Gap sides. Existing portrait/landscape/200% font, paused rendering, real touches, foreground/background, automatic raids, crop destruction/repair/recruitment and checkpoint recovery checks remain. Nine screenshots accompany the candidate. Final build/lint/package and both exact-APK emulator gates must pass; observed results ship with the release. Handset/endurance/TalkBack/performance/production signing remain BLOCKED; no hardware control/HIL applies.

##0.7 build follow-up

Source3f981ae/run40486100422 failed before Android compilation: Groovy generated invalid bytecode for a mixed long/int closure in the new score task. Release correctly skipped. Score serialization now uses untyped closure parameters and bounded32-bit values (all WAV lengths/samples fit). All gates must rerun; none is waived. Local updated JVM suite:58 tests PASS.

Sourcee6fae38/run40486354416: build/lint/package/score and58 JVM tests PASS. Both native suites reached confirmed raid playback and actual Options mute, then FAIL at the new dialog helper comparing Close against Android's rendered CLOSE button. Captured API35 failure screen confirms Action music: OFF and a visible enabled CLOSE; no app crash. The helper now accepts case differences for the same label while still requiring an actual accessibility click within10s. All state, playback, persistence, geometry and touch assertions remain; publication was skipped and all gates rerun.

##0.8 native follow-up

Source61583ad/run40493349440: build/lint/package/six WAV audits and63 JVM tests PASS; API35 PASS824 checks (including all4 actual SoundPool cues). API26 stopped on a centering assertion after rotation; AndroidRuntime shows the instrumentation AssertionError on the main thread, not an app logic exception. The assertion mixed screen coordinates with Resources display metrics. It now compares the same rendered coordinate space (cross versus full-width command bar), retaining the same<=2px centering requirement and checking vertical visibility against actual window bounds. Failure messages include measured positions/sizes. No touch, affordability, no-pause, audio, persistence or geometry check is removed. API35 screenshots self-reviewed: pinned cross/shortcuts and costs visible,200% font controls usable, farm independently scrollable. All gates rerun before publication.

##0.10 build follow-up

Source8ffe5ab/run40506458288 failed before compilation because compact Groovy multiplication followed by a leading-dot decimal was parsed as spread-dot syntax in the new score. Numeric literals now have explicit leading zeros and obsolete note tables are removed. Release was skipped; all Android/build/package/native gates must run on corrected source. Local gameplay/migration suite72 PASS.
