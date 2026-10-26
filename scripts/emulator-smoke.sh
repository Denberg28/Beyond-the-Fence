#!/usr/bin/env bash
set -euo pipefail
mkdir -p evidence
collect_diagnostics() {
  adb logcat -d -s AndroidRuntime:E > evidence/crash-log.txt || true
  adb shell dumpsys activity activities > evidence/activities.txt || true
  adb exec-out screencap -p > evidence/final-screen.png || true
}
trap collect_diagnostics EXIT
adb install -r artifacts/Beyond-the-Fence-0.1.0-rc.2-Preview.apk
adb install -r artifacts/Beyond-the-Fence-0.1.0-rc.2-Tests.apk
adb shell pm clear com.denberg.beyondthefence
adb shell am instrument -w com.denberg.beyondthefence.test/com.denberg.beyondthefence.SmokeInstrumentation | tee evidence/instrumentation.txt
grep -Eq '^OK \([0-9]+ checks\)' evidence/instrumentation.txt
if grep -q 'FAILURES!!!' evidence/instrumentation.txt; then exit 1; fi
adb exec-out run-as com.denberg.beyondthefence cat files/smoke-farm.png > evidence/farm.png
adb logcat -d -s AndroidRuntime:E > evidence/crash-log.txt
if grep -q 'FATAL EXCEPTION' evidence/crash-log.txt; then exit 1; fi
