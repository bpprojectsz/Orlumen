package com.zdmgold.orlumen.core.repository

import android.app.Activity
import com.zdmgold.orlumen.platform.compliance.ConsentManager
import com.zdmgold.orlumen.platform.compliance.ConsentSnapshot
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.StateFlow

fun adsAllowed(isPro: Boolean, consent: ConsentSnapshot): Boolean = !isPro && consent.canRequestAds

@Singleton
class ConsentRepository @Inject constructor(private val manager: ConsentManager) {
    val consent: StateFlow<ConsentSnapshot> = manager.snapshot

    fun gatherConsent(activity: Activity, onFinished: (errorMessage: String?) -> Unit) {
        manager.gatherConsent(activity, onFinished)
    }

    fun showPrivacyOptions(activity: Activity, onFinished: (errorMessage: String?) -> Unit) {
        manager.showPrivacyOptions(activity, onFinished)
    }
}
