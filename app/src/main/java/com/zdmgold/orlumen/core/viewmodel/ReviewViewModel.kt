package com.zdmgold.orlumen.core.viewmodel

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zdmgold.orlumen.core.datastore.PreferencesRepository
import com.zdmgold.orlumen.core.model.INTERSTITIAL_MIN_GAP_MS
import com.zdmgold.orlumen.core.model.recentInterstitialTimes
import com.zdmgold.orlumen.core.model.shouldRequestReview
import com.zdmgold.orlumen.platform.review.ReviewRequester
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Decides, once per launch, whether to ask Google Play for its review popup. The rules live in
 * [shouldRequestReview]. It never asks within the interstitial spacing of an ad, because finishing an editor returns
 * to Home right after one.
 */
@HiltViewModel
class ReviewViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesRepository: PreferencesRepository,
    private val reviewRequester: ReviewRequester
) : ViewModel() {
    private var checked = false

    fun checkOnHomeOpen(activity: Activity) {
        if (checked) return
        checked = true
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val adShownRecently = recentInterstitialTimes(now, preferencesRepository.interstitialHistory.first())
                .any { now - it < INTERSTITIAL_MIN_GAP_MS }
            val progress = preferencesRepository.reviewProgress.first()
            if (adShownRecently || !shouldRequestReview(progress, installTimeMillis(), now)) return@launch
            // Google reports nothing back, so the attempt is counted when it is made.
            preferencesRepository.recordReviewAttempt(now)
            reviewRequester.request(activity)
        }
    }

    private fun installTimeMillis(): Long = runCatching {
        val packages = context.packageManager
        val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packages.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            packages.getPackageInfo(context.packageName, 0)
        }
        info.firstInstallTime
    }.getOrDefault(0L)
}
