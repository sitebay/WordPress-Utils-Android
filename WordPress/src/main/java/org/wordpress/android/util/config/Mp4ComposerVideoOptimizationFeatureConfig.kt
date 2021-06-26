package org.sitebay.android.util.config

import org.sitebay.android.BuildConfig
import org.sitebay.android.annotation.Feature
import org.sitebay.android.util.config.Mp4ComposerVideoOptimizationFeatureConfig.Companion.MP4_COMPOSER_REMOTE_FIELD
import javax.inject.Inject

/**
 * Configuration of the Mp4Composer Video Optimizer
 */
@Feature(MP4_COMPOSER_REMOTE_FIELD, true)
class Mp4ComposerVideoOptimizationFeatureConfig
@Inject constructor(appConfig: AppConfig) : FeatureConfig(
        appConfig,
        BuildConfig.MP4_COMPOSER_VIDEO_OPTIMIZATION,
        MP4_COMPOSER_REMOTE_FIELD
) {
    companion object {
        const val MP4_COMPOSER_REMOTE_FIELD = "mp4_composer_video_optimization_enabled"
    }
}
