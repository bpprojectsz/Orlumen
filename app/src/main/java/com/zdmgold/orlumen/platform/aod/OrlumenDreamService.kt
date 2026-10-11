package com.zdmgold.orlumen.platform.aod

import android.service.dreams.DreamService
import com.zdmgold.orlumen.platform.overlay.OverlayComposeHost
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class OrlumenDreamService : DreamService() {
    @Inject lateinit var aodController: AodController

    private var host: OverlayComposeHost? = null

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isInteractive = false
        isFullscreen = true
        isScreenBright = false
        val dreamHost = OverlayComposeHost(this)
        dreamHost.setContent { AodHostScreen(controller = aodController, onTap = { wakeUp() }) }
        setContentView(dreamHost.view)
        host = dreamHost
    }

    override fun onDreamingStarted() {
        super.onDreamingStarted()
        host?.onShown()
    }

    override fun onDetachedFromWindow() {
        host?.destroy()
        host = null
        super.onDetachedFromWindow()
    }
}
