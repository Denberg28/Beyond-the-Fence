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
adb install -r artifacts/Beyond-the-Fence-0.5.0-rc.1-Preview.apk
adb install -r artifacts/Beyond-the-Fence-0.5.0-rc.1-Tests.apk
adb shell pm clear com.denberg.beyondthefence
timeout 300s adb shell am instrument -w com.denberg.beyondthefence.test/com.denberg.beyondthefence.SmokeInstrumentation | tee evidence/instrumentation.txt
grep -Eq '^OK \([0-9]+ checks\)' evidence/instrumentation.txt
if grep -q 'FAILURES!!!' evidence/instrumentation.txt; then exit 1; fi
for scene in roaming farm night landscape upgrades-landscape large-font breach rebuilt; do
  adb exec-out run-as com.denberg.beyondthefence cat "files/smoke-$scene.png" > "evidence/$scene.png"
done
adb logcat -d -s AndroidRuntime:E > evidence/crash-log.txt
if grep -q 'FATAL EXCEPTION' evidence/crash-log.txt; then exit 1; fi
