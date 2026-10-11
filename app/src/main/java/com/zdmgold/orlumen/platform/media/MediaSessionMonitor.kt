package com.zdmgold.orlumen.platform.media

import android.content.ComponentName
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.SystemClock
import android.util.Log
import com.zdmgold.orlumen.core.model.NowPlaying
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val TAG = "MediaSessionMonitor"

private class TrackedSession(val controller: MediaController, val callback: MediaController.Callback)

@Singleton
class MediaSessionMonitor @Inject constructor(private val sessionManager: MediaSessionManager) {
    private val _sessions = MutableStateFlow<List<NowPlaying>>(emptyList())
    val sessions: StateFlow<List<NowPlaying>> = _sessions.asStateFlow()

    @Volatile
    private var tracked: List<TrackedSession> = emptyList()

    private val sessionsListener = MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
        track(controllers.orEmpty())
    }

    fun start(listenerComponent: ComponentName) {
        try {
            sessionManager.addOnActiveSessionsChangedListener(sessionsListener, listenerComponent)
            track(sessionManager.getActiveSessions(listenerComponent))
        } catch (denied: SecurityException) {
            Log.w(TAG, "Notification access is not granted", denied)
            untrackAll()
            _sessions.value = emptyList()
        }
    }

    fun stop() {
        runCatching { sessionManager.removeOnActiveSessionsChangedListener(sessionsListener) }
        untrackAll()
        _sessions.value = emptyList()
    }

    fun controllerFor(packageName: String): MediaController? = tracked.firstOrNull {
        it.controller.packageName == packageName
    }?.controller

    private fun track(active: List<MediaController>) {
        untrackAll()
        tracked = active.map { controller ->
            val callback = object : MediaController.Callback() {
                override fun onPlaybackStateChanged(state: PlaybackState?) = publish()

                override fun onMetadataChanged(metadata: MediaMetadata?) = publish()

                override fun onSessionDestroyed() = publish()
            }
            controller.registerCallback(callback)
            TrackedSession(controller, callback)
        }
        publish()
    }

    private fun untrackAll() {
        tracked.forEach { it.controller.unregisterCallback(it.callback) }
        tracked = emptyList()
    }

    private fun publish() {
        _sessions.value = tracked.map { it.controller.toNowPlaying() }
    }

    private fun MediaController.toNowPlaying(): NowPlaying {
        val data = metadata
        val state = playbackState
        val capturedAt = state?.lastPositionUpdateTime?.takeIf { it > 0 } ?: SystemClock.elapsedRealtime()
        return NowPlaying(
            sourcePackage = packageName,
            title = data?.getString(MediaMetadata.METADATA_KEY_TITLE)
                ?: data?.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE),
            artist = data?.getString(MediaMetadata.METADATA_KEY_ARTIST)
                ?: data?.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
                ?: data?.getString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE),
            albumArt = data?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
                ?: data?.getBitmap(MediaMetadata.METADATA_KEY_ART)
                ?: data?.getBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON),
            isPlaying = state?.state == PlaybackState.STATE_PLAYING,
            positionMs = state?.position?.coerceAtLeast(0L) ?: 0L,
            durationMs = data?.getLong(MediaMetadata.METADATA_KEY_DURATION)?.coerceAtLeast(0L) ?: 0L,
            capturedAtElapsedMs = capturedAt
        )
    }
}
