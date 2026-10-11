package com.zdmgold.orlumen.platform.media

import android.media.session.PlaybackState
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaCommandSender @Inject constructor(private val monitor: MediaSessionMonitor) {
    fun togglePlayPause(packageName: String): Boolean {
        val controller = monitor.controllerFor(packageName) ?: return false
        if (controller.playbackState?.state == PlaybackState.STATE_PLAYING) {
            controller.transportControls.pause()
        } else {
            controller.transportControls.play()
        }
        return true
    }

    fun skipToNext(packageName: String): Boolean {
        val controller = monitor.controllerFor(packageName) ?: return false
        controller.transportControls.skipToNext()
        return true
    }

    fun skipToPrevious(packageName: String): Boolean {
        val controller = monitor.controllerFor(packageName) ?: return false
        controller.transportControls.skipToPrevious()
        return true
    }
}
