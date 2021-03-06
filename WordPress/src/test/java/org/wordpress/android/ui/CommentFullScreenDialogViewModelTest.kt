package org.sitebay.android.ui

import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.sitebay.android.BaseUnitTest
import org.sitebay.android.viewmodel.Event

class CommentFullScreenDialogViewModelTest : BaseUnitTest() {
    private lateinit var viewModel: CommentFullScreenDialogViewModel
    @Before
    fun setUp() {
        viewModel = CommentFullScreenDialogViewModel()
    }

    @Test
    fun `on init opens keyboard`() {
        var openKeyboardEvent: Event<Unit>? = null
        viewModel.onKeyboardOpened.observeForever { openKeyboardEvent = it }

        viewModel.init()

        assertThat(openKeyboardEvent).isNotNull()
        assertThat(openKeyboardEvent!!.getContentIfNotHandled()).isNotNull()
    }
}
