package org.sitebay.android.ui.posts

import org.sitebay.android.analytics.AnalyticsTracker.Stat
import org.sitebay.android.util.analytics.AnalyticsTrackerWrapper

private const val VIA = "via"
private const val POST_SETTINGS = "settings"
private const val PREPUBLISHING_NUDGES = "prepublishing_nudges"

fun AnalyticsTrackerWrapper.trackPrepublishingNudges(stat: Stat) {
    this.track(stat, mapOf(VIA to PREPUBLISHING_NUDGES))
}

fun AnalyticsTrackerWrapper.trackPostSettings(stat: Stat) {
    this.track(stat, mapOf(VIA to POST_SETTINGS))
}
