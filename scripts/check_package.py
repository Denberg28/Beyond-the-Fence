#!/usr/bin/env python3
"""Fail closed on unexpected identity, capability, or oversized preview packaging."""
import pathlib, subprocess, sys, zipfile

aapt, filename = sys.argv[1:]
apk = pathlib.Path(filename)
assert 0 < apk.stat().st_size <= 5 * 1024 * 1024, 'Preview must be <=5MiB'
badging = subprocess.check_output([aapt, 'dump', 'badging', str(apk)], text=True)
assert "package: name='com.denberg.beyondthefence'" in badging
assert "versionCode='4'" in badging and "versionName='0.2.0-rc.2'" in badging
assert "sdkVersion:'26'" in badging and "targetSdkVersion:'35'" in badging
permissions = subprocess.check_output([aapt, 'dump', 'permissions', str(apk)], text=True)
assert 'uses-permission:' not in permissions, permissions
with zipfile.ZipFile(apk) as z:
    assert not any(n.endswith(('.so', '.jks', '.keystore')) for n in z.namelist())
print(f'PASS: package, version, API range, zero permissions, portable DEX; {apk.stat().st_size} bytes')
