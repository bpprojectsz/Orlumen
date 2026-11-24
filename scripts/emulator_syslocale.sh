#!/usr/bin/env bash
# First launch after the SYSTEM language is changed: Arabic (right to left), Japanese, and Italian (not supported, so it should fall back to English).
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

set_system_locale() {   # $1 = language tag such as ar-SA
  adb root > /dev/null 2>&1; sleep 3
  adb shell setprop persist.sys.locale "$1"
  adb shell setprop ctl.restart zygote
  sleep 20
  adb wait-for-device
  for _ in $(seq 1 60); do
    adb shell pm path android > /dev/null 2>&1 && break
    sleep 3
  done
  sleep 30
  log "system locale property: $(adb shell getprop persist.sys.locale | tr -d '\r')"
  adb shell am get-config 2>/dev/null | head -2 | tee -a "$OUT/flow.log"
  adb shell settings put global hide_error_dialogs 1
  clear_anr
}

for LOC in ar-SA ja-JP it-IT; do
  TAG="${LOC%%-*}"
  log "SYSTEM LANGUAGE $LOC"
  set_system_locale "$LOC"
  adb shell pm clear "$PKG" > /dev/null
  fresh
  clear_anr
  shot "${TAG}_onboarding_1"
  for page in 2 3 4; do
    clear_anr
    adb shell input tap 540 2206; sleep 2
    clear_anr
    shot "${TAG}_onboarding_$page"
  done
  clear_anr
  adb shell input tap 540 2206; sleep 8
  clear_anr
  shot "${TAG}_home"
done
set_system_locale en-US
adb logcat -d -v threadtime | grep -a -E 'AndroidRuntime|FATAL EXCEPTION' | tail -n 200 > "$OUT/logcat_app.txt"
log "done: $count screenshots"
exit 0
