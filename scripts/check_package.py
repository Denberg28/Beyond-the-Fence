#!/usr/bin/env python3
"""Fail closed on unexpected identity, capability, or oversized preview packaging."""
import array, io, pathlib, subprocess, sys, wave, zipfile

aapt, filename = sys.argv[1:]
apk = pathlib.Path(filename)
assert 0 < apk.stat().st_size <= 5 * 1024 * 1024, 'Preview must be <=5MiB'
badging = subprocess.check_output([aapt, 'dump', 'badging', str(apk)], text=True)
assert "package: name='com.denberg.beyondthefence'" in badging
assert "versionCode='13'" in badging and "versionName='0.11.0-rc.1'" in badging
assert "sdkVersion:'26'" in badging and "targetSdkVersion:'35'" in badging
permissions = subprocess.check_output([aapt, 'dump', 'permissions', str(apk)], text=True)
assert 'uses-permission:' not in permissions, permissions
with zipfile.ZipFile(apk) as z:
    lengths = {'sunny': 705600, 'creepy': 705600, 'shot': 3969, 'zombie': 30870, 'owl': 39690, 'cricket': 35280}
    audio = []
    for name, frames in lengths.items():
        data = z.read('res/raw/' + name + '.wav'); audio.append(data)
        with wave.open(io.BytesIO(data), 'rb') as score:
            assert score.getparams()[:4] == (1, 2, 22050, frames), name + ' format/length'
            samples = array.array('h', score.readframes(score.getnframes()))
            if sys.byteorder != 'little': samples.byteswap()
            assert 1000 < max(abs(s) for s in samples) < 32767, name + ' non-silence/no clipping'
            assert abs(samples[0]) < 100 and abs(samples[-1]) < 100, name + ' quiet boundaries'
    assert len(set(audio)) == 6, 'Six distinct original scores/effects'
    assert not any(n.endswith(('.so', '.jks', '.keystore')) for n in z.namelist())
print(f'PASS: package, version, API range, zero permissions, portable DEX; {apk.stat().st_size} bytes')
