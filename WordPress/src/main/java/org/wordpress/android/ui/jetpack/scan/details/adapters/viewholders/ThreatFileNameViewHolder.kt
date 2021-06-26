package org.sitebay.android.ui.jetpack.scan.details.adapters.viewholders

import android.view.ViewGroup
import org.sitebay.android.databinding.ThreatDetailsListFileNameItemBinding
import org.sitebay.android.ui.jetpack.common.JetpackListItemState
import org.sitebay.android.ui.jetpack.common.viewholders.JetpackViewHolder
import org.sitebay.android.ui.jetpack.scan.details.ThreatDetailsListItemState.ThreatFileNameState
import org.sitebay.android.ui.utils.UiHelpers

class ThreatFileNameViewHolder(
    private val uiHelpers: UiHelpers,
    parent: ViewGroup
) : JetpackViewHolder<ThreatDetailsListFileNameItemBinding>(
        parent,
        ThreatDetailsListFileNameItemBinding::inflate
) {
    override fun onBind(itemUiState: JetpackListItemState) {
        val threatFileNameState = itemUiState as ThreatFileNameState
        binding.fileName.text = uiHelpers.getTextOfUiString(itemView.context, threatFileNameState.fileName)
    }
}
