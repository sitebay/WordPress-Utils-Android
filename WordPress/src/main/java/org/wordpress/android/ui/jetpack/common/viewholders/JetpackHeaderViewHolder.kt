package org.sitebay.android.ui.jetpack.common.viewholders

import android.view.ViewGroup
import androidx.core.content.ContextCompat
import org.sitebay.android.databinding.JetpackListHeaderItemBinding
import org.sitebay.android.ui.jetpack.common.JetpackListItemState
import org.sitebay.android.ui.jetpack.common.JetpackListItemState.HeaderState
import org.sitebay.android.ui.utils.UiHelpers
import org.sitebay.android.util.getColorResIdFromAttribute

class JetpackHeaderViewHolder(
    private val uiHelpers: UiHelpers,
    parent: ViewGroup
) : JetpackViewHolder<JetpackListHeaderItemBinding>(
        parent,
        JetpackListHeaderItemBinding::inflate
) {
    override fun onBind(itemUiState: JetpackListItemState) {
        val headerState = itemUiState as HeaderState
        val context = itemView.context

        binding.header.text = uiHelpers.getTextOfUiString(context, headerState.text)
        val textColorRes = context.getColorResIdFromAttribute(headerState.textColorRes)
        binding.header.setTextColor(ContextCompat.getColor(context, textColorRes))
    }
}
