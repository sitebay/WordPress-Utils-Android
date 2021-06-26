package org.sitebay.android.ui.bloggingreminders

import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.sitebay.android.R
import org.sitebay.android.ui.bloggingreminders.BloggingRemindersItem.HighEmphasisText
import org.sitebay.android.ui.bloggingreminders.BloggingRemindersItem.Illustration
import org.sitebay.android.ui.bloggingreminders.BloggingRemindersItem.Title
import org.sitebay.android.ui.bloggingreminders.BloggingRemindersViewModel.UiState.PrimaryButton
import org.sitebay.android.ui.utils.ListItemInteraction.Companion
import org.sitebay.android.ui.utils.UiString.UiStringRes

@RunWith(MockitoJUnitRunner::class)
class PrologueBuilderTest {
    private lateinit var prologueBuilder: PrologueBuilder
    private var confirmed = false

    private val onConfirm: () -> Unit = {
        confirmed = true
    }

    @Before
    fun setUp() {
        prologueBuilder = PrologueBuilder()
        confirmed = false
    }

    @Test
    fun `builds UI model with no selected days`() {
        val uiModel = prologueBuilder.buildUiItems()

        assertModel(uiModel)
    }

    @Test
    fun `builds primary button`() {
        val primaryButton = prologueBuilder.buildPrimaryButton(onConfirm)

        assertThat(primaryButton).isEqualTo(
                PrimaryButton(
                        UiStringRes(R.string.set_your_blogging_goals_button),
                        true,
                        Companion.create(onConfirm)
                )
        )
    }

    @Test
    fun `click on primary button confims selection`() {
        val primaryButton = prologueBuilder.buildPrimaryButton(onConfirm)

        primaryButton.onClick.click()

        assertThat(confirmed).isTrue()
    }

    private fun assertModel(
        uiModel: List<BloggingRemindersItem>
    ) {
        assertThat(uiModel[0]).isEqualTo(Illustration(R.drawable.img_illustration_celebration_150dp))
        assertThat(uiModel[1]).isEqualTo(Title(UiStringRes(R.string.set_your_blogging_goals_title)))
        assertThat(uiModel[2]).isEqualTo(HighEmphasisText(UiStringRes(R.string.set_your_blogging_goals_message)))
    }
}
