package org.sitebay.android.ui.reader.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.sitebay.android.R
import org.sitebay.android.analytics.AnalyticsTracker.Stat
import org.sitebay.android.datasets.wrappers.ReaderPostTableWrapper
import org.sitebay.android.fluxc.store.AccountStore
import org.sitebay.android.ui.reader.tracker.ReaderTracker
import org.sitebay.android.ui.reader.usecases.ReaderCommentsFollowUseCase.AnalyticsFollowCommentsAction.FOLLOW_COMMENTS
import org.sitebay.android.ui.reader.usecases.ReaderCommentsFollowUseCase.AnalyticsFollowCommentsAction.UNFOLLOW_COMMENTS
import org.sitebay.android.ui.reader.usecases.ReaderCommentsFollowUseCase.AnalyticsFollowCommentsActionResult.ERROR
import org.sitebay.android.ui.reader.usecases.ReaderCommentsFollowUseCase.AnalyticsFollowCommentsActionResult.SUCCEEDED
import org.sitebay.android.ui.reader.usecases.ReaderCommentsFollowUseCase.AnalyticsFollowCommentsGenericError.NO_NETWORK
import org.sitebay.android.ui.reader.usecases.ReaderCommentsFollowUseCase.FollowCommentsState.UserNotAuthenticated
import org.sitebay.android.ui.reader.utils.PostSubscribersApiCallsProvider
import org.sitebay.android.ui.reader.utils.PostSubscribersApiCallsProvider.PostSubscribersCallResult.Failure
import org.sitebay.android.ui.reader.utils.PostSubscribersApiCallsProvider.PostSubscribersCallResult.Success
import org.sitebay.android.ui.utils.UiString
import org.sitebay.android.ui.utils.UiString.UiStringRes
import org.sitebay.android.ui.utils.UiString.UiStringText
import org.sitebay.android.util.NetworkUtilsWrapper
import javax.inject.Inject

class ReaderCommentsFollowUseCase @Inject constructor(
    private val networkUtilsWrapper: NetworkUtilsWrapper,
    private val postSubscribersApiCallsProvider: PostSubscribersApiCallsProvider,
    private val accountStore: AccountStore,
    private val readerTracker: ReaderTracker,
    private val readerPostTableWrapper: ReaderPostTableWrapper
) {
    private val FOLLOW_COMMENT_ACTION = "follow_action"
    private val FOLLOW_COMMENT_ACTION_RESULT = "follow_action_result"
    private val FOLLOW_COMMENT_ACTION_ERROR = "follow_action_error"

    suspend fun getMySubscriptionToPost(blogId: Long, postId: Long, isInit: Boolean) = flow {
        if (!accountStore.hasAccessToken()) {
            emit(UserNotAuthenticated)
        } else {
            emit(FollowCommentsState.Loading)

            if (!networkUtilsWrapper.isNetworkAvailable()) {
                emit(FollowCommentsState.Failure(blogId, postId, UiStringRes(R.string.error_network_connection)))
            } else {
                val canFollowComments = postSubscribersApiCallsProvider.getCanFollowComments(blogId)

                if (!canFollowComments) {
                    emit(FollowCommentsState.FollowCommentsNotAllowed)
                } else {
                    val status = postSubscribersApiCallsProvider.getMySubscriptionToPost(blogId, postId)

                    when (status) {
                        is Success -> {
                            emit(
                                    FollowCommentsState.FollowStateChanged(
                                            blogId,
                                            postId,
                                            status.isFollowing,
                                            isInit
                                    )
                            )
                        }
                        is Failure -> {
                            emit(FollowCommentsState.Failure(blogId, postId, UiStringText(status.error)))
                        }
                    }
                }
            }
        }
    }

    suspend fun setMySubscriptionToPost(
        blogId: Long,
        postId: Long,
        subscribe: Boolean
    ): Flow<FollowCommentsState> = flow {
        val properties = mutableMapOf<String, Any?>()

        properties.addFollowAction(subscribe)

        emit(FollowCommentsState.Loading)

        if (!networkUtilsWrapper.isNetworkAvailable()) {
            emit(FollowCommentsState.Failure(blogId, postId, UiStringRes(R.string.error_network_connection)))
            properties.addFollowActionResult(ERROR, NO_NETWORK.errorMessage)
        } else {
            val status = if (subscribe) {
                postSubscribersApiCallsProvider.subscribeMeToPost(blogId, postId)
            } else {
                postSubscribersApiCallsProvider.unsubscribeMeFromPost(blogId, postId)
            }

            when (status) {
                is Success -> {
                    emit(
                            FollowCommentsState.FollowStateChanged(
                                    blogId,
                                    postId,
                                    status.isFollowing,
                                    false,
                                    UiStringRes(
                                        if (status.isFollowing)
                                            R.string.reader_follow_comments_subscribe_success
                                        else
                                            R.string.reader_follow_comments_unsubscribe_success
                                    )
                            )
                    )
                    properties.addFollowActionResult(SUCCEEDED)
                }
                is Failure -> {
                    emit(FollowCommentsState.Failure(blogId, postId, UiStringText(status.error)))
                    properties.addFollowActionResult(ERROR, status.error)
                }
            }
        }

        val post = readerPostTableWrapper.getBlogPost(blogId, postId, true)

        readerTracker.trackPostComments(
                Stat.COMMENT_FOLLOW_CONVERSATION,
                blogId,
                postId,
                post,
                properties
        )
    }

    sealed class FollowCommentsState {
        object Loading : FollowCommentsState()

        data class FollowStateChanged(
            val blogId: Long,
            val postId: Long,
            val isFollowing: Boolean,
            val isInit: Boolean = false,
            val userMessage: UiString? = null
        ) : FollowCommentsState()

        data class Failure(
            val blogId: Long,
            val postId: Long,
            val error: UiString
        ) : FollowCommentsState()

        object FollowCommentsNotAllowed : FollowCommentsState()

        object UserNotAuthenticated : FollowCommentsState()
    }

    private enum class AnalyticsFollowCommentsAction(val action: String) {
        FOLLOW_COMMENTS("followed"),
        UNFOLLOW_COMMENTS("unfollowed")
    }

    private enum class AnalyticsFollowCommentsActionResult(val actionResult: String) {
        SUCCEEDED("succeeded"),
        ERROR("error")
    }

    private enum class AnalyticsFollowCommentsGenericError(val errorMessage: String) {
        NO_NETWORK("no_network")
    }

    private fun MutableMap<String, Any?>.addFollowAction(subscribe: Boolean): MutableMap<String, Any?> {
        this[FOLLOW_COMMENT_ACTION] = if (subscribe) {
            FOLLOW_COMMENTS.action
        } else {
            UNFOLLOW_COMMENTS.action
        }
        return this
    }

    private fun MutableMap<String, Any?>.addFollowActionResult(
        result: AnalyticsFollowCommentsActionResult,
        errorMessage: String? = null
    ): MutableMap<String, Any?> {
        this[FOLLOW_COMMENT_ACTION_RESULT] = result.actionResult
        errorMessage?.also {
            this[FOLLOW_COMMENT_ACTION_ERROR] = errorMessage
        }
        return this
    }
}
