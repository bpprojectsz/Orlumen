package com.zdmgold.orlumen.core.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zdmgold.orlumen.core.repository.ConsentRepository
import com.zdmgold.orlumen.core.repository.EntitlementRepository
import com.zdmgold.orlumen.core.repository.adsAllowed
import com.zdmgold.orlumen.platform.compliance.ConsentSnapshot
import com.zdmgold.orlumen.platform.monetization.AdAdapter
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

private const val STOP_TIMEOUT_MS = 5_000L

@HiltViewModel
class ConsentViewModel @Inject constructor(
    private val consentRepository: ConsentRepository,
    entitlementRepository: EntitlementRepository,
    private val adAdapter: AdAdapter
) : ViewModel() {
    val consent: StateFlow<ConsentSnapshot> = consentRepository.consent

    /** True when ads may be shown: consent allows requests and the user has not bought Pro. */
    val showAds: StateFlow<Boolean> = combine(entitlementRepository.isPro, consentRepository.consent) {
            isPro,
            snapshot
        ->
        adsAllowed(isPro, snapshot)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), false)

    fun onConsentFinished(canRequestAds: Boolean) {
        adAdapter.initialize(canRequestAds)
    }

    fun gatherConsent(activity: Activity) {
        consentRepository.gatherConsent(activity) { onConsentFinished(consentRepository.consent.value.canRequestAds) }
    }

    fun showPrivacyOptions(activity: Activity) {
        consentRepository.showPrivacyOptions(activity) {}
    }
}
