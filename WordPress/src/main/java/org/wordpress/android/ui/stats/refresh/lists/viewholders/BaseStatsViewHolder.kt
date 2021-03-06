package org.sitebay.android.ui.stats.refresh.lists.viewholders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.recyclerview.widget.StaggeredGridLayoutManager.LayoutParams
import org.sitebay.android.fluxc.store.StatsStore.InsightType.LATEST_POST_SUMMARY
import org.sitebay.android.fluxc.store.StatsStore.ManagementType
import org.sitebay.android.fluxc.store.StatsStore.StatsType
import org.sitebay.android.fluxc.store.StatsStore.TimeStatsType.OVERVIEW
import org.sitebay.android.ui.stats.refresh.lists.sections.BlockListItem

abstract class BaseStatsViewHolder(
    parent: ViewGroup,
    @LayoutRes layout: Int
) : ViewHolder(LayoutInflater.from(parent.context).inflate(layout, parent, false)) {
    @CallSuper
    open fun bind(statsType: StatsType?, items: List<BlockListItem>) {
        when (statsType) {
            OVERVIEW, LATEST_POST_SUMMARY -> {
                setFullWidth()
            }
            ManagementType.CONTROL -> {
                setFullWidth()
            }
        }
    }

    private fun setFullWidth() {
        val layoutParams = itemView.layoutParams as? LayoutParams
        layoutParams?.isFullSpan = true
    }
}
