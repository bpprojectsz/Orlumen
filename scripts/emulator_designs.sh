#!/usr/bin/env bash
# Opens both design galleries on a clean launch and scrolls through every design.
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
fresh() { adb shell am start -S -n "$PKG/.MainActivity" > /dev/null; sleep 4; }
swipe_up() { adb shell input swipe 540 1900 540 400 600; sleep 1; }

adb shell settings put global hide_error_dialogs 1
adb shell settings put global window_animation_scale 0
adb shell settings put global transition_animation_scale 0
adb shell settings put global animator_duration_scale 0
sleep 45
adb install -r "$APK" | tee -a "$OUT/flow.log"
fresh
ui tap "Skip"; sleep 2

fresh
shot home
ui scroll-tap-exact "Edge lights"; sleep 3
shot visualizer_gallery_1
for n in 2 3 4 5; do swipe_up; shot "visualizer_gallery_$n"; done

fresh
ui scroll-tap-exact "Always-on clock"; sleep 3
shot aod_gallery_1
for n in 2 3 4 5; do swipe_up; shot "aod_gallery_$n"; done

fresh
ui scroll-tap-exact "Edge lights"; sleep 3
ui scroll-tap-exact "Comet"; sleep 3
shot comet_editor
fresh
ui scroll-tap-exact "Edge lights"; sleep 3
ui scroll-tap-exact "Prism"; sleep 3
shot prism_editor
fresh
ui scroll-tap-exact "Edge lights"; sleep 3
ui scroll-tap-exact "Spark"; sleep 3
shot spark_editor
fresh
ui scroll-tap-exact "Always-on clock"; sleep 3
ui scroll-tap-exact "Binary"; sleep 3
shot binary_editor
fresh
ui scroll-tap-exact "Always-on clock"; sleep 3
ui scroll-tap-exact "Horizon"; sleep 3
shot horizon_editor
fresh
ui scroll-tap-exact "Always-on clock"; sleep 3
ui scroll-tap-exact "Gauge"; sleep 3
shot gauge_editor
adb logcat -d -v threadtime | grep -a -E 'AndroidRuntime|FATAL EXCEPTION' | tail -n 200 > "$OUT/logcat_app.txt"
log "done: $count screenshots"
exit 0
