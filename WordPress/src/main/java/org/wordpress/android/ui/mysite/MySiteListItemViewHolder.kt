package org.sitebay.android.ui.mysite

import android.view.ViewGroup
import org.sitebay.android.databinding.MySiteItemBlockBinding
import org.sitebay.android.ui.utils.UiHelpers
import org.sitebay.android.util.viewBinding

class MySiteListItemViewHolder(
    parent: ViewGroup,
    private val uiHelpers: UiHelpers
) : MySiteItemViewHolder<MySiteItemBlockBinding>(parent.viewBinding(MySiteItemBlockBinding::inflate)) {
    fun bind(item: MySiteItem.ListItem) = with(binding) {
        uiHelpers.setImageOrHide(mySiteItemPrimaryIcon, item.primaryIcon)
        uiHelpers.setImageOrHide(mySiteItemSecondaryIcon, item.secondaryIcon)
        uiHelpers.setTextOrHide(mySiteItemPrimaryText, item.primaryText)
        uiHelpers.setTextOrHide(mySiteItemSecondaryText, item.secondaryText)
        itemView.setOnClickListener { item.onClick.click() }
        mySiteItemQuickStartFocusPoint.setVisibleOrGone(item.showFocusPoint)
    }
}
