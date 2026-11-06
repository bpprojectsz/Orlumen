#!/usr/bin/env bash
# Opens real screens in English, switches the app language, and screenshots the result.
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
setlocale() { adb shell cmd locale set-app-locales "$PKG" --locales "$1" | tee -a "$OUT/flow.log"; sleep 4; }
swipe_up() { adb shell input swipe 540 1800 540 600 500; sleep 1; }

adb shell settings put global hide_error_dialogs 1
adb shell settings put global window_animation_scale 0
adb shell settings put global transition_animation_scale 0
adb shell settings put global animator_duration_scale 0
sleep 45
adb install -r "$APK" | tee -a "$OUT/flow.log"
adb shell pm grant "$PKG" android.permission.POST_NOTIFICATIONS
fresh
ui tap "Skip"; sleep 2

for L in ar he de ja; do
  log "LANGUAGE $L"

  setlocale en; fresh
  ui scroll-tap-exact "Settings"; sleep 3
  setlocale "$L"
  shot "${L}_settings_top"; swipe_up; shot "${L}_settings_middle"; swipe_up; shot "${L}_settings_bottom"

  setlocale en; fresh
  ui scroll-tap-exact "Edge lights"; sleep 3
  ui scroll-tap "Strand"; sleep 3
  setlocale "$L"
  shot "${L}_editor_top"; swipe_up; swipe_up; shot "${L}_editor_bottom"

  setlocale en; fresh
  ui scroll-tap "Check audio, permissions"; sleep 3
  setlocale "$L"
  shot "${L}_diagnostics"

  setlocale en; fresh
  setlocale "$L"; sleep 2
  shot "${L}_home"
  swipe_up; shot "${L}_home_bottom"
done
adb logcat -d -v threadtime | grep -a -E 'AndroidRuntime|FATAL EXCEPTION' | tail -n 200 > "$OUT/logcat_app.txt"
log "done: $count screenshots"
exit 0
