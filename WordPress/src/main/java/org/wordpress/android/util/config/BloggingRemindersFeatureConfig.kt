package org.sitebay.android.util.config

import org.sitebay.android.BuildConfig
import org.sitebay.android.annotation.FeatureInDevelopment
import javax.inject.Inject

@FeatureInDevelopment
class BloggingRemindersFeatureConfig
@Inject constructor(appConfig: AppConfig) : FeatureConfig(appConfig, BuildConfig.BLOGGING_REMINDERS)
