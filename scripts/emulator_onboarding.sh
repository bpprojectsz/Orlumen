#!/usr/bin/env bash
# Onboarding pages in all ten languages, then the Diagnostics consent line for Japanese and Spanish.
set -u
PKG=com.zdmgold.orlumen
OUT="${1:-emu-out}"
export EMU_OUT="$OUT"
APK=app/build/outputs/apk/debug/app-debug.apk
mkdir -p "$OUT/shots" "$OUT/ui"
count=0
log() { echo "[$(date +%T)] $*" | tee -a "$OUT/flow.log"; }
ui() { python3 scripts/emu_ui.py "$@" 2>&1 | tee -a "$OUT/flow.log"; }
shot() {
  count=$((count + 1))
  local name; name="$(printf '%02d_%s' "$count" "$1")"
  adb exec-out screencap -p > "$OUT/shots/$name.png"
  python3 scripts/emu_ui.py texts > "$OUT/ui/$name.txt" 2>&1
  log "shot $name"
}
# The slow emulator sometimes shows a "not responding" dialog over everything; dismiss it with Wait.
clear_anr() {
  for _ in 1 2 3; do
    python3 scripts/emu_ui.py has "responding" > /dev/null || return 0
    python3 scripts/emu_ui.py tap "Wait" > /dev/null; sleep 2
  done
}
fresh() { adb shell am start -S -n "$PKG/.MainActivity" > /dev/null; sleep 4; clear_anr; }
setlocale() { adb shell cmd locale set-app-locales "$PKG" --locales "$1" | tee -a "$OUT/flow.log"; sleep 4; }
swipe_up() { adb shell input swipe 540 1800 540 600 500; sleep 1; }

adb shell settings put global hide_error_dialogs 1
adb shell settings put global window_animation_scale 0
adb shell settings put global transition_animation_scale 0
adb shell settings put global animator_duration_scale 0
sleep 45
adb install -r "$APK" | tee -a "$OUT/flow.log"
adb shell pm grant "$PKG" android.permission.POST_NOTIFICATIONS

for L in ar he de ja es fr pt hi ko zh; do
  log "ONBOARDING $L"
  # Clearing the app data also resets the app language, so the language is set afterwards.
  adb shell pm clear "$PKG" > /dev/null; setlocale "$L"; fresh
  clear_anr
  shot "${L}_onboarding_1"
  for page in 2 3 4; do
    clear_anr
    adb shell input tap 540 2206; sleep 2
    clear_anr
    shot "${L}_onboarding_$page"
  done
done

setlocale en; adb shell pm clear "$PKG" > /dev/null; fresh
clear_anr
ui tap "Skip"; sleep 3
clear_anr
for L in ja es; do
  log "DIAGNOSTICS $L"
  setlocale en; fresh
  ui scroll-tap "Check audio, permissions"; sleep 3
  setlocale "$L"
  shot "${L}_diagnostics"; swipe_up; swipe_up; swipe_up; shot "${L}_diagnostics_ads"
done
adb logcat -d -v threadtime | grep -a -E 'AndroidRuntime|FATAL EXCEPTION' | tail -n 200 > "$OUT/logcat_app.txt"
log "done: $count screenshots"
exit 0
