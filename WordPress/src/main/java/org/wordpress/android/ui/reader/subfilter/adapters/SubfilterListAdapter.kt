package org.sitebay.android.ui.reader.subfilter.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView.Adapter
import org.sitebay.android.ui.reader.subfilter.SubfilterListItem
import org.sitebay.android.ui.reader.subfilter.SubfilterListItem.ItemType
import org.sitebay.android.ui.reader.subfilter.SubfilterListItem.ItemType.DIVIDER
import org.sitebay.android.ui.reader.subfilter.SubfilterListItem.ItemType.SECTION_TITLE
import org.sitebay.android.ui.reader.subfilter.SubfilterListItem.ItemType.SITE
import org.sitebay.android.ui.reader.subfilter.SubfilterListItem.ItemType.SITE_ALL
import org.sitebay.android.ui.reader.subfilter.SubfilterListItem.ItemType.TAG
import org.sitebay.android.ui.reader.subfilter.SubfilterListItem.SectionTitle
import org.sitebay.android.ui.reader.subfilter.SubfilterListItem.Site
import org.sitebay.android.ui.reader.subfilter.SubfilterListItem.SiteAll
import org.sitebay.android.ui.reader.subfilter.SubfilterListItem.Tag
import org.sitebay.android.ui.reader.subfilter.viewholders.DividerViewHolder
import org.sitebay.android.ui.reader.subfilter.viewholders.SectionTitleViewHolder
import org.sitebay.android.ui.reader.subfilter.viewholders.SiteAllViewHolder
import org.sitebay.android.ui.reader.subfilter.viewholders.SiteViewHolder
import org.sitebay.android.ui.reader.subfilter.viewholders.SubFilterDiffCallback
import org.sitebay.android.ui.reader.subfilter.viewholders.SubfilterListItemViewHolder
import org.sitebay.android.ui.reader.subfilter.viewholders.TagViewHolder
import org.sitebay.android.ui.stats.refresh.utils.StatsUtils
import org.sitebay.android.ui.utils.UiHelpers
import org.sitebay.android.util.config.SeenUnseenWithCounterFeatureConfig

class SubfilterListAdapter(
    val uiHelpers: UiHelpers,
    val statsUtils: StatsUtils,
    val seenUnseenWithCounterFeatureConfig: SeenUnseenWithCounterFeatureConfig
) : Adapter<SubfilterListItemViewHolder>() {
    private var items: List<SubfilterListItem> = listOf()

    fun update(newItems: List<SubfilterListItem>) {
        val diffResult = DiffUtil.calculateDiff(
                SubFilterDiffCallback(
                        items,
                        newItems
                )
        )
        items = newItems
        diffResult.dispatchUpdatesTo(this)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: SubfilterListItemViewHolder, position: Int) {
        val item = items[position]
        when (holder) {
            is SectionTitleViewHolder -> holder.bind(item as SectionTitle, uiHelpers)
            is SiteViewHolder -> holder.bind(
                    item as Site,
                    uiHelpers,
                    statsUtils,
                    seenUnseenWithCounterFeatureConfig.isEnabled()
            )
            is SiteAllViewHolder -> holder.bind(item as SiteAll, uiHelpers)
            is TagViewHolder -> holder.bind(item as Tag, uiHelpers)
            is DividerViewHolder -> {}
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubfilterListItemViewHolder {
        return when (ItemType.values()[viewType]) {
            SECTION_TITLE -> SectionTitleViewHolder(parent)
            SITE -> SiteViewHolder(parent)
            SITE_ALL -> SiteAllViewHolder(parent)
            DIVIDER -> DividerViewHolder(parent)
            TAG -> TagViewHolder(parent)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return items[position].type.ordinal
    }
}
