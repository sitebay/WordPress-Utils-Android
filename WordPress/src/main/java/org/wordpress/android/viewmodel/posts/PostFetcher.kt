package org.sitebay.android.viewmodel.posts

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.sitebay.android.fluxc.Dispatcher
import org.sitebay.android.fluxc.generated.PostActionBuilder
import org.sitebay.android.fluxc.model.CauseOfOnPostChanged.UpdatePost
import org.sitebay.android.fluxc.model.LocalOrRemoteId.RemoteId
import org.sitebay.android.fluxc.model.PostModel
import org.sitebay.android.fluxc.model.SiteModel
import org.sitebay.android.fluxc.store.PostStore.OnPostChanged
import org.sitebay.android.fluxc.store.PostStore.RemotePostPayload

/**
 * Class which takes care of dispatching fetch post events while ignoring duplicate requests.
 */
class PostFetcher constructor(
    private val lifecycle: Lifecycle,
    private val dispatcher: Dispatcher
) : LifecycleObserver {
    private val ongoingRequests = HashSet<RemoteId>()

    init {
        dispatcher.register(this)
        lifecycle.addObserver(this)
    }

    /**
     * Handles the [Lifecycle.Event.ON_DESTROY] event to cleanup the registration for dispatcher and removing the
     * observer for lifecycle.
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun onDestroy() {
        lifecycle.removeObserver(this)
        dispatcher.unregister(this)
    }

    // TODO: We should implement batch fetching when it's available in the API
    fun fetchPosts(site: SiteModel, remoteItemIds: List<RemoteId>) {
        remoteItemIds
                .filter {
                    // ignore duplicate requests
                    !ongoingRequests.contains(it)
                }
                .forEach { remoteId ->
                    ongoingRequests.add(remoteId)

                    val postToFetch = PostModel()
                    postToFetch.setRemotePostId(remoteId.value)
                    dispatcher.dispatch(PostActionBuilder.newFetchPostAction(RemotePostPayload(postToFetch, site)))
                }
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onPostChanged(event: OnPostChanged) {
        (event.causeOfChange as? UpdatePost)?.let { updatePostCauseOfChange ->
            ongoingRequests.remove(RemoteId(updatePostCauseOfChange.remotePostId))
        }
    }
}
