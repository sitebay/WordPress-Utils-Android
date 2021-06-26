package org.sitebay.android.ui.bloggingreminders

import org.sitebay.android.R
import org.sitebay.android.ui.bloggingreminders.BloggingRemindersItem.HighEmphasisText
import org.sitebay.android.ui.bloggingreminders.BloggingRemindersItem.Illustration
import org.sitebay.android.ui.bloggingreminders.BloggingRemindersItem.Title
import org.sitebay.android.ui.bloggingreminders.BloggingRemindersViewModel.UiState.PrimaryButton
import org.sitebay.android.ui.utils.ListItemInteraction
import org.sitebay.android.ui.utils.UiString.UiStringRes
import javax.inject.Inject

class PrologueBuilder
@Inject constructor() {
    fun buildUiItems(): List<BloggingRemindersItem> {
        return listOf(Illustration(R.drawable.img_illustration_celebration_150dp),
                Title(UiStringRes(R.string.set_your_blogging_goals_title)),
                HighEmphasisText(UiStringRes(R.string.set_your_blogging_goals_message))
        )
    }

    fun buildPrimaryButton(
        onContinue: () -> Unit
    ): PrimaryButton {
        return PrimaryButton(
                UiStringRes(R.string.set_your_blogging_goals_button),
                enabled = true,
                ListItemInteraction.create(onContinue)
        )
    }
}
