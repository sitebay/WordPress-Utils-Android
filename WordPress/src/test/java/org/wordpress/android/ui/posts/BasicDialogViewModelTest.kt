package org.sitebay.android.ui.posts

import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.sitebay.android.BaseUnitTest
import org.sitebay.android.ui.posts.BasicDialogViewModel.DialogInteraction
import org.sitebay.android.ui.posts.BasicDialogViewModel.DialogInteraction.Dismissed
import org.sitebay.android.ui.posts.BasicDialogViewModel.DialogInteraction.Negative
import org.sitebay.android.ui.posts.BasicDialogViewModel.DialogInteraction.Positive

class BasicDialogViewModelTest : BaseUnitTest() {
    private lateinit var interactions: MutableList<DialogInteraction>
    private lateinit var viewModel: BasicDialogViewModel
    private val tag = "tag"

    @Before
    fun setUp() {
        viewModel = BasicDialogViewModel()
        interactions = mutableListOf()
        viewModel.onInteraction.observeForever { event ->
            event?.getContentIfNotHandled()?.let {
                interactions.add(it)
            }
        }
    }

    @Test
    fun `propagates positive interaction`() {
        viewModel.onPositiveClicked(tag)

        assertThat(interactions).hasSize(1)
        assertThat(interactions.last().tag).isEqualTo(tag)
        assertThat(interactions.last() is Positive).isTrue()
    }

    @Test
    fun `propagates negative interaction`() {
        viewModel.onNegativeButtonClicked(tag)

        assertThat(interactions).hasSize(1)
        assertThat(interactions.last().tag).isEqualTo(tag)
        assertThat(interactions.last() is Negative).isTrue()
    }

    @Test
    fun `propagates dismiss interaction`() {
        viewModel.onDismissByOutsideTouch(tag)

        assertThat(interactions).hasSize(1)
        assertThat(interactions.last().tag).isEqualTo(tag)
        assertThat(interactions.last() is Dismissed).isTrue()
    }
}
