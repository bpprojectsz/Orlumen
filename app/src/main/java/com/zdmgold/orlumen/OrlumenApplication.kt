package com.zdmgold.orlumen

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.zdmgold.orlumen.core.error.GlobalExceptionHandler
import com.zdmgold.orlumen.platform.overlay.SessionTracker
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class OrlumenApplication : Application() {
    @Inject lateinit var sessionTracker: SessionTracker

    override fun onCreate() {
        super.onCreate()
        sessionTracker.start()
        GlobalExceptionHandler.install(this)
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val manager = getSystemService(NotificationManager::class.java)
        val runtimeChannel = NotificationChannel(
            RUNTIME_CHANNEL_ID,
            getString(R.string.channel_runtime_name),
            NotificationManager.IMPORTANCE_LOW
        )
        manager.createNotificationChannel(runtimeChannel)
    }

    companion object {
        const val RUNTIME_CHANNEL_ID = "orlumen_runtime"
    }
}
