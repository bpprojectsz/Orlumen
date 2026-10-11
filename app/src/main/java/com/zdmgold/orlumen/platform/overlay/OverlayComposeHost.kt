package com.zdmgold.orlumen.platform.overlay

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.zdmgold.orlumen.core.model.SpectrumFrame
import com.zdmgold.orlumen.core.model.VisualizerConfig
import com.zdmgold.orlumen.core.model.VisualizerDirection

private const val HALF = 0.5f

/**
 * Hosts Compose content inside a service-owned window. A service has no lifecycle, saved-state or
 * view-model-store owners of its own, so this class supplies them for the ComposeView.
 */
class OverlayComposeHost(context: Context) :
    LifecycleOwner,
    SavedStateRegistryOwner,
    ViewModelStoreOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateController = SavedStateRegistryController.create(this)

    override val viewModelStore = ViewModelStore()
    override val lifecycle: Lifecycle
        get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateController.savedStateRegistry

    val view = ComposeView(context)

    init {
        savedStateController.performRestore(null)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        view.setViewTreeLifecycleOwner(this)
        view.setViewTreeSavedStateRegistryOwner(this)
        view.setViewTreeViewModelStoreOwner(this)
    }

    fun setContent(content: @Composable () -> Unit) {
        view.setContent(content)
    }

    fun onShown() {
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }

    fun destroy() {
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        view.disposeComposition()
        viewModelStore.clear()
    }
}

@Composable
fun EdgeLevelsContent(
    frame: SpectrumFrame?,
    config: VisualizerConfig,
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val current = frame ?: return@Canvas
        val bands = current.size
        val thickness = config.thickness.dp.toPx()
        val maxLength = config.height.dp.toPx()
        val clockwise = config.direction == VisualizerDirection.CLOCKWISE
        for (index in 0 until bands) {
            val y = size.height * (1f - (index + HALF) / bands)
            val color = colorAt(colors, index / (bands - 1).coerceAtLeast(1).toFloat())
            val leftLevel = current[if (clockwise) index else bands - 1 - index]
            val rightLevel = current[if (clockwise) bands - 1 - index else index]
            drawLine(color, Offset(0f, y), Offset(maxLength * leftLevel, y), thickness, StrokeCap.Round)
            drawLine(
                color,
                Offset(size.width, y),
                Offset(size.width - maxLength * rightLevel, y),
                thickness,
                StrokeCap.Round
            )
        }
    }
}

private fun colorAt(colors: List<Color>, fraction: Float): Color {
    if (colors.size < 2) return colors.firstOrNull() ?: Color.White
    val scaled = fraction.coerceIn(0f, 1f) * (colors.size - 1)
    val lower = scaled.toInt().coerceAtMost(colors.size - 2)
    return lerp(colors[lower], colors[lower + 1], scaled - lower)
}
