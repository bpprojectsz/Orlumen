#!/usr/bin/env bash
# Drives Orlumen on a running emulator and records screenshots, UI text and logs into $1.
# Never aborts on a failed step: every failure is logged so the remaining frames still arrive.
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
  local name
  name="$(printf '%02d_%s' "$count" "$1")"
  adb exec-out screencap -p > "$OUT/shots/$name.png"
  python3 scripts/emu_ui.py texts > "$OUT/ui/$name.txt" 2>&1
  log "shot $name"
}
top_activity() {
  log "top: $(adb shell dumpsys activity activities | grep -E 'topResumedActivity|ResumedActivity' | head -2 | tr '\n' ' ')"
}
launch() { adb shell am start -n "$PKG/.MainActivity" > /dev/null; }

log "device: $(adb shell getprop ro.product.model) api $(adb shell getprop ro.build.version.sdk) $(adb shell wm size | tr '\n' ' ')"
adb shell settings put global hide_error_dialogs 1
adb shell settings put global window_animation_scale 0
adb shell settings put global transition_animation_scale 0
adb shell settings put global animator_duration_scale 0
adb shell input keyevent KEYCODE_WAKEUP
adb shell wm dismiss-keyguard

log "letting the fresh emulator settle"
sleep 45
if python3 scripts/emu_ui.py has "isn't responding" > /dev/null 2>&1; then python3 scripts/emu_ui.py tap "Wait"; fi
log "install"
adb install -r "$APK" | tee -a "$OUT/flow.log"
adb logcat -c

# ---------- Phase 1: first run with nothing granted ----------
log "PHASE 1: cold start, nothing granted"
launch
sleep 5
shot onboarding_1
ui tap "Next"; sleep 1; shot onboarding_2
ui tap "Next"; sleep 1; shot onboarding_3
ui tap "Get started"; sleep 2; shot home_first_open

ui scroll-tap "Start edge lights"; sleep 3
shot start_pressed_nothing_granted
top_activity
ui tap "Allow"; sleep 2
shot audio_system_dialog
ui tap "While using"; sleep 2
shot setup_step_2_overlay
ui tap "Allow"; sleep 3
shot overlay_settings_page
top_activity
adb shell input keyevent KEYCODE_BACK; sleep 2
shot back_from_overlay_settings

# ---------- Phase 2: permissions granted through adb ----------
log "PHASE 2: permissions granted, run the engine"
adb shell pm grant "$PKG" android.permission.RECORD_AUDIO
adb shell pm grant "$PKG" android.permission.POST_NOTIFICATIONS
adb shell appops set "$PKG" SYSTEM_ALERT_WINDOW allow
adb shell appops set "$PKG" GET_USAGE_STATS allow
adb shell cmd notification allow_listener "$PKG/$PKG.platform.media.OrlumenNotificationListener"
adb shell am force-stop "$PKG"
sleep 1
launch
sleep 4
shot home_granted

ui scroll-tap "Start edge lights"; sleep 5
shot home_running
adb shell dumpsys activity services "$PKG" > "$OUT/services.txt" 2>&1

adb shell input keyevent KEYCODE_HOME; sleep 2
shot home_screen_with_lights
adb shell am start -a android.settings.SETTINGS > /dev/null; sleep 3
shot settings_app_with_lights
adb shell input keyevent KEYCODE_HOME; sleep 1

adb shell input swipe 540 0 540 1100 900; sleep 2
shot notification_shade
adb shell input swipe 540 0 540 2000 900; sleep 2
shot quick_settings_expanded
adb shell cmd statusbar collapse; sleep 1
adb shell input keyevent KEYCODE_BACK; sleep 1
adb shell input keyevent KEYCODE_HOME; sleep 1

# ---------- Phase 3: always-on display ----------
log "PHASE 3: always-on display"
adb shell settings put secure immersive_mode_confirmations confirmed
launch; sleep 3
ui tap-class "checkable"; sleep 1
shot aod_toggled
ui scroll-tap "Check audio, permissions"; sleep 2
shot diagnostics_screen
ui scroll-tap "Open the always-on screen now"; sleep 4
shot aod_open_now
top_activity
adb shell input keyevent KEYCODE_BACK; sleep 2
adb shell input keyevent KEYCODE_BACK; sleep 2

log "screen off"
adb shell input keyevent KEYCODE_SLEEP; sleep 5
log "power: $(adb shell dumpsys power | grep -E 'mWakefulness=' | head -1)"
top_activity
shot screen_off_state
adb shell input keyevent KEYCODE_WAKEUP; sleep 3
shot after_wake
top_activity


# ---------- Phase 5: design galleries, editors, palettes ----------
log "PHASE 5: design screens"
adb shell input keyevent KEYCODE_WAKEUP
adb shell cmd statusbar collapse; sleep 1
launch; sleep 3
shot home_final
ui scroll-tap "Pick a design and tune it"; sleep 3
shot visualizer_gallery
ui scroll-tap "Tide"; sleep 3
shot visualizer_editor_top
adb shell input swipe 540 1800 540 500 500; sleep 1
shot visualizer_editor_middle
adb shell input swipe 540 1800 540 300 500; sleep 1
shot visualizer_editor_bottom
adb shell input keyevent KEYCODE_BACK; sleep 1
adb shell input keyevent KEYCODE_BACK; sleep 2
ui scroll-tap "Pick a clock style"; sleep 3
shot aod_gallery
ui scroll-tap "Lunar"; sleep 3
shot aod_editor_top
adb shell input swipe 540 1800 540 500 500; sleep 1
shot aod_editor_middle
adb shell input swipe 540 1800 540 300 500; sleep 1
shot aod_editor_bottom
adb shell input keyevent KEYCODE_BACK; sleep 1
adb shell input keyevent KEYCODE_BACK; sleep 2
ui scroll-tap "Palettes, album art and wallpaper"; sleep 3
shot palettes_stock
ui tap "Mine"; sleep 2
shot palettes_mine
ui tap "Album art"; sleep 2
shot palettes_album
ui tap "Wallpaper"; sleep 2
shot palettes_wallpaper
adb shell input keyevent KEYCODE_BACK; sleep 1

# ---------- Phase 4: about screen and teardown ----------
launch; sleep 3
ui scroll-tap "About Orlumen"; sleep 2
shot about

adb logcat -d -v threadtime > "$OUT/logcat_full.txt"
tail -n 4000 "$OUT/logcat_full.txt" > "$OUT/logcat_tail.txt"
grep -E 'AndroidRuntime|FATAL EXCEPTION|zdmgold|Orlumen' "$OUT/logcat_full.txt" | tail -n 1500 > "$OUT/logcat_app.txt"
rm -f "$OUT/logcat_full.txt"
log "done: $count screenshots"
exit 0
