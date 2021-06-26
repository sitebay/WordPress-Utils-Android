package org.sitebay.android.ui.reader

import androidx.lifecycle.MutableLiveData
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.InternalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mock
import org.sitebay.android.BaseUnitTest
import org.sitebay.android.TEST_DISPATCHER
import org.sitebay.android.test
import org.sitebay.android.ui.pages.SnackbarMessageHolder
import org.sitebay.android.ui.reader.FollowCommentsUiStateType.VISIBLE_WITH_STATE
import org.sitebay.android.ui.reader.ReaderCommentListViewModel.ScrollPosition
import org.sitebay.android.ui.reader.usecases.ReaderCommentsFollowUseCase.FollowCommentsState
import org.sitebay.android.ui.reader.usecases.ReaderCommentsFollowUseCase.FollowCommentsState.FollowStateChanged
import org.sitebay.android.viewmodel.Event

@InternalCoroutinesApi
class ReaderCommentListViewModelTest : BaseUnitTest() {
    @Mock lateinit var followCommentsHandler: ReaderFollowCommentsHandler

    private lateinit var viewModel: ReaderCommentListViewModel
    private val blogId = 100L
    private val postId = 1000L
    private var snackbarEvents = MutableLiveData<Event<SnackbarMessageHolder>>()
    private var followStatusUpdate = MutableLiveData<FollowCommentsState>()
    private var uiState: FollowCommentsUiState? = null

    @Before
    fun setUp() {
        whenever(followCommentsHandler.snackbarEvents).thenReturn(snackbarEvents)
        whenever(followCommentsHandler.followStatusUpdate).thenReturn(followStatusUpdate)

        viewModel = ReaderCommentListViewModel(
                followCommentsHandler,
                TEST_DISPATCHER,
                TEST_DISPATCHER
        )
    }

    @Test
    fun `emits scroll event on scroll`() {
        var scrollEvent: Event<ScrollPosition>? = null
        viewModel.scrollTo.observeForever {
            scrollEvent = it
        }

        val expectedPosition = 10
        val isSmooth = true

        viewModel.scrollToPosition(expectedPosition, isSmooth)

        val scrollPosition = scrollEvent?.getContentIfNotHandled()!!

        assertThat(scrollPosition.isSmooth).isEqualTo(isSmooth)
        assertThat(scrollPosition.position).isEqualTo(expectedPosition)
    }

    @Test
    fun `follow ui state is DISABLED on start`() {
        setupObserversAndStart()

        assertThat(uiState).isNotNull
        assertThat(uiState!!.type).isEqualTo(FollowCommentsUiStateType.DISABLED)
    }

    @Test
    fun `onSwipeToRefresh updates follow conversation status`() = test {
        var stateChanged = FollowStateChanged(blogId, postId, true, true)
        doAnswer {
            followStatusUpdate.postValue(stateChanged)
        }.whenever(followCommentsHandler).handleFollowCommentsStatusRequest(anyLong(), anyLong(), anyBoolean())

        setupObserversAndStart()

        requireNotNull(uiState).let {
            assertThat(it.type).isEqualTo(VISIBLE_WITH_STATE)
            assertThat(it.animate).isFalse()
        }

        stateChanged = FollowStateChanged(blogId, postId, true, false)
        doAnswer {
            followStatusUpdate.postValue(stateChanged)
        }.whenever(followCommentsHandler).handleFollowCommentsStatusRequest(anyLong(), anyLong(), anyBoolean())

        viewModel.onSwipeToRefresh()

        requireNotNull(uiState).let {
            assertThat(it.type).isEqualTo(VISIBLE_WITH_STATE)
            assertThat(it.animate).isTrue()
        }
    }

    @Test
    fun `onFollowConversationClicked toggles follow button status`() = test {
        var stateChanged = FollowStateChanged(blogId, postId, true)
        doAnswer {
            followStatusUpdate.postValue(stateChanged)
        }.whenever(followCommentsHandler).handleFollowCommentsStatusRequest(anyLong(), anyLong(), anyBoolean())

        doAnswer {
            stateChanged = FollowStateChanged(blogId, postId, false)
            followStatusUpdate.postValue(stateChanged)
        }.whenever(followCommentsHandler).handleFollowCommentsClicked(blogId, postId, false)

        setupObserversAndStart()

        requireNotNull(uiState).let {
            assertThat(it.type).isEqualTo(VISIBLE_WITH_STATE)
            assertThat(it.isFollowing).isTrue()
            it.onFollowButtonClick?.invoke(false)
        }

        requireNotNull(uiState).let {
            assertThat(it.type).isEqualTo(VISIBLE_WITH_STATE)
            assertThat(it.isFollowing).isFalse()
        }
    }

    private fun setupObserversAndStart() {
        viewModel.updateFollowUiState.observeForever {
            uiState = it
        }

        viewModel.start(blogId, postId)
    }
}
