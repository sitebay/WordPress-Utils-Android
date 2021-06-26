package org.sitebay.android.ui.mysite

import org.sitebay.android.R
import org.sitebay.android.fluxc.model.SiteModel
import org.sitebay.android.ui.mysite.MySiteItem.CategoryHeader
import org.sitebay.android.ui.themes.ThemeBrowserUtils
import org.sitebay.android.ui.utils.UiString.UiStringRes
import org.sitebay.android.util.SiteUtilsWrapper
import javax.inject.Inject

class SiteCategoryItemBuilder
@Inject constructor(private val themeBrowserUtils: ThemeBrowserUtils, private val siteUtilsWrapper: SiteUtilsWrapper) {
    fun buildJetpackCategoryIfAvailable(site: SiteModel): MySiteItem? {
        val jetpackSettingsVisible = site.isJetpackConnected && // jetpack is installed and connected
                !site.isWPComAtomic // isn't atomic site
        return if (jetpackSettingsVisible) {
            CategoryHeader(UiStringRes(R.string.my_site_header_jetpack))
        } else null
    }

    fun buildLookAndFeelHeaderIfAvailable(site: SiteModel): MySiteItem? {
        return if (themeBrowserUtils.isAccessible(site)) {
            CategoryHeader(UiStringRes(R.string.my_site_header_look_and_feel))
        } else null
    }

    fun buildConfigurationHeaderIfAvailable(site: SiteModel): MySiteItem? {
        // if either people or settings is visible, configuration header should be visible
        return if (site.hasCapabilityManageOptions ||
                !siteUtilsWrapper.isAccessedViaWPComRest(site) ||
                site.hasCapabilityListUsers) {
            CategoryHeader(UiStringRes(R.string.my_site_header_configuration))
        } else null
    }
}
