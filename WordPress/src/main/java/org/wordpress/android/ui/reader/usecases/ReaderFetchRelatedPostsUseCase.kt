package org.sitebay.android.ui.reader.usecases

import kotlinx.coroutines.suspendCancellableCoroutine
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.sitebay.android.models.ReaderPost
import org.sitebay.android.ui.reader.ReaderEvents.RelatedPostsUpdated
import org.sitebay.android.ui.reader.actions.ReaderPostActionsWrapper
import org.sitebay.android.ui.reader.models.ReaderSimplePostList
import org.sitebay.android.ui.reader.usecases.ReaderFetchRelatedPostsUseCase.FetchRelatedPostsState.AlreadyRunning
import org.sitebay.android.ui.reader.usecases.ReaderFetchRelatedPostsUseCase.FetchRelatedPostsState.Failed.NoNetwork
import org.sitebay.android.ui.reader.usecases.ReaderFetchRelatedPostsUseCase.FetchRelatedPostsState.Failed.RequestFailed
import org.sitebay.android.ui.reader.usecases.ReaderFetchRelatedPostsUseCase.FetchRelatedPostsState.Success
import org.sitebay.android.util.NetworkUtilsWrapper
import javax.inject.Inject
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

class ReaderFetchRelatedPostsUseCase @Inject constructor(
    private val networkUtilsWrapper: NetworkUtilsWrapper,
    private val readerPostActionsWrapper: ReaderPostActionsWrapper
) {
    private val continuations: MutableMap<RelatedPostsRequest, Continuation<FetchRelatedPostsState>?> = mutableMapOf()

    suspend fun fetchRelatedPosts(sourcePost: ReaderPost): FetchRelatedPostsState {
        val request = RelatedPostsRequest(postId = sourcePost.postId, blogId = sourcePost.blogId)

        return when {
            continuations[request] != null -> AlreadyRunning

            !networkUtilsWrapper.isNetworkAvailable() -> NoNetwork

            else -> {
                suspendCancellableCoroutine { cont ->
                    continuations[request] = cont
                    readerPostActionsWrapper.requestRelatedPosts(sourcePost)
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    @SuppressWarnings("unused")
    fun onRelatedPostUpdated(event: RelatedPostsUpdated) {
        val result = if (event.didSucceed()) {
            Success(
                    localRelatedPosts = event.localRelatedPosts,
                    globalRelatedPosts = event.globalRelatedPosts
            )
        } else {
            RequestFailed
        }

        val request = RelatedPostsRequest(postId = event.sourcePostId, blogId = event.sourceSiteId)
        continuations[request]?.resume(result)
        continuations[request] = null
    }

    sealed class FetchRelatedPostsState {
        data class Success(
            val localRelatedPosts: ReaderSimplePostList,
            val globalRelatedPosts: ReaderSimplePostList
        ) : FetchRelatedPostsState()

        object AlreadyRunning : FetchRelatedPostsState()
        sealed class Failed : FetchRelatedPostsState() {
            object NoNetwork : Failed()
            object RequestFailed : Failed()
        }
    }

    data class RelatedPostsRequest(
        val postId: Long,
        val blogId: Long
    )
}
