package com.zdmgold.orlumen.platform.aod

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

private const val FULL_PERCENT = 100

data class BatteryInfo(val percent: Int, val isCharging: Boolean) {
    companion object {
        val unknown = BatteryInfo(0, false)
    }
}

internal fun batteryInfoFrom(level: Int, scale: Int, status: Int): BatteryInfo {
    val percent = if (level < 0 || scale <= 0) 0 else (level * FULL_PERCENT / scale).coerceIn(0, FULL_PERCENT)
    val charging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
    return BatteryInfo(percent, charging)
}

@Singleton
class BatteryInfoProvider @Inject constructor(@ApplicationContext private val context: Context) {
    fun batteryInfo(): Flow<BatteryInfo> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                intent?.let { trySend(parse(it)) }
            }
        }
        val sticky = ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        sticky?.let { trySend(parse(it)) }
        awaitClose { context.unregisterReceiver(receiver) }
    }

    private fun parse(intent: Intent): BatteryInfo = batteryInfoFrom(
        level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1),
        scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1),
        status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
    )
}
