package org.sitebay.android.ui.posts

import androidx.lifecycle.MutableLiveData
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.InternalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.sitebay.android.BaseUnitTest
import org.sitebay.android.TEST_DISPATCHER
import org.sitebay.android.fluxc.Dispatcher
import org.sitebay.android.fluxc.model.LocalOrRemoteId.LocalId
import org.sitebay.android.fluxc.model.PostModel
import org.sitebay.android.fluxc.model.SiteModel
import org.sitebay.android.ui.posts.PostListViewLayoutType.COMPACT
import org.sitebay.android.ui.posts.PostListViewLayoutType.STANDARD
import org.sitebay.android.ui.prefs.AppPrefsWrapper
import org.sitebay.android.ui.uploads.UploadStarter
import org.sitebay.android.viewmodel.Event

class PostListMainViewModelTest : BaseUnitTest() {
    @Mock lateinit var site: SiteModel
    private val currentBottomSheetPostId = LocalId(0)
    @Mock lateinit var uploadStarter: UploadStarter
    @Mock lateinit var dispatcher: Dispatcher
    @Mock lateinit var editPostRepository: EditPostRepository
    @Mock lateinit var savePostToDbUseCase: SavePostToDbUseCase
    private lateinit var viewModel: PostListMainViewModel

    @InternalCoroutinesApi
    @Before
    fun setUp() {
        val prefs = mock<AppPrefsWrapper> {
            on { postListViewLayoutType } doReturn STANDARD
        }

        whenever(editPostRepository.postChanged).thenReturn(MutableLiveData(Event(PostModel())))

        viewModel = PostListMainViewModel(
                dispatcher = dispatcher,
                postStore = mock(),
                accountStore = mock(),
                uploadStore = mock(),
                mediaStore = mock(),
                networkUtilsWrapper = mock(),
                prefs = prefs,
                previewStateHelper = mock(),
                analyticsTracker = mock(),
                mainDispatcher = Dispatchers.Unconfined,
                bgDispatcher = Dispatchers.Unconfined,
                postListEventListenerFactory = mock(),
                uploadStarter = uploadStarter,
                uploadActionUseCase = mock(),
                savePostToDbUseCase = savePostToDbUseCase
        )
    }

    @Test
    fun `when started, it uploads all local drafts`() {
        viewModel.start(site, PostListRemotePreviewState.NONE, currentBottomSheetPostId, editPostRepository, mock())

        verify(uploadStarter, times(1)).queueUploadFromSite(eq(site))
    }

    @Test
    fun `calling onSearch() updates search query`() {
        val testSearch = "keyword"
        viewModel.start(site, PostListRemotePreviewState.NONE, currentBottomSheetPostId, editPostRepository, mock())

        var searchQuery: String? = null
        viewModel.searchQuery.observeForever {
            searchQuery = it
        }

        viewModel.onSearch(testSearch)

        assertThat(searchQuery).isEqualTo(testSearch)
    }

    @Test
    fun `expanding and collapsing search triggers isSearchExpanded`() {
        viewModel.start(site, PostListRemotePreviewState.NONE, currentBottomSheetPostId, editPostRepository, mock())

        var isSearchExpanded = false
        viewModel.isSearchExpanded.observeForever {
            isSearchExpanded = it
        }

        viewModel.onSearchExpanded(false)
        assertThat(isSearchExpanded).isTrue()

        viewModel.onSearchCollapsed(delay = 0)
        assertThat(isSearchExpanded).isFalse()
    }

    @Test
    fun `expanding search after configuration change preserves search query`() {
        val testSearch = "keyword"

        viewModel.start(site, PostListRemotePreviewState.NONE, currentBottomSheetPostId, editPostRepository, mock())

        var searchQuery: String? = null
        viewModel.searchQuery.observeForever {
            searchQuery = it
        }

        viewModel.onSearch(testSearch)

        assertThat(searchQuery).isNotNull()
        assertThat(searchQuery).isEqualTo(testSearch)

        viewModel.onSearchExpanded(true)
        assertThat(searchQuery).isEqualTo(testSearch)

        viewModel.onSearchCollapsed(0)

        viewModel.onSearchExpanded(false)
        assertThat(searchQuery).isNull()
    }

    @Test
    fun `search is using compact view mode independently from normal post list`() {
        viewModel.start(site, PostListRemotePreviewState.NONE, currentBottomSheetPostId, editPostRepository, mock())
        assertThat(viewModel.viewLayoutType.value).isEqualTo(STANDARD) // default value

        var viewLayoutType: PostListViewLayoutType? = null
        viewModel.viewLayoutType.observeForever {
            viewLayoutType = it
        }

        viewModel.onSearchExpanded(false)

        assertThat(viewLayoutType).isEqualTo(COMPACT)

        viewModel.onSearchCollapsed()

        assertThat(viewLayoutType).isEqualTo(STANDARD)
    }

    @Test
    fun `if currentBottomSheetPostId isn't 0 then set the post in editPostRepository from the postStore`() {
        // arrange
        val bottomSheetPostId = LocalId(2)

        // act
        viewModel.start(site, PostListRemotePreviewState.NONE, bottomSheetPostId, editPostRepository, mock())

        // assert
        verify(editPostRepository, times(1)).loadPostByLocalPostId(any())
    }

    @Test
    fun `if currentBottomSheetPostId is 0 then don't set the post in editPostRepository from the postStore`() {
        // arrange
        val bottomSheetPostId = LocalId(0)

        // act
        viewModel.start(site, PostListRemotePreviewState.NONE, bottomSheetPostId, editPostRepository, mock())

        // assert
        verify(editPostRepository, times(0)).loadPostByLocalPostId(any())
    }

    @InternalCoroutinesApi
    @Test
    fun `if post in EditPostRepository is modified then the savePostToDbUseCase should update the post`() {
        // arrange
        val editPostRepository = EditPostRepository(mock(), mock(), mock(), TEST_DISPATCHER, TEST_DISPATCHER)
        editPostRepository.set { mock() }
        val action = { _: PostModel -> true }

        // act
        viewModel.start(site, PostListRemotePreviewState.NONE, currentBottomSheetPostId, editPostRepository, mock())
        // simulates the Publish Date, Status & Visibility or Tags being updated in the bottom sheet.
        editPostRepository.updateAsync(action, null)

        // assert
        verify(savePostToDbUseCase, times(1)).savePostToDb(any(), any())
    }

    @Test
    fun `if onFabClicked then _onFabClicked is called`() {
        whenever(site.isWPCom).thenReturn(true)

        viewModel.start(site, PostListRemotePreviewState.NONE, currentBottomSheetPostId, editPostRepository, mock())
        viewModel.fabClicked()

        assertThat(viewModel.onFabClicked.value?.peekContent()).isNotNull
    }

    @Test
    fun `if onFabLongPressed then onFabLongPressedForCreateMenu is called`() {
        whenever(site.isWPCom).thenReturn(true)

        viewModel.start(site, PostListRemotePreviewState.NONE, currentBottomSheetPostId, editPostRepository, mock())
        viewModel.onFabLongPressed()

        assertThat(viewModel.onFabLongPressedForCreateMenu.value?.peekContent()).isNotNull
    }
}
