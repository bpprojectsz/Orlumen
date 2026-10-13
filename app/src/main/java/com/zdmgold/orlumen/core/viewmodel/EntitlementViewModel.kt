package com.zdmgold.orlumen.core.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zdmgold.orlumen.core.repository.EntitlementRepository
import com.zdmgold.orlumen.platform.monetization.ProProductInfo
import com.zdmgold.orlumen.platform.monetization.PurchaseEvent
import com.zdmgold.orlumen.platform.monetization.PurchaseLaunchResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val STOP_TIMEOUT_MS = 5_000L

data class EntitlementUiState(val isPro: Boolean, val product: ProProductInfo?, val lastEvent: PurchaseEvent?)

@HiltViewModel
class EntitlementViewModel @Inject constructor(private val repository: EntitlementRepository) : ViewModel() {
    private val lastEvent = MutableStateFlow<PurchaseEvent?>(null)

    val uiState: StateFlow<EntitlementUiState> = combine(
        repository.isPro,
        repository.product,
        lastEvent
    ) { isPro, product, event -> EntitlementUiState(isPro, product, event) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), EntitlementUiState(false, null, null))

    init {
        repository.connect()
        viewModelScope.launch { repository.events.collect { lastEvent.value = it } }
        viewModelScope.launch { repository.persistConfirmedOwnership() }
    }

    fun purchase(activity: Activity): PurchaseLaunchResult = repository.purchase(activity)

    fun restore() = repository.refresh()

    fun consumeEvent() {
        lastEvent.value = null
    }
}
