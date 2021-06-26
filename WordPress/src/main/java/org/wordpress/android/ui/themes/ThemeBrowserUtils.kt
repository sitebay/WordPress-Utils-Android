package org.sitebay.android.ui.themes

import org.sitebay.android.fluxc.model.SiteModel
import javax.inject.Inject

class ThemeBrowserUtils
@Inject constructor() {
    fun isAccessible(site: SiteModel?): Boolean {
        // themes are only accessible to admin sitebay.com users
        return site != null &&
                site.isUsingWpComRestApi &&
                site.hasCapabilityEditThemeOptions &&
                !site.isWpForTeamsSite
    }
}
