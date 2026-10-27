#!/usr/bin/env bash
set -euo pipefail
mkdir -p evidence
adb shell settings put system font_scale 1.0
collect_diagnostics() {
  adb logcat -d -s AndroidRuntime:E > evidence/crash-log.txt || true
  adb shell dumpsys activity activities > evidence/activities.txt || true
  adb exec-out screencap -p > evidence/final-screen.png || true
  adb shell settings put system font_scale 1.0 || true
  adb logcat -d -s BTFSmoke:I > evidence/touch-log.txt || true
  adb shell dumpsys activity lastanr > evidence/last-anr.txt || true
  adb exec-out run-as com.denberg.beyondthefence cat files/smoke-failure.png > evidence/failure.png || true
  adb exec-out run-as com.denberg.beyondthefence cat files/smoke-window.txt > evidence/failure-window.txt || true
}
trap collect_diagnostics EXIT
if [ -f artifacts/Upgrade-Baseline.apk ]; then
  adb install -r artifacts/Upgrade-Baseline.apk
  adb shell pm clear com.denberg.beyondthefence
  adb shell am start -W -n com.denberg.beyondthefence/.MainActivity
  adb shell am force-stop com.denberg.beyondthefence
  base64 -d app/src/test/resources/schema7.b64 > evidence/upgrade-fixture.btfs
  adb push evidence/upgrade-fixture.btfs /data/local/tmp/btf-upgrade.btfs
  adb shell chmod 644 /data/local/tmp/btf-upgrade.btfs
  adb shell run-as com.denberg.beyondthefence cp /data/local/tmp/btf-upgrade.btfs files/farm-a.btfs
  adb shell run-as com.denberg.beyondthefence rm -f files/farm-b.btfs
fi
adb install -r artifacts/TAG-Zombie-Survival-Fantasy-0.13.0-rc.1-Preview.apk
adb install -r artifacts/TAG-Zombie-Survival-Fantasy-0.13.0-rc.1-Tests.apk
if [ -f artifacts/Upgrade-Baseline.apk ]; then
  timeout 60s adb shell am instrument -w -e upgradeOnly true com.denberg.beyondthefence.test/com.denberg.beyondthefence.SmokeInstrumentation | tee evidence/upgrade.txt
  grep -Eq '^OK \([0-9]+ upgrade checks\)' evidence/upgrade.txt
fi
adb shell pm clear com.denberg.beyondthefence
timeout 420s adb shell am instrument -w com.denberg.beyondthefence.test/com.denberg.beyondthefence.SmokeInstrumentation | tee evidence/instrumentation.txt
grep -Eq '^OK \([0-9]+ checks\)' evidence/instrumentation.txt
if grep -q 'FAILURES!!!' evidence/instrumentation.txt; then exit 1; fi
for scene in roaming farm night landscape upgrades-landscape large-font breach rebuilt materials survival shop compact barracks items village castle tabs boss potions century equipment arena-offer arena rift; do
  adb exec-out run-as com.denberg.beyondthefence cat "files/smoke-$scene.png" > "evidence/$scene.png"
done
if [ -f artifacts/TAG-Zombie-Survival-Fantasy-0.13.0-rc.1-Release.apk ]; then
  # Seed the installed preview's real data, then upgrade to the non-debuggable release.
  adb shell am force-stop com.denberg.beyondthefence
  base64 -d app/src/test/resources/schema7.b64 > evidence/release-upgrade-fixture.btfs
  adb push evidence/release-upgrade-fixture.btfs /data/local/tmp/btf-release-upgrade.btfs
  adb shell chmod 644 /data/local/tmp/btf-release-upgrade.btfs
  adb shell run-as com.denberg.beyondthefence cp /data/local/tmp/btf-release-upgrade.btfs files/farm-a.btfs
  adb shell run-as com.denberg.beyondthefence rm -f files/farm-b.btfs
  adb install -r artifacts/TAG-Zombie-Survival-Fantasy-0.13.0-rc.1-Release.apk
  timeout 60s adb shell am instrument -w -e upgradeOnly true com.denberg.beyondthefence.test/com.denberg.beyondthefence.SmokeInstrumentation | tee evidence/release-upgrade.txt
  grep -Eq '^OK \([0-9]+ upgrade checks\)' evidence/release-upgrade.txt
fi
adb logcat -d -s AndroidRuntime:E > evidence/crash-log.txt
if grep -q 'FATAL EXCEPTION' evidence/crash-log.txt; then exit 1; fi
