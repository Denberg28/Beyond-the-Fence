# Signed public delivery payloads for0.16.0-rc.1

Source:7a71c41601190643ebc8dd0219da035e57712fad; passed source run40789687988.
117 JVM tests per variant; build/lint/package checks;2305 installed-app checks on each API26/35. Pond/planting/wolf screenshots self-reviewed. These payloads are signed using the retained private certificate; no private key or password is stored here. The main release pipeline must compare every compiled entry to a fresh build and repeat signed installed-app/upgrade checks before publication. Physical-phone validation remains pending. Upgrade-Baseline.apk is the retained re-signed0.11.1 fixture, for emulator testing only.
