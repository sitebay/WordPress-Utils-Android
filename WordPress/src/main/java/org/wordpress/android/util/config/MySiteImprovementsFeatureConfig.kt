package org.sitebay.android.util.config

import org.sitebay.android.BuildConfig
import org.sitebay.android.annotation.FeatureInDevelopment
import javax.inject.Inject

/**
 * Configuration of the my site infrastructure improvements
 */
@FeatureInDevelopment
class MySiteImprovementsFeatureConfig
@Inject constructor(
    appConfig: AppConfig
) : FeatureConfig(
        appConfig,
        BuildConfig.MY_SITE_IMPROVEMENTS
)
