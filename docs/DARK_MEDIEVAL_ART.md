# Dark medieval pixel-art integration — candidate

This art pass redraws Greenhaven and Ashen March as low-saturation masonry landscapes.
The fortress courtyard uses clipped stone tiles, corner bastions, heraldic flags and
static torchlight; the existing hero entity receives a red cloak, steel plates and
blue spell light. Arena masonry and combat-banner tones, board underlay and common
buttons share the same muted palette. Art is rendered by native Canvas at runtime,
so no downloaded or copyrighted third-party asset is used.

**Scope:** Rendering only. Game rules, mercenary capacity, fence hitboxes, crop and
building touch rectangles, resource costs, enemy AI, random streams, game tick, save
schema, app identifier, and signing identity are unchanged. The hero retains the
existing Barracks unlock and paid replacement mechanics. The generated visual
reference is a side-scroller; this version intentionally remains top-down.

**Verification required before promotion:** JVM tests including MedievalPaletteTest;
both release/debug Android builds and lint; Android 26 and 35 emulator controls,
rotation, pause/resume and checkpoint/reload smoke tests; signed-key upgrade and
physical phone readability test at normal and 200% text scale. Confirm the
new low-light colors preserve legibility and touch target affordances on both
biomes. Static art is deterministic; reduced motion has no animated torches.

**Rollback:** retain v0.17.0-rc.1 and export a .btfs backup before testing. Do not
install a test-key Preview.apk over a fixed-signature Release.apk.

## 0.19 torchlight and terrain pass
Broken slab clusters replace uniform outdoor masonry. Moss/ash, edge pines and stepped border darkness establish foreground separation; dusk courtyard tones stay readable. Cached radial amber lights replace green circles. All decoration draws before roaming/combat actors. The HUD uses an inset charcoal frame, gold resources and read-only farm/fence bars. Original geometry, pale build surfaces, simulation and save data are preserved.

## 0.20 character/phase integration
Original integer-grid sprites replace round human/undead/fantasy units; tier colours, armour, cloaks, crossbows and narrow eye lights provide identity. Hero artwork is shared with the arena. Bosses use lazy128-square canvases per species; arena uses one300x164 stage, both scaled without filtering. No per-frame sprite/shader allocation after cache creation.12-second dusk interpolates ground/courtyard/edge tones and torch opacity without darkening actors. All motion/phase cues derive from simulation time; paused rain no longer reads wall clock.
