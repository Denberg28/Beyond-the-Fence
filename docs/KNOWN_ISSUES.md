# Known limitations

- First candidate: no physical Android phone validation or player study is claimed. Stable promotion is blocked pending the procedure in TESTING.md.
- Debug test signing is not a production identity; fresh CI runners may require backup/uninstall/reinstall rather than an in-place upgrade. Configure a persistent private release signing secret before stable builds.
- English UI only, no audio/music, no free roaming, no cloud sync or offline growth. Fog is visual; all lanes retain native buttons and numerical threat status.
- Very large fonts or short windows can require scrolling to reach the dock; native world targets retain their minimum size. TalkBack and 200% font need handset validation.
- Sudden process termination can lose the last three active seconds plus pending disk writes. Failed disk writes are shown; export a backup before reinstalling. CRC detects accidental corruption, not intentional modification.
- A `.btfs` backup replaces the current farm after explicit confirmation. Import from newer schemas is rejected. Android cloud backup is disabled to keep storage local.
- Published0.1.0-rc.2 at545a5bb is the rollback APK/source baseline. Retain it and a schema1 backup; Android downgrade/signature rules may require reinstall before import.
