#!/usr/bin/env bash
# Screenshots the launcher and the app drawer so the installed app icon can be checked on a real launcher.
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

log "launch once so the launcher knows the app, then go Home"
fresh
ui tap "Skip"; sleep 3; clear_anr
adb shell input keyevent KEYCODE_HOME; sleep 5; clear_anr
shot launcher_home

log "open the app drawer"
adb shell input swipe 540 2100 540 500 400; sleep 3; clear_anr
shot app_drawer
adb shell input swipe 540 1800 540 700 400; sleep 2
shot app_drawer_scrolled
adb shell input keyevent KEYCODE_HOME; sleep 2
adb logcat -d -v threadtime | grep -a -E 'AndroidRuntime|FATAL EXCEPTION' | tail -n 200 > "$OUT/logcat_app.txt"
log "done: $count screenshots"
exit 0
