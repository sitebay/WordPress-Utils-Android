package org.sitebay.android.ui.reader.usecases

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.toList
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.sitebay.android.R
import org.sitebay.android.R.string
import org.sitebay.android.datasets.wrappers.ReaderPostTableWrapper
import org.sitebay.android.fluxc.store.AccountStore
import org.sitebay.android.test
import org.sitebay.android.ui.reader.tracker.ReaderTracker
import org.sitebay.android.ui.reader.usecases.ReaderCommentsFollowUseCase.FollowCommentsState.Failure
import org.sitebay.android.ui.reader.usecases.ReaderCommentsFollowUseCase.FollowCommentsState.FollowCommentsNotAllowed
import org.sitebay.android.ui.reader.usecases.ReaderCommentsFollowUseCase.FollowCommentsState.FollowStateChanged
import org.sitebay.android.ui.reader.usecases.ReaderCommentsFollowUseCase.FollowCommentsState.Loading
import org.sitebay.android.ui.reader.usecases.ReaderCommentsFollowUseCase.FollowCommentsState.UserNotAuthenticated
import org.sitebay.android.ui.reader.utils.PostSubscribersApiCallsProvider
import org.sitebay.android.ui.reader.utils.PostSubscribersApiCallsProvider.PostSubscribersCallResult
import org.sitebay.android.ui.utils.UiString.UiStringRes
import org.sitebay.android.ui.utils.UiString.UiStringText
import org.sitebay.android.util.NetworkUtilsWrapper

@InternalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class ReaderCommentsFollowUseCaseTest {
    @Rule
    @JvmField val rule = InstantTaskExecutorRule()

    @Mock private lateinit var networkUtilsWrapper: NetworkUtilsWrapper
    @Mock private lateinit var postSubscribersApiCallsProvider: PostSubscribersApiCallsProvider
    @Mock private lateinit var accountStore: AccountStore
    @Mock private lateinit var readerTracker: ReaderTracker
    @Mock private lateinit var readerPostTableWrapper: ReaderPostTableWrapper

    private lateinit var followCommentsUseCase: ReaderCommentsFollowUseCase

    private val blogId = 100L
    private val postId = 1000L

    @Before
    fun setup() {
        whenever(accountStore.hasAccessToken()).thenReturn(true)
        whenever(networkUtilsWrapper.isNetworkAvailable()).thenReturn(true)

        followCommentsUseCase = ReaderCommentsFollowUseCase(
                networkUtilsWrapper,
                postSubscribersApiCallsProvider,
                accountStore,
                readerTracker,
                readerPostTableWrapper
        )
    }

    @Test
    fun `getMySubscriptionToPost emits expected state when user not logged in`() = test {
        whenever(accountStore.hasAccessToken()).thenReturn(false)
        val flow = followCommentsUseCase.getMySubscriptionToPost(blogId, postId, false)

        assertThat(flow.toList()).isEqualTo(listOf(UserNotAuthenticated))
    }

    @Test
    fun `getMySubscriptionToPost emits expected state when no network`() = test {
        whenever(networkUtilsWrapper.isNetworkAvailable()).thenReturn(false)

        val flow = followCommentsUseCase.getMySubscriptionToPost(blogId, postId, false)

        assertThat(flow.toList()).isEqualTo(
                listOf(Loading, Failure(blogId, postId, UiStringRes(string.error_network_connection)))
        )
    }

    @Test
    fun `getMySubscriptionToPost emits expected state when cannot follow comments`() = test {
        whenever(postSubscribersApiCallsProvider.getCanFollowComments(anyLong())).thenReturn(false)

        val flow = followCommentsUseCase.getMySubscriptionToPost(blogId, postId, false)

        assertThat(flow.toList()).isEqualTo(listOf(Loading, FollowCommentsNotAllowed))
    }

    @Test
    fun `getMySubscriptionToPost emits expected state when can follow with success`() = test {
        whenever(postSubscribersApiCallsProvider.getCanFollowComments(anyLong())).thenReturn(true)
        whenever(postSubscribersApiCallsProvider.getMySubscriptionToPost(anyLong(), anyLong()))
                .thenReturn(PostSubscribersCallResult.Success(true))

        val flow = followCommentsUseCase.getMySubscriptionToPost(blogId, postId, false)

        assertThat(flow.toList()).isEqualTo(listOf(
                        Loading,
                        FollowStateChanged(
                            blogId,
                            postId,
                            true,
                            false
                        )
        ))
    }

    @Test
    fun `getMySubscriptionToPost emits expected state when can follow with failure`() = test {
        val errorMessage = "There was an error"
        val failure = PostSubscribersCallResult.Failure(errorMessage)

        whenever(postSubscribersApiCallsProvider.getCanFollowComments(anyLong())).thenReturn(true)
        whenever(postSubscribersApiCallsProvider.getMySubscriptionToPost(anyLong(), anyLong())).thenReturn(failure)

        val flow = followCommentsUseCase.getMySubscriptionToPost(blogId, postId, false)

        assertThat(flow.toList()).isEqualTo(listOf(
                Loading,
                Failure(blogId, postId, UiStringText(errorMessage))
        ))
    }

    @Test
    fun `setMySubscriptionToPost emits expected state when no network`() = test {
        whenever(networkUtilsWrapper.isNetworkAvailable()).thenReturn(false)

        val flow = followCommentsUseCase.setMySubscriptionToPost(blogId, postId, true)

        assertThat(flow.toList()).isEqualTo(
                listOf(Loading, Failure(blogId, postId, UiStringRes(string.error_network_connection)))
        )
    }

    @Test
    fun `setMySubscriptionToPost emits expected state when subscribing with success`() = test {
        whenever(postSubscribersApiCallsProvider.subscribeMeToPost(anyLong(), anyLong()))
                .thenReturn(PostSubscribersCallResult.Success(true))

        val flow = followCommentsUseCase.setMySubscriptionToPost(blogId, postId, true)

        assertThat(flow.toList()).isEqualTo(listOf(
                Loading,
                FollowStateChanged(
                        blogId,
                        postId,
                        true,
                        false,
                        UiStringRes(R.string.reader_follow_comments_subscribe_success)
                )
        ))
    }

    @Test
    fun `setMySubscriptionToPost emits expected state when unsubscribing with failure`() = test {
        val errorMessage = "There was an error"
        val failure = PostSubscribersCallResult.Failure(errorMessage)

        whenever(postSubscribersApiCallsProvider.unsubscribeMeFromPost(anyLong(), anyLong())).thenReturn(failure)

        val flow = followCommentsUseCase.setMySubscriptionToPost(blogId, postId, false)

        assertThat(flow.toList()).isEqualTo(listOf(
                Loading,
                Failure(blogId, postId, UiStringText(errorMessage))
        ))
    }
}
