package com.zdmgold.orlumen.core.repository

import com.zdmgold.orlumen.core.datastore.PreferencesRepository
import com.zdmgold.orlumen.core.model.ColorPalette
import com.zdmgold.orlumen.core.model.NowPlaying
import com.zdmgold.orlumen.platform.media.AlbumPaletteExtractor
import com.zdmgold.orlumen.platform.media.MediaSessionMonitor
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest

internal fun selectActiveSession(sessions: List<NowPlaying>, allowedPackages: Set<String>): NowPlaying {
    val candidates = if (allowedPackages.isEmpty()) {
        sessions
    } else {
        sessions.filter {
            it.sourcePackage in allowedPackages
        }
    }
    return candidates.firstOrNull { it.isPlaying } ?: candidates.firstOrNull() ?: NowPlaying.idle
}

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class NowPlayingRepository @Inject constructor(
    monitor: MediaSessionMonitor,
    preferencesRepository: PreferencesRepository,
    private val extractor: AlbumPaletteExtractor
) {
    val nowPlaying: Flow<NowPlaying> = combine(
        monitor.sessions,
        preferencesRepository.preferences.map { it.musicSourcePackages }.distinctUntilChanged()
    ) { sessions, allowed -> selectActiveSession(sessions, allowed) }

    val albumPalette: Flow<ColorPalette?> = nowPlaying
        .distinctUntilChangedBy { listOf(it.sourcePackage, it.title, it.artist, it.albumArt) }
        .mapLatest { current ->
            current.albumArt?.let { art ->
                extractor.extract("${current.sourcePackage}|${current.title}|${current.artist}", art)
            }
        }
}
