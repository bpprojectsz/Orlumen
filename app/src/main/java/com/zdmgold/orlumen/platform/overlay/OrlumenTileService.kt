package com.zdmgold.orlumen.platform.overlay

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.zdmgold.orlumen.MainActivity
import com.zdmgold.orlumen.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OrlumenTileService : TileService() {
    @Inject lateinit var controller: VisualizerController

    private var listeningScope: CoroutineScope? = null

    override fun onStartListening() {
        val newScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        listeningScope = newScope
        newScope.launch { controller.state.collect { render(it) } }
    }

    override fun onStopListening() {
        listeningScope?.cancel()
        listeningScope = null
    }

    override fun onClick() {
        val state = controller.state.value
        if (state is RuntimeState.Running || state == RuntimeState.Starting) {
            controller.stop()
        } else if (controller.start() != null) {
            openApp()
        }
    }

    private fun render(state: RuntimeState) {
        val tile = qsTile ?: return
        val active = state is RuntimeState.Running || state == RuntimeState.Starting
        tile.state = if (active) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = getString(R.string.tile_label)
        tile.updateTile()
    }

    @Suppress("DEPRECATION")
    private fun openApp() {
        val launch = Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startActivityAndCollapse(PendingIntent.getActivity(this, 0, launch, PendingIntent.FLAG_IMMUTABLE))
        } else {
            startActivityAndCollapse(launch)
        }
    }
}
