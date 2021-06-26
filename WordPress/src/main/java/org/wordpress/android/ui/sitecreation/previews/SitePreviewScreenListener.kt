package org.sitebay.android.ui.sitecreation.previews

import org.sitebay.android.ui.sitecreation.previews.SitePreviewViewModel.CreateSiteState

interface SitePreviewScreenListener {
    fun onSitePreviewScreenDismissed(createSiteState: CreateSiteState)
    fun onSiteCreationCompleted()
}
