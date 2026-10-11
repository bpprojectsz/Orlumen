package com.zdmgold.orlumen.platform.monetization

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.zdmgold.orlumen.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "AdAdapter"
private const val INTERSTITIAL_MIN_INTERVAL_MS = 3 * 60 * 1000L

internal fun shouldShowInterstitial(
    nowMillis: Long,
    lastShownMillis: Long,
    minIntervalMillis: Long,
    isPro: Boolean
): Boolean = !isPro && (lastShownMillis == 0L || nowMillis - lastShownMillis >= minIntervalMillis)

/**
 * Thin wrapper over the Google Mobile Ads SDK. Nothing here loads until consent allows ads, and a
 * failed banner is reported to the caller so the slot can collapse to zero height.
 */
@Singleton
class AdAdapter @Inject constructor(@ApplicationContext private val context: Context) {
    private val initialized = AtomicBoolean(false)
    private var interstitial: InterstitialAd? = null
    private var lastInterstitialMillis = 0L

    fun initialize(canRequestAds: Boolean) {
        if (canRequestAds && initialized.compareAndSet(false, true)) {
            MobileAds.initialize(context) {}
            loadInterstitial()
        }
    }

    fun createBanner(viewContext: Context, widthDp: Int, onResult: (loaded: Boolean) -> Unit): AdView =
        AdView(viewContext).apply {
            setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(viewContext, widthDp))
            adUnitId = BuildConfig.ADMOB_BANNER_ID
            adListener = object : AdListener() {
                override fun onAdLoaded() = onResult(true)

                override fun onAdFailedToLoad(error: LoadAdError) = onResult(false)
            }
            loadAd(AdRequest.Builder().build())
        }

    /** Shows a preloaded interstitial when the frequency cap allows it. Always calls [onFinished]. */
    fun showInterstitial(activity: Activity, isPro: Boolean, onFinished: () -> Unit): Boolean {
        val ad = interstitial
        val now = System.currentTimeMillis()
        if (ad == null || !shouldShowInterstitial(now, lastInterstitialMillis, INTERSTITIAL_MIN_INTERVAL_MS, isPro)) {
            onFinished()
            return false
        }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitial = null
                loadInterstitial()
                onFinished()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                interstitial = null
                loadInterstitial()
                onFinished()
            }
        }
        lastInterstitialMillis = now
        ad.show(activity)
        return true
    }

    private fun loadInterstitial() {
        InterstitialAd.load(
            context,
            BuildConfig.ADMOB_INTERSTITIAL_ID,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitial = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitial = null
                    Log.w(TAG, "Interstitial failed to load: ${error.message}")
                }
            }
        )
    }
}
