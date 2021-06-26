package org.sitebay.android.ui.bloggingreminders

import org.sitebay.android.R
import org.sitebay.android.fluxc.model.BloggingRemindersModel
import org.sitebay.android.fluxc.model.BloggingRemindersModel.Day
import org.sitebay.android.ui.bloggingreminders.BloggingRemindersItem.DayButtons
import org.sitebay.android.ui.bloggingreminders.BloggingRemindersItem.DayButtons.DayItem
import org.sitebay.android.ui.bloggingreminders.BloggingRemindersItem.EmphasizedText
import org.sitebay.android.ui.bloggingreminders.BloggingRemindersItem.Illustration
import org.sitebay.android.ui.bloggingreminders.BloggingRemindersItem.MediumEmphasisText
import org.sitebay.android.ui.bloggingreminders.BloggingRemindersItem.Tip
import org.sitebay.android.ui.bloggingreminders.BloggingRemindersItem.Title
import org.sitebay.android.ui.bloggingreminders.BloggingRemindersViewModel.UiState.PrimaryButton
import org.sitebay.android.ui.utils.ListItemInteraction
import org.sitebay.android.ui.utils.UiString.UiStringRes
import org.sitebay.android.ui.utils.UiString.UiStringText
import javax.inject.Inject

class DaySelectionBuilder
@Inject constructor(private val daysProvider: DaysProvider, private val dayLabelUtils: DayLabelUtils) {
    fun buildSelection(
        bloggingRemindersModel: BloggingRemindersModel?,
        onSelectDay: (Day) -> Unit
    ): List<BloggingRemindersItem> {
        val daysOfWeek = daysProvider.getDays()
        val text = dayLabelUtils.buildNTimesLabel(bloggingRemindersModel)
        val nTimesLabel = MediumEmphasisText(
                EmphasizedText(text),
                bloggingRemindersModel?.enabledDays?.isEmpty() == true
        )
        return listOf(
                Illustration(R.drawable.img_illustration_calendar),
                Title(UiStringRes(R.string.blogging_reminders_select_days)),
                MediumEmphasisText(UiStringRes(R.string.blogging_reminders_select_days_message)),
                DayButtons(daysOfWeek.map {
                    DayItem(
                            UiStringText(it.first),
                            bloggingRemindersModel?.enabledDays?.contains(it.second) == true,
                            ListItemInteraction.create(it.second, onSelectDay)
                    )
                }),
                nTimesLabel,
                Tip(UiStringRes(R.string.blogging_reminders_tip), UiStringRes(R.string.blogging_reminders_tip_message))
        )
    }

    fun buildPrimaryButton(
        bloggingRemindersModel: BloggingRemindersModel?,
        isFirstTimeFlow: Boolean,
        onConfirm: (BloggingRemindersModel?) -> Unit
    ): PrimaryButton {
        val buttonEnabled = if (isFirstTimeFlow) {
            bloggingRemindersModel?.enabledDays?.isNotEmpty() == true
        } else {
            true
        }
        val buttonText = if (isFirstTimeFlow) {
            R.string.blogging_reminders_notify_me
        } else {
            R.string.blogging_reminders_update
        }
        return PrimaryButton(
                UiStringRes(buttonText),
                enabled = buttonEnabled,
                ListItemInteraction.create(bloggingRemindersModel, onConfirm)
        )
    }
}
