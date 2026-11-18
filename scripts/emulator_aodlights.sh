#!/usr/bin/env bash
# Checks whether the edge lights come back after the always-on screen was opened and then left with Home.
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
service_state() {
  adb shell dumpsys activity services "$PKG" > "$OUT/services_$1.txt" 2>&1
  log "service $1: $(grep -c 'OrlumenRuntimeService' "$OUT/services_$1.txt") mentions, foreground: $(grep -c 'isForeground=true' "$OUT/services_$1.txt")"
}
clear_anr() {
  for _ in 1 2 3; do
    python3 scripts/emu_ui.py has "responding" > /dev/null || return 0
    python3 scripts/emu_ui.py tap "Wait" > /dev/null; sleep 2
  done
}
open_app() { adb shell am start -n "$PKG/.MainActivity" > /dev/null; sleep 3; clear_anr; }

adb shell settings put global hide_error_dialogs 1
adb shell settings put global window_animation_scale 0
adb shell settings put global transition_animation_scale 0
adb shell settings put global animator_duration_scale 0
adb shell settings put secure immersive_mode_confirmations confirmed
sleep 45
adb install -r "$APK" | tee -a "$OUT/flow.log"
adb shell pm grant "$PKG" android.permission.RECORD_AUDIO
adb shell pm grant "$PKG" android.permission.POST_NOTIFICATIONS
adb shell appops set "$PKG" SYSTEM_ALERT_WINDOW allow
adb shell appops set "$PKG" GET_USAGE_STATS allow
adb shell am start -S -n "$PKG/.MainActivity" > /dev/null; sleep 5; clear_anr
ui tap "Skip"; sleep 3; clear_anr
shot home

activity_state() {
  adb shell dumpsys activity activities > "$OUT/activities_$1.txt" 2>&1
  log "activities $1: AodActivity mentions=$(grep -c 'AodActivity' "$OUT/activities_$1.txt")"
  grep -a -E 'AodActivity|ResumedActivity' "$OUT/activities_$1.txt" | head -6 | tee -a "$OUT/flow.log"
}

log "1: start the lights, look at the launcher"
ui scroll-tap "Start edge lights"; sleep 5
service_state 1
adb shell input keyevent KEYCODE_HOME; sleep 4
shot launcher_before_aod

log "2: open the always-on screen from Diagnostics"
open_app
ui scroll-tap "Diagnostics"; sleep 3
ui scroll-tap "Open the always-on screen now"; sleep 5
shot aod_showing
activity_state 2

log "3: leave the always-on screen with Home (not by tapping it)"
adb shell input keyevent KEYCODE_HOME; sleep 5
shot launcher_after_aod
service_state 3
activity_state 3

log "4: open the app again and read the Diagnostics status"
open_app
shot app_after_aod
ui scroll-tap "Diagnostics"; sleep 3
shot diagnostics_after_aod
adb shell input keyevent KEYCODE_HOME; sleep 4
shot launcher_end

PID=$(adb shell pidof "$PKG" | tr -d '\r')
adb logcat -d -v threadtime --pid="$PID" | tail -n 3000 > "$OUT/logcat_pid.txt"
adb logcat -d -v threadtime | grep -a -E 'AndroidRuntime|FATAL EXCEPTION' | tail -n 200 > "$OUT/logcat_app.txt"
log "done: $count screenshots"
exit 0
