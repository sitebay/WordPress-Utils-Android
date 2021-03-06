package org.sitebay.android.ui.reader.subfilter.viewholders

import android.view.ViewGroup
import android.widget.TextView
import org.sitebay.android.R
import org.sitebay.android.ui.reader.subfilter.SubfilterListItem.SiteAll
import org.sitebay.android.ui.utils.UiHelpers

class SiteAllViewHolder(
    parent: ViewGroup
) : SubfilterListItemViewHolder(parent, R.layout.subfilter_list_item) {
    private val itemTitle = itemView.findViewById<TextView>(R.id.item_title)

    fun bind(site: SiteAll, uiHelpers: UiHelpers) {
        super.bind(site, uiHelpers)
        this.itemTitle.text = uiHelpers.getTextOfUiString(parent.context, site.label)
    }
}
