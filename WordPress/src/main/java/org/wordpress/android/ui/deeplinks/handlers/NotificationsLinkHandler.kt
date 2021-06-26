package org.sitebay.android.ui.deeplinks.handlers

import org.sitebay.android.ui.deeplinks.DeepLinkNavigator.NavigateAction
import org.sitebay.android.ui.deeplinks.DeepLinkNavigator.NavigateAction.OpenNotifications
import org.sitebay.android.ui.deeplinks.DeepLinkingIntentReceiverViewModel
import org.sitebay.android.util.UriWrapper
import javax.inject.Inject

class NotificationsLinkHandler
@Inject constructor() : DeepLinkHandler {
    /**
     * Builds navigate action from URL like:
     * https://sitebay.com/notifications
     */
    override fun buildNavigateAction(uri: UriWrapper): NavigateAction {
        return OpenNotifications
    }

    /**
     * Returns true if the URI should be handled by NotificationsLinkHandler.
     * The handled links are `https://sitebay.com/notifications`
     */
    override fun shouldHandleUrl(uri: UriWrapper): Boolean {
        return (uri.host == DeepLinkingIntentReceiverViewModel.HOST_WORDPRESS_COM &&
                uri.pathSegments.firstOrNull() == NOTIFICATIONS_PATH) || uri.host == NOTIFICATIONS_PATH
    }

    override fun stripUrl(uri: UriWrapper): String {
        return buildString {
            if (uri.host == NOTIFICATIONS_PATH) {
                append(DeepLinkingIntentReceiverViewModel.APPLINK_SCHEME)
            } else {
                append("${DeepLinkingIntentReceiverViewModel.HOST_WORDPRESS_COM}/")
            }
            append(NOTIFICATIONS_PATH)
        }
    }

    companion object {
        private const val NOTIFICATIONS_PATH = "notifications"
    }
}
