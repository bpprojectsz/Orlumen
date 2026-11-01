# Foreground service and permission declarations

Orlumen uses one foreground service, `OrlumenRuntimeService`, declared with three types. Play Console asks for a description and demo video for each. Use the text below.

## microphone
**What the user does:** taps Start edge lights, grants the microphone permission, and plays music.
**Why a foreground service:** the lights must keep animating while the user is in other apps with the screen on. Android requires the microphone type for reading the device audio output level.
**What it does with audio:** reads the output level and its frequency bands in memory to animate lights. Audio is never recorded, stored or sent.
**User control:** an ongoing notification with a Stop action, a Stop button in the app, and a Quick Settings tile.
**Demo video (about 30 s):** open Orlumen, tap Start edge lights, show the permission prompt, play music, show the lights over another app, pull down the notification, tap Stop.

## mediaProjection
**What the user does:** optionally selects "App audio capture" in Settings or taps "Start with app audio capture", then accepts Android's screen-capture prompt.
**Why:** on devices where the global audio mix returns silence, Android only allows capturing other apps' playback audio through the media projection API.
**What it does:** captures audio output only, for the same on-device analysis. No video or screen content is captured, stored or sent.
**User control:** Android asks for consent every time; the user can stop from the notification or app.
**Demo video:** Settings → Audio source → App audio capture → Start edge lights → accept the Android prompt → lights react → Stop.

## specialUse (subtype `always_on_display_clock`)
**What the user does:** turns on Always-on display, then runs the always-on mode without edge lights.
**Why:** a screen-off clock must be triggered when the screen turns off, which requires a running component while the app is not in front. No other foreground service type describes a clock shown when the screen is off.
**What it does:** listens for the screen-off event and opens the clock screen. No audio or microphone is used in this mode.
**User control:** the same notification Stop action and in-app switch.
**Demo video:** turn on Always-on display, press the power button, show the clock appearing, tap to return.

## Other sensitive access (be ready if review asks)
- **Display over other apps (SYSTEM_ALERT_WINDOW):** core feature, draws the edge lights above other apps. The overlay is not touchable and shows no ads.
- **Notification access (NotificationListenerService):** used only to connect to active media sessions for track title, artist, artwork and playback controls. Optional, with an in-app explanation before the system settings page opens.
- **Usage access (PACKAGE_USAGE_STATS):** used only to detect the app in front so lights can hide over full-screen apps or show only over chosen apps. Optional.
- **Package visibility:** the app lists launchable apps through a launcher-intent `<queries>` entry, not QUERY_ALL_PACKAGES.
