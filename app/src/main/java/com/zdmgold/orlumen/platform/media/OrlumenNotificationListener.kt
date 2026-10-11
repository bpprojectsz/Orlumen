package com.zdmgold.orlumen.platform.media

import android.content.ComponentName
import android.service.notification.NotificationListenerService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class OrlumenNotificationListener : NotificationListenerService() {
    @Inject
    lateinit var monitor: MediaSessionMonitor

    override fun onListenerConnected() {
        monitor.start(ComponentName(this, OrlumenNotificationListener::class.java))
    }

    override fun onListenerDisconnected() {
        monitor.stop()
    }
}
