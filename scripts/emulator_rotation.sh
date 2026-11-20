#!/usr/bin/env bash
# Checks that onboarding keeps its page when the language changes and when the phone is rotated.
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

log "start onboarding on page 3"
adb shell pm clear "$PKG" > /dev/null; fresh
clear_anr
for page in 2 3; do adb shell input tap 540 2206; sleep 2; clear_anr; done
shot page3_before

log "change the language to Arabic while on page 3"
setlocale ar; sleep 4; clear_anr
shot page3_after_language_change

log "rotate to landscape while on page 3"
adb shell settings put system accelerometer_rotation 0
adb shell settings put system user_rotation 1; sleep 4; clear_anr
shot page3_landscape
adb shell settings put system user_rotation 0; sleep 4; clear_anr
shot page3_back_to_portrait

setlocale en; sleep 3
shot page3_english_again
adb logcat -d -v threadtime | grep -a -E 'AndroidRuntime|FATAL EXCEPTION' | tail -n 200 > "$OUT/logcat_app.txt"
log "done: $count screenshots"
exit 0
