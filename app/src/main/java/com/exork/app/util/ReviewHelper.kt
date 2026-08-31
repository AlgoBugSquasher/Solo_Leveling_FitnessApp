package com.exork.app.util

import android.app.Activity
import android.util.Log
import com.google.android.play.core.review.ReviewManagerFactory

/**
 * Helper to handle Google Play In-App Review Flow.
 */
object ReviewHelper {

    fun launchReviewFlow(activity: Activity) {
        val manager = ReviewManagerFactory.create(activity)
        val request = manager.requestReviewFlow()
        
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // We got the ReviewInfo object
                val reviewInfo = task.result
                val flow = manager.launchReviewFlow(activity, reviewInfo)
                flow.addOnCompleteListener { _ ->
                    // The flow has finished. The API does not indicate whether the user
                    // reviewed or not, or even whether the review dialog was shown.
                    Log.d("ReviewHelper", "In-App Review flow finished.")
                }
            } else {
                // There was some problem, log or handle the error code.
                Log.e("ReviewHelper", "Review request failed: ${task.exception?.message}")
            }
        }
    }
}
