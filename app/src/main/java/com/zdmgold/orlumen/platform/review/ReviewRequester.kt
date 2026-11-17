package com.zdmgold.orlumen.platform.review

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.play.core.review.ReviewManagerFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "ReviewRequester"

/**
 * Asks Google Play to show its own review popup. Play decides whether anything appears (it has a quota and shows
 * nothing outside a Play install), and gives no answer about it, so callers must not wait for one.
 */
@Singleton
class ReviewRequester @Inject constructor(@ApplicationContext private val context: Context) {
    fun request(activity: Activity) {
        val manager = ReviewManagerFactory.create(context)
        manager.requestReviewFlow().addOnCompleteListener { request ->
            if (!request.isSuccessful) {
                Log.w(TAG, "Review flow not available", request.exception)
            } else if (!activity.isFinishing && !activity.isDestroyed) {
                manager.launchReviewFlow(activity, request.result)
            }
        }
    }
}
