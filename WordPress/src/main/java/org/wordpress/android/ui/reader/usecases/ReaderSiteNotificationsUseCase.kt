package org.sitebay.android.ui.reader.usecases

import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.sitebay.android.analytics.AnalyticsTracker
import org.sitebay.android.datasets.ReaderBlogTableWrapper
import org.sitebay.android.fluxc.Dispatcher
import org.sitebay.android.fluxc.generated.AccountActionBuilder
import org.sitebay.android.fluxc.store.AccountStore.AddOrDeleteSubscriptionPayload
import org.sitebay.android.fluxc.store.AccountStore.AddOrDeleteSubscriptionPayload.SubscriptionAction
import org.sitebay.android.fluxc.store.AccountStore.AddOrDeleteSubscriptionPayload.SubscriptionAction.DELETE
import org.sitebay.android.fluxc.store.AccountStore.AddOrDeleteSubscriptionPayload.SubscriptionAction.NEW
import org.sitebay.android.fluxc.store.AccountStore.OnSubscriptionUpdated
import org.sitebay.android.ui.reader.tracker.ReaderTracker
import org.sitebay.android.ui.reader.usecases.ReaderSiteNotificationsUseCase.SiteNotificationState.Failed.AlreadyRunning
import org.sitebay.android.ui.reader.usecases.ReaderSiteNotificationsUseCase.SiteNotificationState.Failed.NoNetwork
import org.sitebay.android.ui.reader.usecases.ReaderSiteNotificationsUseCase.SiteNotificationState.Failed.RequestFailed
import org.sitebay.android.ui.reader.usecases.ReaderSiteNotificationsUseCase.SiteNotificationState.Success
import org.sitebay.android.util.AppLog
import org.sitebay.android.util.AppLog.T.API
import org.sitebay.android.util.NetworkUtilsWrapper
import javax.inject.Inject
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * This class handles reader notification events.
 */
class ReaderSiteNotificationsUseCase @Inject constructor(
    private val dispatcher: Dispatcher,
    private val readerTracker: ReaderTracker,
    private val readerBlogTableWrapper: ReaderBlogTableWrapper,
    private val networkUtilsWrapper: NetworkUtilsWrapper
) {
    private var continuation: Continuation<Boolean>? = null

    suspend fun toggleNotification(
        blogId: Long,
        feedId: Long
    ): SiteNotificationState {
        if (continuation != null) {
            return AlreadyRunning
        }
        if (!networkUtilsWrapper.isNetworkAvailable()) {
            return NoNetwork
        }

        // We want to track the action no matter the result
        trackEvent(blogId, feedId)

        val succeeded = suspendCoroutine<Boolean> { cont ->
            continuation = cont

            val action = if (readerBlogTableWrapper.isNotificationsEnabled(blogId)) {
                DELETE
            } else {
                NEW
            }
            updateSubscription(blogId, action)
        }

        return if (succeeded) {
            updateNotificationEnabledForBlogInDb(blogId, !readerBlogTableWrapper.isNotificationsEnabled(blogId))
            fetchSubscriptions()
            Success
        } else {
            RequestFailed
        }
    }

    private fun trackEvent(blogId: Long, feedId: Long) {
        val trackingEvent = if (readerBlogTableWrapper.isNotificationsEnabled(blogId)) {
            AnalyticsTracker.Stat.FOLLOWED_BLOG_NOTIFICATIONS_READER_MENU_OFF
        } else {
            AnalyticsTracker.Stat.FOLLOWED_BLOG_NOTIFICATIONS_READER_MENU_ON
        }

        readerTracker.trackBlog(trackingEvent, blogId, feedId)
    }

    fun updateNotificationEnabledForBlogInDb(blogId: Long, isNotificationEnabledForBlog: Boolean) {
        readerBlogTableWrapper.setNotificationsEnabledByBlogId(blogId, isNotificationEnabledForBlog)
    }

    fun fetchSubscriptions() {
        dispatcher.dispatch(AccountActionBuilder.newFetchSubscriptionsAction())
    }

    fun updateSubscription(blogId: Long, action: SubscriptionAction) {
        val payload = AddOrDeleteSubscriptionPayload(blogId.toString(), action)
        dispatcher.dispatch(AccountActionBuilder.newUpdateSubscriptionNotificationPostAction(payload))
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    @SuppressWarnings("unused")
    fun onSubscriptionUpdated(event: OnSubscriptionUpdated) {
        if (event.isError) {
            continuation?.resume(false)
            AppLog.e(
                    API,
                    ReaderSiteNotificationsUseCase::class.java.simpleName + ".onSubscriptionUpdated: " +
                            event.error.type + " - " + event.error.message
            )
        } else {
            continuation?.resume(true)
        }
        continuation = null
    }

    sealed class SiteNotificationState {
        object Success : SiteNotificationState()
        sealed class Failed : SiteNotificationState() {
            object NoNetwork : Failed()
            object RequestFailed : Failed()
            object AlreadyRunning : Failed()
        }
    }
}
