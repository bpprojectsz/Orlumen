#!/usr/bin/env bash
# Checks the edge lights over the launcher in three situations: option off, option switched on while the lights run,
# and the lights restarted with the option on. The service state is saved each time, so "not running" can be told
# apart from "running but not drawn".
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

log "A: start the lights with the option off, then look at the launcher"
ui scroll-tap "Start edge lights"; sleep 5
shot home_running
service_state A
adb shell input keyevent KEYCODE_HOME; sleep 4
shot A_launcher_option_off

log "B: switch the option on while the lights run, then look at the launcher"
open_app
ui scroll-tap-exact "Settings"; sleep 3
ui scroll-tap "Keep clear of the status bar"; sleep 3
shot setting_on
service_state B
adb shell input keyevent KEYCODE_HOME; sleep 4
shot B_launcher_option_on_live

log "C: restart the lights with the option on"
open_app
shot app_before_restart
ui scroll-tap-exact "Stop"; sleep 3
ui scroll-tap "Start edge lights"; sleep 5
service_state C
adb shell input keyevent KEYCODE_HOME; sleep 4
shot C_launcher_option_on_restarted

PID=$(adb shell pidof "$PKG" | tr -d '\r')
adb logcat -d -v threadtime --pid="$PID" | tail -n 3000 > "$OUT/logcat_pid.txt"
adb logcat -d -v threadtime | grep -a -E 'AndroidRuntime|FATAL EXCEPTION' | tail -n 200 > "$OUT/logcat_app.txt"
log "done: $count screenshots"
exit 0
