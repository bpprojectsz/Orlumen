package com.zdmgold.orlumen.platform.aod

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

private const val AOD_BRIGHTNESS = 0.01f

@AndroidEntryPoint
class AodActivity : ComponentActivity() {
    @Inject lateinit var aodController: AodController

    private val screenReceiver = ScreenStateReceiver(
        object : ScreenStateReceiver.Listener {
            override fun onUserPresent() {
                finish()
            }
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        showOverLockscreen()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.attributes = window.attributes.apply { screenBrightness = AOD_BRIGHTNESS }
        hideSystemBars()
        screenReceiver.register(this)
        aodController.setActivityActive(true)
        setContent { AodHostScreen(controller = aodController, onTap = { finish() }) }
    }

    override fun onDestroy() {
        screenReceiver.unregister(this)
        aodController.setActivityActive(false)
        super.onDestroy()
    }

    @Suppress("DEPRECATION")
    private fun showOverLockscreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
    }

    private fun hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}
