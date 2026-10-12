package com.zdmgold.orlumen.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import com.zdmgold.orlumen.core.repository.ConsentRepository

private const val TAG = "ConsentDialog"

internal tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

/**
 * Runs the Google consent flow once when it enters the composition. The form itself is shown by the
 * UMP SDK over the current activity, and only where the user's region requires it. [onFinished]
 * receives whether ads may be requested afterwards.
 */
@Composable
fun ConsentDialog(consentRepository: ConsentRepository, onFinished: (canRequestAds: Boolean) -> Unit) {
    val activity = LocalContext.current.findActivity()
    val currentOnFinished = rememberUpdatedState(onFinished)
    LaunchedEffect(activity) {
        if (activity != null) {
            consentRepository.gatherConsent(activity) { errorMessage ->
                if (errorMessage != null) Log.w(TAG, "Consent flow reported: $errorMessage")
                currentOnFinished.value(consentRepository.consent.value.canRequestAds)
            }
        }
    }
}
