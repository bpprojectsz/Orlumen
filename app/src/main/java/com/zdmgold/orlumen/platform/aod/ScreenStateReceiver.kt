package com.zdmgold.orlumen.platform.aod

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat

class ScreenStateReceiver(private val listener: Listener) : BroadcastReceiver() {
    interface Listener {
        fun onScreenOff() = Unit

        fun onScreenOn() = Unit

        fun onUserPresent() = Unit
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_SCREEN_OFF -> listener.onScreenOff()
            Intent.ACTION_SCREEN_ON -> listener.onScreenOn()
            Intent.ACTION_USER_PRESENT -> listener.onUserPresent()
        }
    }

    fun register(context: Context) {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        ContextCompat.registerReceiver(context, this, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
    }

    fun unregister(context: Context) {
        runCatching { context.unregisterReceiver(this) }
    }
}
