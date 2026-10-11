package com.zdmgold.orlumen.core.model

import android.graphics.Bitmap

data class NowPlaying(
    val sourcePackage: String,
    val title: String?,
    val artist: String?,
    val albumArt: Bitmap?,
    val isPlaying: Boolean,
    val positionMs: Long,
    val durationMs: Long,
    val capturedAtElapsedMs: Long = 0L
) {
    val progress: Float
        get() = if (durationMs > 0) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f

    fun positionAt(nowElapsedMs: Long): Long {
        if (!isPlaying) return positionMs
        val advanced = positionMs + (nowElapsedMs - capturedAtElapsedMs).coerceAtLeast(0L)
        return if (durationMs > 0) advanced.coerceAtMost(durationMs) else advanced
    }

    companion object {
        val idle = NowPlaying(
            sourcePackage = "",
            title = null,
            artist = null,
            albumArt = null,
            isPlaying = false,
            positionMs = 0L,
            durationMs = 0L
        )
    }
}
