package com.zdmgold.orlumen.core.viewmodel

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zdmgold.orlumen.core.datastore.PreferencesRepository
import com.zdmgold.orlumen.core.model.NowPlaying
import com.zdmgold.orlumen.core.repository.ConsentRepository
import com.zdmgold.orlumen.core.repository.EntitlementRepository
import com.zdmgold.orlumen.core.repository.NowPlayingRepository
import com.zdmgold.orlumen.core.repository.adsAllowed
import com.zdmgold.orlumen.platform.monetization.AdAdapter
import com.zdmgold.orlumen.platform.overlay.RuntimeFailure
import com.zdmgold.orlumen.platform.overlay.RuntimeState
import com.zdmgold.orlumen.platform.overlay.VisualizerController
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val STOP_TIMEOUT_MS = 5_000L

data class MainUiState(
    val runtime: RuntimeState,
    val isPro: Boolean,
    val aodEnabled: Boolean,
    val onboardingCompleted: Boolean,
    val nowPlaying: NowPlaying,
    val adsAllowed: Boolean
)

enum class AodToggleAction {
    START_AOD_ONLY,
    STOP,
    NONE
}

/** Enabling AOD starts the runtime if idle; disabling it stops a runtime that is running only for AOD. */
internal fun aodToggleAction(enabled: Boolean, state: RuntimeState): AodToggleAction = when {
    enabled && (state == RuntimeState.Idle || state is RuntimeState.Failed) -> AodToggleAction.START_AOD_ONLY
    !enabled && state is RuntimeState.Running && state.source == null -> AodToggleAction.STOP
    else -> AodToggleAction.NONE
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val controller: VisualizerController,
    private val preferencesRepository: PreferencesRepository,
    private val entitlementRepository: EntitlementRepository,
    consentRepository: ConsentRepository,
    nowPlayingRepository: NowPlayingRepository,
    private val adAdapter: AdAdapter
) : ViewModel() {
    private val showAds = combine(entitlementRepository.isPro, consentRepository.consent) { isPro, consent ->
        adsAllowed(isPro, consent)
    }

    val uiState: StateFlow<MainUiState?> = combine(
        controller.state,
        preferencesRepository.preferences,
        entitlementRepository.isPro,
        nowPlayingRepository.nowPlaying,
        showAds
    ) { runtime, prefs, isPro, nowPlaying, ads ->
        MainUiState(runtime, isPro, prefs.aodEnabled, prefs.onboardingCompleted, nowPlaying, ads)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

    init {
        entitlementRepository.connect()
        viewModelScope.launch { entitlementRepository.persistConfirmedOwnership() }
        viewModelScope.launch { showAds.collect { allowed -> if (allowed) adAdapter.initialize(true) } }
    }

    fun startVisualizer(projectionResultCode: Int? = null, projectionData: Intent? = null): RuntimeFailure? =
        controller.start(projectionResultCode, projectionData)

    fun stopVisualizer() = controller.stop()

    fun setAodEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setAodEnabled(enabled)
            when (aodToggleAction(enabled, controller.state.value)) {
                AodToggleAction.START_AOD_ONLY -> controller.start(aodOnly = true)
                AodToggleAction.STOP -> controller.stop()
                AodToggleAction.NONE -> Unit
            }
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch { preferencesRepository.setOnboardingCompleted(true) }
    }
}
