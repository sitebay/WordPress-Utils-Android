package org.sitebay.android.ui.reader.usecases

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.toList
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.sitebay.android.datasets.wrappers.ReaderPostTableWrapper
import org.sitebay.android.models.ReaderPost
import org.sitebay.android.test
import org.sitebay.android.ui.reader.actions.ReaderPostActionsWrapper
import org.sitebay.android.ui.reader.tracker.ReaderTracker
import org.sitebay.android.ui.reader.usecases.BookmarkPostState.PreLoadPostContent
import org.sitebay.android.util.NetworkUtilsWrapper

private const val SOURCE = "source"

@InternalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class ReaderPostBookmarkUseCaseTest {
    @Rule
    @JvmField val rule = InstantTaskExecutorRule()

    lateinit var useCase: ReaderPostBookmarkUseCase
    @Mock lateinit var readerTracker: ReaderTracker
    @Mock lateinit var networkUtilsWrapper: NetworkUtilsWrapper
    @Mock lateinit var readerPostActionsWrapper: ReaderPostActionsWrapper
    @Mock lateinit var readerPostTableWrapper: ReaderPostTableWrapper

    @Before
    fun setup() {
        useCase = ReaderPostBookmarkUseCase(
                readerTracker,
                networkUtilsWrapper,
                readerPostActionsWrapper,
                readerPostTableWrapper
        )
    }

    @Test
    fun `bookmark action updates the bookmark state to true`() = test {
        // Arrange
        val dummyPost = init(isBookmarked = false)
        // Act
        useCase.toggleBookmark(
                post(),
                false,
                SOURCE
        ).toList(mutableListOf())

        // Assert
        verify(readerPostActionsWrapper).addToBookmarked(dummyPost)
    }

    @Test
    fun `unbookmark action updates the bookmark state to false`() = test {
        // Arrange
        val dummyPost = init(isBookmarked = true)
        // Act
        useCase.toggleBookmark(
                post(),
                false,
                SOURCE
        ).toList(mutableListOf())

        // Assert
        verify(readerPostActionsWrapper).removeFromBookmarked(dummyPost)
    }

    @Test
    fun `initiates content preload when network available`() = test {
        // Arrange
        init(isBookmarked = false, networkAvailable = true)

        // Act
        val result = useCase.toggleBookmark(
                post(),
                false,
                SOURCE
        ).toList(mutableListOf())

        // Assert
        assertThat(result.contains(PreLoadPostContent(0L, 0L))).isTrue
    }

    @Test
    fun `does not initiate content preload when network not available`() = test {
        // Arrange
        init(isBookmarked = false, networkAvailable = false)

        // Act
        val result = useCase.toggleBookmark(
                post(),
                false,
                SOURCE
        ).toList(mutableListOf())

        // Assert
        assertThat(result.contains(PreLoadPostContent(0L, 0L))).isFalse
    }

    @Test
    fun `does not initiate content preload on unbookmark action`() = test {
        // Arrange
        init(isBookmarked = true)

        // Act
        val result = useCase.toggleBookmark(
                post(),
                false,
                SOURCE
        ).toList(mutableListOf())

        // Assert
        assertThat(result.contains(PreLoadPostContent(0L, 0L))).isFalse
    }

    @Test
    fun `does not initiate content preload when on bookmarkList(savedTab)`() = test {
        // Arrange
        init()

        // Act
        val result = useCase.toggleBookmark(
                post(),
                true,
                SOURCE
        ).toList(mutableListOf())

        // Assert
        assertThat(result.contains(PreLoadPostContent(0L, 0L))).isFalse
    }

    private fun init(isBookmarked: Boolean = false, networkAvailable: Boolean = true): ReaderPost {
        val post = ReaderPost().apply { this.isBookmarked = isBookmarked }
        whenever(readerPostTableWrapper.getBlogPost(anyLong(), anyLong(), anyBoolean()))
                .thenReturn(post)
        whenever(networkUtilsWrapper.isNetworkAvailable()).thenReturn(networkAvailable)
        return post
    }

    private fun post(): ReaderPost {
        val post = ReaderPost()
        post.postId = 0L
        post.feedId = 0L
        post.postId = 0L
        post.isFollowedByCurrentUser = false
        return post
    }
}
