package com.zdmgold.orlumen.core.model

enum class ThemeMode {
    SYSTEM,
    DARK,
    LIGHT
}

enum class AudioSourceMode {
    AUTO,
    GLOBAL_MIX,
    PLAYBACK_CAPTURE
}

enum class AodHostMode {
    ACTIVITY,
    SCREENSAVER
}

data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.DARK,
    val audioSourceMode: AudioSourceMode = AudioSourceMode.AUTO,
    val aodHostMode: AodHostMode = AodHostMode.ACTIVITY,
    val aodEnabled: Boolean = false,
    val isProUser: Boolean = false,
    val dimBackground: Boolean = false,
    val keepScreenOn: Boolean = false,
    val hideOverFullscreen: Boolean = true,
    val autoApplyAlbumColors: Boolean = true,
    val onboardingCompleted: Boolean = false,
    val visualizerAppPackages: Set<String> = emptySet(),
    val musicSourcePackages: Set<String> = emptySet()
)
