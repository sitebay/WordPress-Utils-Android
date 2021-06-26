package org.sitebay.android.ui.bloggingreminders

import org.sitebay.android.ui.prefs.AppPrefsWrapper
import org.sitebay.android.util.config.BloggingRemindersFeatureConfig
import javax.inject.Inject

class BloggingRemindersManager
@Inject constructor(
    private val bloggingRemindersFeatureConfig: BloggingRemindersFeatureConfig,
    private val appPrefsWrapper: AppPrefsWrapper
) {
    fun shouldShowBloggingRemindersPrompt(siteId: Int): Boolean {
        return bloggingRemindersFeatureConfig.isEnabled() && !appPrefsWrapper.isBloggingRemindersShown(siteId)
    }

    fun bloggingRemindersShown(siteId: Int) {
        appPrefsWrapper.setBloggingRemindersShown(siteId)
    }
}
