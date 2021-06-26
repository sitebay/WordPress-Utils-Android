package org.sitebay.android.util.config

import org.sitebay.android.BuildConfig
import org.sitebay.android.annotation.FeatureInDevelopment
import javax.inject.Inject

@FeatureInDevelopment
class LayoutGridBlockFeatureConfig
@Inject constructor(appConfig: AppConfig) : FeatureConfig(appConfig, BuildConfig.LAYOUT_GRID_BLOCK_AVAILABLE)
