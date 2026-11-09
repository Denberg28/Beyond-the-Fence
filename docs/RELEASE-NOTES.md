# 0.18.1-rc.1 — Light build tabs

Build pages now use warm light limestone/parchment instead of dark padding and
charcoal actions. Six compact tabs (Fence, Farm, Mill, Crew, Items, Gear)
retain original IDs and ordering, 48dp touch targets and light-gold selection.
Action rows prioritize costs/status while their accessible labels retain details.
The dark pixel medieval battlefield and gameplay controls are unchanged.

No economy, save or world mechanics changes. Schema 12, application identity,
pausing during build, swipe-dismiss, rotation restoration and market routing stay
the same.

**Preview warning:** Automatic Preview.apk uses CI debug signing; it CANNOT
upgrade an installed private-key Release.apk. Keep the original installation and
export a .btfs backup. Unsigned APK is not installable. Same-key signing and
physical handset verification remain blocked for stable release.

Release gates: debug/release build, JVM tests, lint, APK audit, API 26 and API 35
native emulator smoke with active light-tab color / readable text and high-font
checks. Inspect the Actions result before installing. Physical phone, TalkBack,
long-session performance and signed install-over are NOT TESTED.

VersionCode 25. API 26+. Previous preview: v0.18.0-rc.1.
9 November 2026 (Asia/Manila).
