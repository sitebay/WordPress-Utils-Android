package org.sitebay.android.ui.mysite

import org.sitebay.android.R
import org.sitebay.android.fluxc.model.SiteModel
import org.sitebay.android.ui.mysite.MySiteItem.SiteInfoBlock
import org.sitebay.android.ui.mysite.MySiteItem.SiteInfoBlock.IconState
import org.sitebay.android.ui.utils.ListItemInteraction
import org.sitebay.android.util.SiteUtils
import org.sitebay.android.viewmodel.ResourceProvider
import javax.inject.Inject

class SiteInfoBlockBuilder
@Inject constructor(private val resourceProvider: ResourceProvider) {
    fun buildSiteInfoBlock(
        site: SiteModel,
        showSiteIconProgressBar: Boolean,
        titleClick: () -> Unit,
        iconClick: () -> Unit,
        urlClick: () -> Unit,
        switchSiteClick: () -> Unit,
        showUpdateSiteTitleFocusPoint: Boolean,
        showUploadSiteIconFocusPoint: Boolean
    ): SiteInfoBlock {
        val homeUrl = SiteUtils.getHomeURLOrHostName(site)
        val blogTitle = SiteUtils.getSiteNameOrHomeURL(site)
        val siteIcon = if (!showSiteIconProgressBar && !site.iconUrl.isNullOrEmpty()) {
            IconState.Visible(SiteUtils.getSiteIconUrl(
                    site,
                    resourceProvider.getDimensionPixelSize(R.dimen.blavatar_sz_small)
            ))
        } else if (showSiteIconProgressBar) {
            IconState.Progress
        } else {
            IconState.Visible()
        }
        return SiteInfoBlock(
                blogTitle,
                homeUrl,
                siteIcon,
                showUpdateSiteTitleFocusPoint,
                showUploadSiteIconFocusPoint,
                buildTitleClick(site, titleClick),
                ListItemInteraction.create(iconClick),
                ListItemInteraction.create(urlClick),
                ListItemInteraction.create(switchSiteClick)
        )
    }

    private fun buildTitleClick(site: SiteModel, titleClick: () -> Unit): ListItemInteraction? {
        return if (SiteUtils.isAccessedViaWPComRest(site)) {
            ListItemInteraction.create(titleClick)
        } else {
            null
        }
    }
}
