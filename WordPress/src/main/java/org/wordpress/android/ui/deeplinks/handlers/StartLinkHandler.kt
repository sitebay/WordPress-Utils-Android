package org.sitebay.android.ui.deeplinks.handlers

import org.sitebay.android.fluxc.store.AccountStore
import org.sitebay.android.ui.deeplinks.DeepLinkNavigator.NavigateAction
import org.sitebay.android.ui.deeplinks.DeepLinkNavigator.NavigateAction.ShowSignInFlow
import org.sitebay.android.ui.deeplinks.DeepLinkNavigator.NavigateAction.StartCreateSiteFlow
import org.sitebay.android.ui.deeplinks.DeepLinkingIntentReceiverViewModel
import org.sitebay.android.util.UriWrapper
import javax.inject.Inject

class StartLinkHandler
@Inject constructor(private val accountStore: AccountStore) : DeepLinkHandler {
    /**
     * Returns true if the URI looks like `sitebay.com/start`
     */
    override fun shouldHandleUrl(uri: UriWrapper): Boolean {
        return uri.host == DeepLinkingIntentReceiverViewModel.HOST_WORDPRESS_COM &&
                uri.pathSegments.firstOrNull() == START_PATH
    }

    /**
     * Returns StartCreateSiteFlow is user logged in and ShowSignInFlow if user is logged out
     */
    override fun buildNavigateAction(uri: UriWrapper): NavigateAction {
        return if (accountStore.hasAccessToken()) {
            StartCreateSiteFlow
        } else {
            ShowSignInFlow
        }
    }

    override fun stripUrl(uri: UriWrapper): String {
        return "${DeepLinkingIntentReceiverViewModel.HOST_WORDPRESS_COM}/$START_PATH"
    }

    companion object {
        private const val START_PATH = "start"
    }
}
