package org.sitebay.android.ui.deeplinks.handlers

import androidx.lifecycle.LiveData
import org.sitebay.android.ui.deeplinks.DeepLinkNavigator.NavigateAction
import org.sitebay.android.util.UriWrapper
import org.sitebay.android.viewmodel.Event

interface DeepLinkHandler {
    /**
     * Returns true when the url is handled by this handler
     */
    fun shouldHandleUrl(uri: UriWrapper): Boolean

    /**
     * Builds navigate action from the deep link
     */
    fun buildNavigateAction(uri: UriWrapper): NavigateAction

    /**
     * Strips all uri sensitive params for tracking purposes
     */
    fun stripUrl(uri: UriWrapper): String

    /**
     * Toast messages emitted from the handler
     */
    fun toast(): LiveData<Event<Int>>? = null
}
