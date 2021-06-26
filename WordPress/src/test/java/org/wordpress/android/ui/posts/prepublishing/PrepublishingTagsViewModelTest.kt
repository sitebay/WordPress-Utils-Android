package org.sitebay.android.ui.posts.prepublishing

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.InternalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.sitebay.android.BaseUnitTest
import org.sitebay.android.R
import org.sitebay.android.TEST_DISPATCHER
import org.sitebay.android.test
import org.sitebay.android.ui.posts.PrepublishingTagsViewModel
import org.sitebay.android.ui.posts.UpdatePostTagsUseCase
import org.sitebay.android.ui.utils.UiString.UiStringRes
import org.sitebay.android.viewmodel.Event

@InternalCoroutinesApi
class PrepublishingTagsViewModelTest : BaseUnitTest() {
    private lateinit var viewModel: PrepublishingTagsViewModel
    @Mock lateinit var updatePostTagsUseCase: UpdatePostTagsUseCase

    @Before
    fun setup() {
        viewModel = PrepublishingTagsViewModel(mock(), updatePostTagsUseCase, TEST_DISPATCHER)
    }

    @Test
    fun `when viewModel is started updateToolbarTitle is called with the tags title`() {
        var title: UiStringRes? = null
        viewModel.toolbarTitleUiState.observeForever {
            title = it as UiStringRes
        }

        viewModel.start(mock())

        assertThat(title?.stringRes).isEqualTo(R.string.prepublishing_nudges_toolbar_title_tags)
    }

    @Test
    fun `when onBackClicked is triggered navigateToHomeScreen is called`() {
        var event: Event<Unit>? = null
        viewModel.navigateToHomeScreen.observeForever {
            event = it
        }

        viewModel.onBackButtonClicked()

        assertThat(event).isNotNull
    }

    @Test
    fun `when onTagsSelected is called updatePostTagsUseCase's updateTags should be called`() = test {
        val expectedTags = "test, data"
        val captor = ArgumentCaptor.forClass(String::class.java)
        doNothing().whenever(updatePostTagsUseCase).updateTags(captor.capture(), any())

        viewModel.start(mock())
        viewModel.onTagsSelected(expectedTags)

        assertThat(captor.value).isEqualTo(expectedTags)
    }

    @Test
    fun `when viewModel is started with closeKeyboard=false then dismissKeyboard is not called when tapping back`() {
        var event: Event<Unit>? = null
        viewModel.dismissKeyboard.observeForever {
            event = it
        }

        viewModel.start(mock(), closeKeyboard = false)
        viewModel.onBackButtonClicked()

        assertThat(event).isNull()
    }

    @Test
    fun `when viewModel is started with closeKeyboard=true then dismissKeyboard is called when tapping back`() {
        var event: Event<Unit>? = null
        viewModel.dismissKeyboard.observeForever {
            event = it
        }

        viewModel.start(mock(), closeKeyboard = true)
        viewModel.onBackButtonClicked()

        assertThat(event).isNotNull
    }
}
