#!/usr/bin/env bash
# Checks the full-screen ad moments and their spacing on a device: Apply shows an interstitial, a preset saved
# within five minutes does not, and a palette saved after the wait does.
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
on_home() { python3 scripts/emu_ui.py has "Show a clock when the screen turns off" > /dev/null; }
# Back out of screens and ads; if Back cannot reach Home, restart the app (the ad history is stored on disk).
to_home() {
  for i in 1 2 3 4 5 6; do
    clear_anr
    on_home && return 0
    if [ "$i" -le 3 ]; then adb shell input keyevent KEYCODE_BACK; sleep 2; else fresh; fi
  done
  log "could not get back to Home"
}
open_strand_editor() {
  ui tap-near "Always-on clock" -339 -120; sleep 3
  ui scroll-tap "Strand"; sleep 3
}

adb shell settings put global hide_error_dialogs 1
adb shell settings put global window_animation_scale 0
adb shell settings put global transition_animation_scale 0
adb shell settings put global animator_duration_scale 0
adb shell settings put secure immersive_mode_confirmations confirmed
sleep 45
adb install -r "$APK" | tee -a "$OUT/flow.log"
adb shell pm grant "$PKG" android.permission.POST_NOTIFICATIONS
fresh
clear_anr
ui tap "Skip"; sleep 3
clear_anr
shot home

log "STEP 1: Apply in the edge-light editor (first ad of the run, expected to show)"
open_strand_editor
shot editor_top
ui scroll-tap "Apply"; sleep 8
shot apply_ad
FIRST_AD=$(date +%s)
adb shell input keyevent KEYCODE_BACK; sleep 2
shot after_apply_ad_back
to_home
shot home_again

log "STEP 2: save a preset within five minutes of the first ad (expected: no ad)"
open_strand_editor
ui scroll-tap "Preset name"; sleep 1
adb shell input text GapTest; sleep 1
adb shell input keyevent 111; sleep 1
ui scroll-tap "Save preset"; sleep 6
shot preset_within_five_minutes
log "seconds since the first ad: $(( $(date +%s) - FIRST_AD ))"
to_home

log "STEP 3: wait until five minutes have passed since the first ad"
while [ $(( $(date +%s) - FIRST_AD )) -lt 310 ]; do sleep 10; done
log "seconds since the first ad: $(( $(date +%s) - FIRST_AD ))"

log "STEP 4: save a palette after the wait (expected to show an ad)"
ui scroll-tap-exact "Colors"; sleep 3
ui tap "Mine"; sleep 2
shot palette_creator
ui scroll-tap "Palette name"; sleep 1
adb shell input text GapPalette; sleep 1
adb shell input keyevent 111; sleep 1
ui scroll-tap "Save palette"; sleep 8
shot palette_after_wait
adb shell input keyevent KEYCODE_BACK; sleep 2
shot after_palette_ad_back
to_home
shot home_end

adb logcat -d -v threadtime | grep -a -E 'AndroidRuntime|FATAL EXCEPTION' | tail -n 200 > "$OUT/logcat_app.txt"
log "done: $count screenshots"
exit 0
