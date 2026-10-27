#!/usr/bin/env python3
"""Sign audited preview/tests and the emulator upgrade fixture using a private local key."""
import argparse, hashlib, pathlib, re, subprocess, zipfile

p=argparse.ArgumentParser()
p.add_argument('--candidate',type=pathlib.Path,required=True)
p.add_argument('--keystore',type=pathlib.Path,required=True)
p.add_argument('--password-file',type=pathlib.Path,required=True)
p.add_argument('--baseline',type=pathlib.Path,required=True)
p.add_argument('--output',type=pathlib.Path,required=True)
a=p.parse_args(); a.output.mkdir(parents=True,exist_ok=True)
jar=a.candidate/'apksigner.jar'
pin=pathlib.Path('docs/signing-certificate.sha256').read_text().strip()
for original in [*a.candidate.glob('*Preview.apk'),*a.candidate.glob('*Tests.apk'),a.baseline]:
    name='Upgrade-Baseline.apk' if original==a.baseline else original.name
    target=a.output/name
    subprocess.run(['java','-jar',str(jar),'sign','--ks',str(a.keystore),'--ks-key-alias','fence',
        '--ks-pass','file:'+str(a.password_file),
        '--out',str(target),str(original)],check=True,capture_output=True)
    report=subprocess.check_output(['java','-jar',str(jar),'verify','--print-certs',str(target)],text=True)
    match=re.search(r'Signer #1 certificate SHA-256 digest: ([0-9a-f]+)',report)
    assert match and match[1]==pin,'Wrong private signing identity'
    with zipfile.ZipFile(original) as before,zipfile.ZipFile(target) as after:
        b={n:before.read(n) for n in before.namelist() if not n.startswith('META-INF/')}
        c={n:after.read(n) for n in after.namelist() if not n.startswith('META-INF/')}
        assert b==c,'Signing must not change compiled code or resources'
    print(target.name,hashlib.sha256(target.read_bytes()).hexdigest())
