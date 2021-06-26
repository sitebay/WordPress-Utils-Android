package org.sitebay.android.ui.reader.subfilter.viewholders

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import org.sitebay.android.R
import org.sitebay.android.ui.reader.subfilter.SubfilterListItem
import org.sitebay.android.ui.utils.UiHelpers
import org.sitebay.android.util.getColorFromAttribute

open class SubfilterListItemViewHolder(
    internal val parent: ViewGroup,
    @LayoutRes layout: Int
) : ViewHolder(LayoutInflater.from(parent.context).inflate(layout, parent, false)) {
    open fun bind(filter: SubfilterListItem, uiHelpers: UiHelpers) {
        val itemText: TextView? = this.itemView.findViewById(R.id.item_title)
        itemText?.let {
            it.setTextColor(
                    parent.context.getColorFromAttribute(
                            if (filter.isSelected) {
                                R.attr.colorPrimary
                            } else {
                                R.attr.colorOnSurface
                            }
                    )
            )
        }

        this.itemView.setOnClickListener {
            filter.onClickAction?.invoke(filter)
        }
    }
}
