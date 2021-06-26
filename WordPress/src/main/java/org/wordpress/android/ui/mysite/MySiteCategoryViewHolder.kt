package org.sitebay.android.ui.mysite

import android.view.ViewGroup
import org.sitebay.android.databinding.MySiteCategoryHeaderBlockBinding
import org.sitebay.android.ui.mysite.MySiteItem.CategoryHeader
import org.sitebay.android.ui.utils.UiHelpers
import org.sitebay.android.util.viewBinding

class MySiteCategoryViewHolder(
    parent: ViewGroup,
    private val uiHelpers: UiHelpers
) : MySiteItemViewHolder<MySiteCategoryHeaderBlockBinding>(
        parent.viewBinding(MySiteCategoryHeaderBlockBinding::inflate)
) {
    fun bind(item: CategoryHeader) = with(binding) {
        uiHelpers.setTextOrHide(category, item.title)
    }
}
