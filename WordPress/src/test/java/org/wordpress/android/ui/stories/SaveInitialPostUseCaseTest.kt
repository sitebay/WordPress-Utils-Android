package org.sitebay.android.ui.stories

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.InternalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.sitebay.android.BaseUnitTest
import org.sitebay.android.TEST_DISPATCHER
import org.sitebay.android.fluxc.model.PostModel
import org.sitebay.android.fluxc.model.SiteModel
import org.sitebay.android.fluxc.model.post.PostStatus
import org.sitebay.android.fluxc.store.PostStore
import org.sitebay.android.ui.posts.EditPostRepository
import org.sitebay.android.ui.posts.SavePostToDbUseCase

class SaveInitialPostUseCaseTest : BaseUnitTest() {
    private lateinit var editPostRepository: EditPostRepository
    private lateinit var saveInitialPostUseCase: SaveInitialPostUseCase
    @Mock lateinit var site: SiteModel
    @Mock lateinit var savePostToDbUseCase: SavePostToDbUseCase
    @Mock lateinit var postStore: PostStore

    @InternalCoroutinesApi
    @Before
    fun setup() {
        saveInitialPostUseCase = SaveInitialPostUseCase(postStore, savePostToDbUseCase)
        editPostRepository = EditPostRepository(mock(), mock(), mock(), TEST_DISPATCHER, TEST_DISPATCHER)
        whenever(postStore.instantiatePostModel(anyOrNull(), any(), anyOrNull(), anyOrNull())).thenReturn(PostModel())
    }

    @Test
    fun `if saveInitialPost is called then the PostModel should get set with a PostStatus of DRAFT`() {
        // arrange
        val expectedPostStatus = PostStatus.DRAFT

        // act
        saveInitialPostUseCase.saveInitialPost(editPostRepository, site)

        // assert
        assertThat(editPostRepository.status).isEqualTo(expectedPostStatus)
    }

    @Test
    fun `if saveInitialPost is called and the site is not null then savePostToDbUseCase is invoked`() {
        // arrange
        val nonNullSite: SiteModel? = mock()

        // act
        saveInitialPostUseCase.saveInitialPost(editPostRepository, nonNullSite)

        // assert
        verify(savePostToDbUseCase, times(1)).savePostToDb(any(), any())
    }

    @Test
    fun `if saveInitialPost is called and the site is null then savePostToDbUseCase is not invoked`() {
        // arrange
        val nullSite: SiteModel? = null

        // act
        saveInitialPostUseCase.saveInitialPost(editPostRepository, nullSite)

        // assert
        verify(savePostToDbUseCase, times(0)).savePostToDb(any(), any())
    }
}
