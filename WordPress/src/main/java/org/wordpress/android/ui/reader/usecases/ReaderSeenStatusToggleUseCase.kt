package org.sitebay.android.ui.reader.usecases

import kotlinx.coroutines.flow.flow
import org.sitebay.android.R.string
import org.sitebay.android.analytics.AnalyticsTracker
import org.sitebay.android.datasets.ReaderBlogTableWrapper
import org.sitebay.android.datasets.wrappers.ReaderPostTableWrapper
import org.sitebay.android.fluxc.store.AccountStore
import org.sitebay.android.models.ReaderPost
import org.sitebay.android.ui.reader.tracker.ReaderTracker
import org.sitebay.android.ui.reader.usecases.ReaderSeenStatusToggleUseCase.PostSeenState.Error
import org.sitebay.android.ui.reader.usecases.ReaderSeenStatusToggleUseCase.PostSeenState.PostSeenStateChanged
import org.sitebay.android.ui.reader.usecases.ReaderSeenStatusToggleUseCase.PostSeenState.UserNotAuthenticated
import org.sitebay.android.ui.reader.usecases.ReaderSeenStatusToggleUseCase.ReaderPostSeenToggleSource.READER_POST_DETAILS
import org.sitebay.android.ui.reader.utils.PostSeenStatusApiCallsProvider
import org.sitebay.android.ui.reader.utils.PostSeenStatusApiCallsProvider.SeenStatusToggleCallResult.Failure
import org.sitebay.android.ui.reader.utils.PostSeenStatusApiCallsProvider.SeenStatusToggleCallResult.Success
import org.sitebay.android.ui.utils.UiString
import org.sitebay.android.ui.utils.UiString.UiStringRes
import org.sitebay.android.ui.utils.UiString.UiStringText
import org.sitebay.android.util.NetworkUtilsWrapper
import javax.inject.Inject

class ReaderSeenStatusToggleUseCase @Inject constructor(
    private val networkUtilsWrapper: NetworkUtilsWrapper,
    private val apiCallsProvider: PostSeenStatusApiCallsProvider,
    private val accountStore: AccountStore,
    private val readerTracker: ReaderTracker,
    private val readerPostTableWrapper: ReaderPostTableWrapper,
    private val readerBlogTableWrapper: ReaderBlogTableWrapper
) {
    /**
     * Convenience method for toggling seen status based on the current state in local DB
     */
    suspend fun toggleSeenStatus(post: ReaderPost, actionSource: ReaderPostSeenToggleSource) = flow {
        if (!networkUtilsWrapper.isNetworkAvailable()) {
            emit(Error(UiStringRes(string.error_network_connection)))
            return@flow
        }

        if (!accountStore.hasAccessToken()) {
            emit(UserNotAuthenticated)
            return@flow
        }

        if (!post.isSeenSupported) {
            emit(Error(UiStringRes(string.reader_error_changing_seen_status_of_unsupported_post)))
            return@flow
        }

        val isAskingToMarkAsSeen = !readerPostTableWrapper.isPostSeen(post)
        val status = if (isAskingToMarkAsSeen) {
            markPostAsSeen(post, actionSource)
        } else {
            markPostAsUnseen(post, actionSource)
        }

        emit(status)
    }

    suspend fun markPostAsSeenIfNecessary(post: ReaderPost) {
        if (!networkUtilsWrapper.isNetworkAvailable() || !accountStore.hasAccessToken() || !post.isSeenSupported) {
            return
        }

        if (!readerPostTableWrapper.isPostSeen(post)) {
            markPostAsSeen(post, READER_POST_DETAILS, true)
        }
    }

    private suspend fun markPostAsSeen(
        post: ReaderPost,
        actionSource: ReaderPostSeenToggleSource,
        doNotTrack: Boolean = false
    ): PostSeenState {
        return when (val status = apiCallsProvider.markPostAsSeen(post)) {
            is Success -> {
                readerPostTableWrapper.setPostSeenStatusInDb(post, true)
                readerBlogTableWrapper.decrementUnseenCount(post.blogId)
                if (!doNotTrack) {
                    readerTracker.trackPost(
                            AnalyticsTracker.Stat.READER_POST_MARKED_AS_SEEN,
                            post,
                            actionSource.toString()
                    )
                }
                PostSeenStateChanged(true, UiStringRes(string.reader_marked_post_as_seen))
            }
            is Failure -> {
                Error(UiStringText(status.error))
            }
        }
    }

    private suspend fun markPostAsUnseen(post: ReaderPost, actionSource: ReaderPostSeenToggleSource): PostSeenState {
        return when (val status = apiCallsProvider.markPostAsUnseen(post)) {
            is Success -> {
                readerPostTableWrapper.setPostSeenStatusInDb(post, false)
                readerBlogTableWrapper.incrementUnseenCount(post.blogId)
                readerTracker.trackPost(
                        AnalyticsTracker.Stat.READER_POST_MARKED_AS_UNSEEN,
                        post,
                        actionSource.toString()
                )
                PostSeenStateChanged(false, UiStringRes(string.reader_marked_post_as_unseen))
            }
            is Failure -> {
                Error(UiStringText(status.error))
            }
        }
    }

    sealed class PostSeenState {
        data class PostSeenStateChanged(
            val isSeen: Boolean,
            val userMessage: UiString? = null
        ) : PostSeenState()

        data class Error(
            val message: UiString? = null
        ) : PostSeenState()

        object UserNotAuthenticated : PostSeenState()
    }

    enum class ReaderPostSeenToggleSource {
        READER_POST_CARD {
            override fun toString(): String {
                return "post_card"
            }
        },
        READER_POST_DETAILS {
            override fun toString(): String {
                return "post_details"
            }
        }
    }
}
