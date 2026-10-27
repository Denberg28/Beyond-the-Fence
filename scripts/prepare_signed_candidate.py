#!/usr/bin/env python3
"""Verify public signed payloads against a fresh build; never receives private keys."""
import hashlib, json, os, pathlib, re, subprocess, sys, tempfile, zipfile

manifest, signer = pathlib.Path(sys.argv[1]), sys.argv[2]
data = json.loads(manifest.read_text())
source = subprocess.check_output(['git', 'rev-parse', 'HEAD'], text=True).strip()
assert data['source_sha'] == source
pin = pathlib.Path('docs/signing-certificate.sha256').read_text().strip()
assert re.fullmatch('[0-9a-f]{64}', pin) and data['signer_sha256'] == pin
repo = os.environ['GITHUB_REPOSITORY']
run = json.loads(subprocess.check_output(['gh', 'api', f'repos/{repo}/actions/runs/{int(data["source_run_id"])}']))
assert run['head_sha'] == source and run['status'] == 'completed' and run['conclusion'] == 'success'

def entries(path):
    with zipfile.ZipFile(path) as z:
        assert z.testzip() is None
        return {n: z.read(n) for n in z.namelist() if not n.endswith('/') and not n.startswith('META-INF/')}

def certificate(path):
    report = subprocess.check_output(['java', '-jar', signer, 'verify', '--print-certs', str(path)], text=True)
    match = re.search(r'Signer #1 certificate SHA-256 digest: ([0-9a-f]+)', report)
    assert match and match[1] == pin, 'Unexpected signing certificate'

def blob(field, target):
    sha = data[field + '_blob']
    assert re.fullmatch('[0-9a-f]{40}', sha)
    raw = subprocess.check_output(['gh', 'api', '-H', 'Accept: application/vnd.github.raw+json', f'repos/{repo}/git/blobs/{sha}'])
    assert hashlib.sha256(raw).hexdigest() == data[field + '_sha256'], field + ' checksum'
    pathlib.Path(target).write_bytes(raw)
    certificate(target)

with tempfile.TemporaryDirectory() as folder:
    for field, target in [('apk', 'app/build/outputs/apk/debug/app-debug.apk'), ('tests', 'app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk')]:
        signed = pathlib.Path(folder) / (field + '.apk')
        blob(field, signed)
        assert entries(target) == entries(signed), field + ' differs from fresh source build'
        pathlib.Path(target).write_bytes(signed.read_bytes())
    # The old package is re-signed ONLY for the emulator update test. It cannot update old phones.
    baseline = pathlib.Path(folder) / 'baseline.apk'
    blob('baseline', baseline)
    original = pathlib.Path(folder) / 'original.apk'
    subprocess.run(['gh', 'release', 'download', 'v0.11.1-rc.1', '--repo', repo, '--pattern', 'Beyond-the-Fence-0.11.1-rc.1-Preview.apk', '--output', str(original)], check=True)
    assert hashlib.sha256(original.read_bytes()).hexdigest() == '5814f5dde0a9a071e25d34c7143dacd21c12c1fa308e7ac0a614784cc105e1b4'
    assert entries(original) == entries(baseline), 'Upgrade fixture must retain released0.11.1 code'
    pathlib.Path('artifacts').mkdir(exist_ok=True)
    pathlib.Path('artifacts/Upgrade-Baseline.apk').write_bytes(baseline.read_bytes())
print('PASS: fixed certificate, passed source run, exact compiled content and upgrade baseline')
