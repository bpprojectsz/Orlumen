package com.zdmgold.orlumen.platform.overlay

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
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
