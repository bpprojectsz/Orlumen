package com.zdmgold.orlumen.platform.compliance

import android.app.Activity
import android.content.Context
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ConsentStatus {
    UNKNOWN,
    REQUIRED,
    NOT_REQUIRED,
    OBTAINED
}

data class ConsentSnapshot(val status: ConsentStatus, val canRequestAds: Boolean, val privacyOptionsRequired: Boolean) {
    companion object {
        val initial = ConsentSnapshot(ConsentStatus.UNKNOWN, false, false)
    }
}

internal fun consentStatusFrom(value: Int): ConsentStatus = when (value) {
    ConsentInformation.ConsentStatus.REQUIRED -> ConsentStatus.REQUIRED
    ConsentInformation.ConsentStatus.NOT_REQUIRED -> ConsentStatus.NOT_REQUIRED
    ConsentInformation.ConsentStatus.OBTAINED -> ConsentStatus.OBTAINED
    else -> ConsentStatus.UNKNOWN
}

@Singleton
class ConsentManager @Inject constructor(@ApplicationContext context: Context) {
    private val consentInformation = UserMessagingPlatform.getConsentInformation(context)
    private val _snapshot = MutableStateFlow(readSnapshot())
    val snapshot: StateFlow<ConsentSnapshot> = _snapshot.asStateFlow()

    /** Refreshes consent info and shows the form when the user's region requires it. */
    fun gatherConsent(activity: Activity, onFinished: (errorMessage: String?) -> Unit) {
        consentInformation.requestConsentInfoUpdate(
            activity,
            ConsentRequestParameters.Builder().build(),
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    refresh()
                    onFinished(formError?.message)
                }
            },
            { requestError ->
                refresh()
                onFinished(requestError.message)
            }
        )
    }

    fun showPrivacyOptions(activity: Activity, onFinished: (errorMessage: String?) -> Unit) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { formError ->
            refresh()
            onFinished(formError?.message)
        }
    }

    private fun refresh() {
        _snapshot.value = readSnapshot()
    }

    private fun readSnapshot(): ConsentSnapshot = ConsentSnapshot(
        status = consentStatusFrom(consentInformation.consentStatus),
        canRequestAds = consentInformation.canRequestAds(),
        privacyOptionsRequired = consentInformation.privacyOptionsRequirementStatus ==
            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
    )
}
