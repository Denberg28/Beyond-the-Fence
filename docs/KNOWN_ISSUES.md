# Candidate limitations

- Physical-phone endurance, TalkBack, measured frame/thermal/latency behavior and production signing/upgrade continuity remain unvalidated; stable promotion is blocked.
- Action score playback state is emulator-testable; actual speakers/headphones, volume comfort, Bluetooth routing and interruption listening still need a phone.
- Preview is test-signed. Export before replacing it; a new certificate can require export/uninstall/install/import. Schema6 cannot downgrade to0.6 or earlier.
- Balance is intentionally dangerous and has deterministic seeded checks, not broad player playtesting. One idle guard cannot cover the farm. Later hordes can defeat even unattended elite crews. Relaxed mode remains available for half damage. Recovery retries the same day with seed aid and a volunteer if no crew remains; ruins stay lost.
- Fixed building sites, maximum3 guards, no wages, inward assault lanes/side-based targeting; no free building placement/pathfinding. Daytime scouts only damage at night. No player avatar movement; the player commands the farm and crew.
- English only; no cloud, multiplayer or offline growth. Short windows/large fonts may scroll. Score is one local8-second loop.
- Sudden termination can lose the last3 active seconds plus pending writes. Alternating checkpoints and manual backups mitigate this. CRC detects corruption, not intentional editing.
