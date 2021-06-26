package org.sitebay.android.util.config

import org.sitebay.android.BuildConfig
import org.sitebay.android.annotation.FeatureInDevelopment
import javax.inject.Inject

@FeatureInDevelopment
class ContactInfoBlockFeatureConfig
@Inject constructor(appConfig: AppConfig) : FeatureConfig(appConfig, BuildConfig.CONTACT_INFO_BLOCK_AVAILABLE)
