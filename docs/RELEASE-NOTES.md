# 0.18.0-rc.1 — Dark medieval pixel-art preview

Original dark-stone terrain, a tiled gothic courtyard, castle bastions, heraldic flags,
static torch glow and a steel-armored red-cloaked hero with blue magic. Arena and
HUD colors are darker. The game is still top-down fortress survival: farming,
fishing, mercenaries, equipment, enemies and boss rules are unchanged. Save schema
12, application ID and permissions remain unchanged.

**Important:** The automatically published Preview.apk is CI-debug-signed. It
CANNOT update an installed signed Release.apk from v0.12 onward. Do not uninstall
your existing game to try this candidate without first exporting your .btfs
backup. The unsigned Gradle release build is not an installable release. A
production-signed install-over update is blocked until a new privately signed
payload can be checked against the exact verified source and certificate.

Candidate quality gates: debug/release builds, JVM tests, lint, package identity,
audio and permissions audits, Android 26/35 native emulator suites. Verify their
PASS results at the GitHub Actions run before installing. Phone performance,
TalkBack/readability, battery life and human playtesting are NOT TESTED; this is
a prerelease, not a stable or same-key signed release.

VersionCode 24. Android 8.0+ (API 26). Baseline: signed v0.17.0-rc.1.
9 November 2026 (Asia/Manila).
