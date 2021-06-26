package org.sitebay.android.ui.reader.reblog

import org.sitebay.android.fluxc.model.SiteModel
import org.sitebay.android.models.ReaderPost

sealed class ReblogState {
    class MultipleSites(val defaultSite: SiteModel, val post: ReaderPost) : ReblogState()
    class SingleSite(val site: SiteModel, val post: ReaderPost) : ReblogState()
    object NoSite : ReblogState()
    object Unknown : ReblogState()
}
