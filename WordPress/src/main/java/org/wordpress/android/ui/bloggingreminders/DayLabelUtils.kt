package org.sitebay.android.ui.bloggingreminders

import org.sitebay.android.R
import org.sitebay.android.fluxc.model.BloggingRemindersModel
import org.sitebay.android.ui.utils.UiString
import org.sitebay.android.ui.utils.UiString.UiStringRes
import org.sitebay.android.ui.utils.UiString.UiStringResWithParams
import org.sitebay.android.ui.utils.UiString.UiStringText
import org.sitebay.android.viewmodel.ResourceProvider
import javax.inject.Inject

class DayLabelUtils
@Inject constructor(private val resourceProvider: ResourceProvider) {
    fun buildNTimesLabel(bloggingRemindersModel: BloggingRemindersModel?): UiString {
        val counts = resourceProvider.getStringArray(R.array.blogging_goals_count)
        val size = bloggingRemindersModel?.enabledDays?.size ?: 0
        return if (size > 0) {
            UiStringResWithParams(
                    R.string.blogging_goals_n_a_week,
                    listOf(UiStringText(counts[size - 1]))
            )
        } else {
            UiStringRes(R.string.blogging_goals_not_set)
        }
    }
}
