package org.sitebay.android.util.config

import org.sitebay.android.BuildConfig.SEEN_UNSEEN_WITH_COUNTER
import org.sitebay.android.annotation.Feature
import org.sitebay.android.util.config.SeenUnseenWithCounterFeatureConfig.Companion.SEEN_UNSEEN_WITH_COUNTER_REMOTE_FIELD
import javax.inject.Inject

/**
 * Configuration of the Unread Posts Count and Seen Status Toggle
 */
@Feature(SEEN_UNSEEN_WITH_COUNTER_REMOTE_FIELD, true)
class SeenUnseenWithCounterFeatureConfig
@Inject constructor(appConfig: AppConfig) : FeatureConfig(
        appConfig,
        SEEN_UNSEEN_WITH_COUNTER,
        SEEN_UNSEEN_WITH_COUNTER_REMOTE_FIELD
) {
    companion object {
        const val SEEN_UNSEEN_WITH_COUNTER_REMOTE_FIELD = "seen_unseen_with_counter"
    }
}
