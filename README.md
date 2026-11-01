# Orlumen

Android app that turns music into edge lights and an always-on clock. Application ID `com.zdmgold.orlumen`. Kotlin, Jetpack Compose, Hilt, Room, DataStore.

## Features
- Six edge-light designs and eight always-on clocks, each with its own options
- Palettes: stock, custom, album art and wallpaper colors
- Audio pipeline with global-mix capture and a playback-capture fallback
- Now-playing detection, quick settings tile, optional app filters
- Ads (AdMob with UMP consent) and a one-time Pro purchase that removes them

## Project layout
- `app/src/main/java/com/zdmgold/orlumen/core`: models, theme, database, repositories, view models
- `.../platform`: audio, overlay, AOD, media, billing, ads, consent
- `.../designs`: edge-light and always-on design renderers
- `.../ui`: components and screens
- `website/`: static site for Cloudflare Pages (landing, privacy, terms, support)
- `store_submission/`: Play listing copy, Data safety draft, foreground-service declarations
- `scripts/`: emulator test helpers

## Workflows
- `light_check.yml`: on every push to `main`, runs ktlint, detekt, a Kotlin compile and unit tests
- `build_android.yml`: manual only. Builds the signed release APK and AAB
- `emulator_check.yml`: manual only. Runs the app on an Android emulator, drives the screens and publishes screenshots to the `ci-screenshots` branch

## Release signing
Create these repository secrets (Settings, Secrets and variables, Actions): `ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, `ANDROID_KEY_PASSWORD`. Without them the release build falls back to debug signing.

Optional repository variables: `WEBSITE_BASE_URL` (shows privacy and terms links in the About screen), and real AdMob IDs once your account exists. Google's public test IDs are the default.

## Local build
Requires JDK 17 and the Android SDK (platform 36).

    ./gradlew ktlintCheck detekt testDebugUnitTest assembleDebug

## Website
Deploy the `website/` folder with Cloudflare Pages (no build command, output directory `website`).
