package org.sitebay.android.ui.mysite

import android.view.View
import android.view.ViewGroup
import org.sitebay.android.databinding.QuickActionsBlockBinding
import org.sitebay.android.ui.mysite.MySiteItem.QuickActionsBlock
import org.sitebay.android.util.viewBinding

class QuickActionsViewHolder(
    parent: ViewGroup
) : MySiteItemViewHolder<QuickActionsBlockBinding>(parent.viewBinding(QuickActionsBlockBinding::inflate)) {
    fun bind(item: QuickActionsBlock) = with(binding) {
        quickActionStatsButton.setOnClickListener { item.onStatsClick.click() }
        quickActionPostsButton.setOnClickListener { item.onPostsClick.click() }
        quickActionMediaButton.setOnClickListener { item.onMediaClick.click() }
        quickActionPagesButton.setOnClickListener { item.onPagesClick.click() }

        val pagesVisibility = if (item.showPages) View.VISIBLE else View.GONE
        quickActionPagesButton.visibility = pagesVisibility
        quickActionPagesLabel.visibility = pagesVisibility

        quickStartStatsFocusPoint.setVisibleOrGone(item.showStatsFocusPoint)
        quickStartPagesFocusPoint.setVisibleOrGone(item.showPagesFocusPoint)
    }
}
