package org.sitebay.android.ui.engagement

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.flow
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.sitebay.android.TEST_DISPATCHER
import org.sitebay.android.fluxc.model.LikeModel.LikeType.COMMENT_LIKE
import org.sitebay.android.fluxc.model.LikeModel.LikeType.POST_LIKE
import org.sitebay.android.test
import org.sitebay.android.ui.engagement.GetLikesUseCase.CurrentUserInListRequirement.DONT_CARE
import org.sitebay.android.ui.engagement.GetLikesUseCase.FailureType
import org.sitebay.android.ui.engagement.GetLikesUseCase.GetLikesState
import org.sitebay.android.ui.engagement.GetLikesUseCase.GetLikesState.Failure
import org.sitebay.android.ui.engagement.GetLikesUseCase.GetLikesState.Failure.EmptyStateData
import org.sitebay.android.ui.engagement.GetLikesUseCase.GetLikesState.LikesData
import org.sitebay.android.ui.engagement.GetLikesUseCase.LikeGroupFingerPrint
import org.sitebay.android.ui.engagement.GetLikesUseCase.PaginationParams
import org.sitebay.android.ui.engagement.utils.getDefaultLikers
import org.sitebay.android.ui.pages.SnackbarMessageHolder
import org.sitebay.android.ui.utils.UiString.UiStringText

@InternalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class GetLikesHandlerTest {
    @Rule
    @JvmField val rule = InstantTaskExecutorRule()

    @Mock lateinit var getLikesUseCase: GetLikesUseCase

    private lateinit var getLikesHandler: GetLikesHandler
    private var likesState: GetLikesState? = null
    private var holder: SnackbarMessageHolder? = null

    private val siteId = 100L
    private val postId = 1000L
    private val commentId = 10000L
    private val expectedNumLikes = 6
    private val defaultPageLenght = 20
    private val noLikesLimit = -1

    @Before
    fun setup() {
        getLikesHandler = GetLikesHandler(getLikesUseCase, TEST_DISPATCHER)
    }

    @Test
    fun `handleGetLikesForPost collects expected state`() = test {
        val fingerPrint = LikeGroupFingerPrint(siteId, postId, expectedNumLikes)
        val paginationParams = PaginationParams(false, defaultPageLenght, noLikesLimit)
        val likesData = getDefaultLikers(expectedNumLikes, POST_LIKE, siteId, postId)

        val state = LikesData(
                likes = likesData,
                expectedNumLikes = expectedNumLikes,
                hasMore = false
        )

        whenever(getLikesUseCase.getLikesForPost(fingerPrint, paginationParams, DONT_CARE)).thenReturn(
                flow { emit(state) }
        )

        setupObservers()

        getLikesHandler.handleGetLikesForPost(
                fingerPrint,
                paginationParams.requestNextPage,
                paginationParams.pageLength,
                paginationParams.limit
        )

        requireNotNull(likesState).let {
            assertThat(it).isEqualTo(state)
        }

        assertThat(holder).isNull()
    }

    @Test
    fun `handleGetLikesForPost forwards failures signaling to snackbar`() = test {
        val error = UiStringText("An error occurred")
        val fingerPrint = LikeGroupFingerPrint(siteId, postId, expectedNumLikes)
        val paginationParams = PaginationParams(false, defaultPageLenght, noLikesLimit)
        val likesData = getDefaultLikers(expectedNumLikes, POST_LIKE, siteId, postId)

        val state = Failure(
                failureType = FailureType.GENERIC,
                error = error,
                cachedLikes = likesData,
                emptyStateData = EmptyStateData(false),
                expectedNumLikes = expectedNumLikes,
                hasMore = false
        )

        whenever(getLikesUseCase.getLikesForPost(fingerPrint, paginationParams, DONT_CARE)).thenReturn(
                flow { emit(state) }
        )

        setupObservers()

        getLikesHandler.handleGetLikesForPost(
                fingerPrint,
                paginationParams.requestNextPage,
                paginationParams.pageLength,
                paginationParams.limit
        )

        requireNotNull(likesState).let {
            assertThat(it).isEqualTo(state)
        }

        requireNotNull(holder).let {
            assertThat(it.message).isEqualTo(error)
        }
    }

    @Test
    fun `handleGetLikesForComment collects expected state`() = test {
        val fingerPrint = LikeGroupFingerPrint(siteId, commentId, expectedNumLikes)
        val paginationParams = PaginationParams(false, defaultPageLenght, noLikesLimit)
        val likesData = getDefaultLikers(expectedNumLikes, COMMENT_LIKE, siteId, commentId)

        val state = LikesData(
                likes = likesData,
                expectedNumLikes = expectedNumLikes,
                hasMore = false
        )

        whenever(getLikesUseCase.getLikesForComment(fingerPrint, paginationParams)).thenReturn(
                flow { emit(state) }
        )

        setupObservers()

        getLikesHandler.handleGetLikesForComment(
                fingerPrint,
                paginationParams.requestNextPage,
                paginationParams.pageLength
        )

        requireNotNull(likesState).let {
            assertThat(it).isEqualTo(state)
        }

        assertThat(holder).isNull()
    }

    @Test
    fun `handleGetLikesForComment forwards failures signaling to snackbar`() = test {
        val error = UiStringText("An error occurred")
        val fingerPrint = LikeGroupFingerPrint(siteId, commentId, expectedNumLikes)
        val paginationParams = PaginationParams(false, defaultPageLenght, noLikesLimit)
        val likesData = getDefaultLikers(expectedNumLikes, COMMENT_LIKE, siteId, commentId)

        val state = Failure(
                failureType = FailureType.GENERIC,
                error = error,
                cachedLikes = likesData,
                emptyStateData = EmptyStateData(false),
                expectedNumLikes = expectedNumLikes,
                hasMore = false
        )

        whenever(getLikesUseCase.getLikesForComment(fingerPrint, paginationParams)).thenReturn(
                flow { emit(state) }
        )

        setupObservers()

        getLikesHandler.handleGetLikesForComment(
                fingerPrint,
                paginationParams.requestNextPage,
                paginationParams.pageLength
        )

        requireNotNull(likesState).let {
            assertThat(it).isEqualTo(state)
        }

        requireNotNull(holder).let {
            assertThat(it.message).isEqualTo(error)
        }
    }

    @Test
    fun `clear calls clear on use case`() {
        getLikesHandler.clear()

        verify(getLikesUseCase, times(1)).clear()
    }

    private fun setupObservers() {
        likesState = null

        getLikesHandler.likesStatusUpdate.observeForever {
            likesState = it
        }

        holder = null

        getLikesHandler.snackbarEvents.observeForever { event ->
            event.applyIfNotHandled {
                holder = this
            }
        }
    }
}
