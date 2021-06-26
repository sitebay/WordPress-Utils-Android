package org.sitebay.android.ui.jetpack.scan.adapters.viewholders

import android.view.ViewGroup
import org.sitebay.android.databinding.ScanListThreatsDateItemBinding
import org.sitebay.android.ui.jetpack.common.JetpackListItemState
import org.sitebay.android.ui.jetpack.common.viewholders.JetpackViewHolder
import org.sitebay.android.ui.jetpack.scan.ScanListItemState.ThreatDateItemState
import org.sitebay.android.ui.utils.UiHelpers

class ThreatsDateHeaderViewHolder(
    private val uiHelpers: UiHelpers,
    parent: ViewGroup
) : JetpackViewHolder<ScanListThreatsDateItemBinding>(
        parent,
        ScanListThreatsDateItemBinding::inflate
) {
    override fun onBind(itemUiState: JetpackListItemState) {
        val headerItemState = itemUiState as ThreatDateItemState
        binding.dateText.text = uiHelpers.getTextOfUiString(itemView.context, headerItemState.text)
    }
}
