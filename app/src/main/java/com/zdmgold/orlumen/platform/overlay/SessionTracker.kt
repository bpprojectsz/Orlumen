package com.zdmgold.orlumen.platform.overlay

import com.zdmgold.orlumen.core.datastore.PreferencesRepository
import com.zdmgold.orlumen.core.model.REVIEW_MIN_SESSION_MS
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Counts a session once the edge lights have run for a full minute without a break. AOD-only mode does not count. It
 * lives as long as the app process, so sessions are counted even when no screen of the app is open.
 */
@Singleton
class SessionTracker @Inject constructor(
    private val controller: VisualizerController,
    private val preferencesRepository: PreferencesRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var started = false

    fun start() {
        if (started) return
        started = true
        scope.launch {
            controller.state
                .map { (it as? RuntimeState.Running)?.source != null }
                .distinctUntilChanged()
                .collectLatest { lightsRunning ->
                    if (lightsRunning) {
                        delay(REVIEW_MIN_SESSION_MS)
                        preferencesRepository.recordQualifyingSession(LocalDate.now().toEpochDay())
                    }
                }
        }
    }
}
