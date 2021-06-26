package org.sitebay.android.ui.stats.refresh.lists.sections.viewholders

import android.view.ViewGroup
import org.sitebay.android.R
import org.sitebay.android.ui.stats.refresh.lists.sections.BlockListItem.LoadingItem

class LoadingItemViewHolder(parent: ViewGroup) : BlockListItemViewHolder(
        parent,
        R.layout.stats_block_loading_item
) {
    fun bind(loadingItem: LoadingItem) {
        if (!loadingItem.isLoading) {
            loadingItem.loadMore()
        }
    }
}
