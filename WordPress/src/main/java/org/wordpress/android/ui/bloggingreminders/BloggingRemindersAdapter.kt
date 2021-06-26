package org.sitebay.android.ui.bloggingreminders

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView.Adapter
import org.sitebay.android.ui.bloggingreminders.BloggingRemindersItem.Caption
import org.sitebay.android.ui.bloggingreminders.BloggingRemindersDiffCallback.DayButtonsPayload
import org.sitebay.android.ui.bloggingreminders.BloggingRemindersItem.DayButtons
import org.sitebay.android.ui.bloggingreminders.BloggingRemindersItem.HighEmphasisText
import org.sitebay.android.ui.bloggingreminders.BloggingRemindersItem.Illustration
import org.sitebay.android.ui.bloggingreminders.BloggingRemindersItem.MediumEmphasisText
import org.sitebay.android.ui.bloggingreminders.BloggingRemindersItem.Tip
import org.sitebay.android.ui.bloggingreminders.BloggingRemindersItem.Title
import org.sitebay.android.ui.bloggingreminders.BloggingRemindersItem.Type
import org.sitebay.android.ui.bloggingreminders.BloggingRemindersItem.Type.CAPTION
import org.sitebay.android.ui.bloggingreminders.BloggingRemindersItem.Type.DAY_BUTTONS
import org.sitebay.android.ui.bloggingreminders.BloggingRemindersItem.Type.HIGH_EMPHASIS_TEXT
import org.sitebay.android.ui.bloggingreminders.BloggingRemindersItem.Type.ILLUSTRATION
import org.sitebay.android.ui.bloggingreminders.BloggingRemindersItem.Type.LOW_EMPHASIS_TEXT
import org.sitebay.android.ui.bloggingreminders.BloggingRemindersItem.Type.TIP
import org.sitebay.android.ui.bloggingreminders.BloggingRemindersItem.Type.TITLE
import org.sitebay.android.ui.bloggingreminders.BloggingRemindersViewHolder.CaptionViewHolder
import org.sitebay.android.ui.bloggingreminders.BloggingRemindersViewHolder.DayButtonsViewHolder
import org.sitebay.android.ui.bloggingreminders.BloggingRemindersViewHolder.HighEmphasisTextViewHolder
import org.sitebay.android.ui.bloggingreminders.BloggingRemindersViewHolder.IllustrationViewHolder
import org.sitebay.android.ui.bloggingreminders.BloggingRemindersViewHolder.MediumEmphasisTextViewHolder
import org.sitebay.android.ui.bloggingreminders.BloggingRemindersViewHolder.TipViewHolder
import org.sitebay.android.ui.bloggingreminders.BloggingRemindersViewHolder.TitleViewHolder
import org.sitebay.android.ui.utils.UiHelpers
import javax.inject.Inject

class BloggingRemindersAdapter
@Inject constructor(private val uiHelpers: UiHelpers) :
    Adapter<BloggingRemindersViewHolder<*>>() {
    private var items: List<BloggingRemindersItem> = listOf()

    fun update(newItems: List<BloggingRemindersItem>) {
        val diffResult = DiffUtil.calculateDiff(
            BloggingRemindersDiffCallback(
                items,
                newItems
            )
        )
        items = newItems
        diffResult.dispatchUpdatesTo(this)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: BloggingRemindersViewHolder<*>, position: Int) {
        onBindViewHolder(holder, position, listOf())
    }

    override fun onBindViewHolder(holder: BloggingRemindersViewHolder<*>, position: Int, payloads: List<Any>) {
        val item = items[position]
        when (holder) {
            is IllustrationViewHolder -> holder.onBind(item as Illustration)
            is TitleViewHolder -> holder.onBind(item as Title)
            is HighEmphasisTextViewHolder -> holder.onBind(item as HighEmphasisText)
            is MediumEmphasisTextViewHolder -> holder.onBind(item as MediumEmphasisText)
            is CaptionViewHolder -> holder.onBind(item as Caption)
            is DayButtonsViewHolder -> holder.onBind(item as DayButtons, payloads.firstOrNull() as? DayButtonsPayload)
            is TipViewHolder -> holder.onBind(item as Tip)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BloggingRemindersViewHolder<*> {
        return when (Type.values()[viewType]) {
            TITLE -> TitleViewHolder(parent, uiHelpers)
            ILLUSTRATION -> IllustrationViewHolder(parent)
            HIGH_EMPHASIS_TEXT -> HighEmphasisTextViewHolder(parent, uiHelpers)
            LOW_EMPHASIS_TEXT -> MediumEmphasisTextViewHolder(parent, uiHelpers)
            CAPTION -> CaptionViewHolder(parent, uiHelpers)
            DAY_BUTTONS -> DayButtonsViewHolder(parent, uiHelpers)
            TIP -> TipViewHolder(parent, uiHelpers)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return items[position].type.ordinal
    }
}
