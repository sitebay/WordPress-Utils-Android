package org.sitebay.android.ui.stories

import com.sitebay.stories.compose.frame.StorySaveEvents.StorySaveResult
import org.sitebay.android.WordPress
import org.sitebay.android.analytics.AnalyticsTracker
import org.sitebay.android.analytics.AnalyticsTracker.Stat
import org.sitebay.android.fluxc.model.SiteModel
import org.sitebay.android.util.analytics.AnalyticsUtils
import javax.inject.Inject

class StoriesTrackerHelper @Inject constructor() {
    fun trackStorySaveResultEvent(event: StorySaveResult) {
        val stat = if (event.isSuccess()) Stat.STORY_SAVE_SUCCESSFUL else Stat.STORY_SAVE_ERROR
        trackStorySaveResultEvent(event, stat)
    }

    fun trackStoryPostSavedEvent(frameQty: Int, site: SiteModel, locallySaved: Boolean) {
        val stat = if (locallySaved) Stat.STORY_POST_SAVE_LOCALLY else Stat.STORY_POST_SAVE_REMOTELY
        val properties: HashMap<String, Any> = HashMap()
        properties.put("slide_qty", frameQty)
        AnalyticsUtils.trackWithSiteDetails(stat, site, properties)
    }

    private fun getCommonProperties(event: StorySaveResult): HashMap<String, Any> {
        val properties: HashMap<String, Any> = HashMap()
        properties.put("is_retry", event.isRetry)
        properties.put("slide_qty", event.frameSaveResult.size)
        properties.put("elapsed_time", event.elapsedTime)
        return properties
    }

    fun trackStorySaveResultEvent(event: StorySaveResult, stat: Stat) {
        val properties = getCommonProperties(event)
        var siteModel: SiteModel? = null
        event.metadata?.let {
            siteModel = it.getSerializable(WordPress.SITE) as SiteModel
        }

        siteModel?.let {
            AnalyticsUtils.trackWithSiteDetails(stat, it, properties)
        } ?: AnalyticsTracker.track(stat, properties)
    }
}
