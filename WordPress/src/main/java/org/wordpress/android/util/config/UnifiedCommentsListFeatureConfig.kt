package org.sitebay.android.util.config

import org.sitebay.android.BuildConfig
import org.sitebay.android.annotation.FeatureInDevelopment
import javax.inject.Inject

/**
 * Configuration of the Unified Comments improvements
 */
@FeatureInDevelopment
class UnifiedCommentsListFeatureConfig
@Inject constructor(
    appConfig: AppConfig
) : FeatureConfig(
        appConfig,
        BuildConfig.UNIFIED_COMMENTS_LIST
)
