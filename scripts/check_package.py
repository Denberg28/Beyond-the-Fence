#!/usr/bin/env python3
"""Fail closed on unexpected identity, capability, audio or APK packaging."""
import array, io, pathlib, re, subprocess, sys, wave, zipfile

aapt, filename = sys.argv[1:3]
assert sys.argv[3:] in ([], ['--release']), 'Usage: check_package.py AAPT APK [--release]'
release = sys.argv[3:] == ['--release']
apk = pathlib.Path(filename)
assert 0 < apk.stat().st_size <= 5 * 1024 * 1024, 'Preview must be <=5MiB'
badging = subprocess.check_output([aapt, 'dump', 'badging', str(apk)], text=True)
assert "package: name='com.denberg.beyondthefence'" in badging
assert "versionCode='18'" in badging and "versionName='0.14.0-rc.1'" in badging
assert "sdkVersion:'26'" in badging and "targetSdkVersion:'35'" in badging
assert "application-label:'TAG: Zombie Survival Fantasy'" in badging
if release:
    assert 'application-debuggable' not in badging, 'Release must not be debuggable'
permissions = subprocess.check_output([aapt, 'dump', 'permissions', str(apk)], text=True)
assert 'uses-permission:' not in permissions, permissions
with zipfile.ZipFile(apk) as z:
    lengths = {'sunny': 705600, 'creepy': 705600, 'shot': 3969, 'zombie': 30870, 'owl': 39690, 'cricket': 35280}
    # AAPT2 shortens release paths. Resolve named resources instead of assuming ZIP names.
    resources = subprocess.check_output([aapt, 'dump', '--values', 'resources', str(apk)], text=True)
    paths = dict(re.findall(r'resource [^\n]+com\.denberg\.beyondthefence:raw/(\w+):[^\n]*\n\s+\(string(?:8|16)\) "([^"\n]+)"', resources))
    assert set(paths) == set(lengths), 'Exactly the six expected raw audio resources'
    audio = []
    for name, frames in lengths.items():
        data = z.read(paths[name]); audio.append(data)
        with wave.open(io.BytesIO(data), 'rb') as score:
            assert score.getparams()[:4] == (1, 2, 22050, frames), name + ' format/length'
            samples = array.array('h', score.readframes(score.getnframes()))
            if sys.byteorder != 'little': samples.byteswap()
            assert 1000 < max(abs(s) for s in samples) < 32767, name + ' non-silence/no clipping'
            assert abs(samples[0]) < 100 and abs(samples[-1]) < 100, name + ' quiet boundaries'
    assert len(set(audio)) == 6, 'Six distinct original scores/effects'
    assert not any(n.endswith(('.so', '.jks', '.keystore', '.p12')) for n in z.namelist())
print(f'PASS: package, version, API range, zero permissions, portable DEX; {apk.stat().st_size} bytes')
