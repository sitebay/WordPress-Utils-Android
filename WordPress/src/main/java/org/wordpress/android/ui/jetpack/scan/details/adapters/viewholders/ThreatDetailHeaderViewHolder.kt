package org.sitebay.android.ui.jetpack.scan.details.adapters.viewholders

import android.view.ViewGroup
import org.sitebay.android.databinding.ThreatDetailsListHeaderBinding
import org.sitebay.android.ui.jetpack.common.JetpackListItemState
import org.sitebay.android.ui.jetpack.common.viewholders.JetpackViewHolder
import org.sitebay.android.ui.jetpack.scan.details.ThreatDetailsListItemState.ThreatDetailHeaderState
import org.sitebay.android.ui.utils.UiHelpers

class ThreatDetailHeaderViewHolder(
    private val uiHelpers: UiHelpers,
    parent: ViewGroup
) : JetpackViewHolder<ThreatDetailsListHeaderBinding>(
        parent,
        ThreatDetailsListHeaderBinding::inflate
) {
    override fun onBind(itemUiState: JetpackListItemState) = with(binding) {
        val state = itemUiState as ThreatDetailHeaderState
        with(uiHelpers) {
            setTextOrHide(header, state.header)
            setTextOrHide(description, state.description)
        }
        icon.setImageResource(state.icon)
        icon.setBackgroundResource(itemUiState.iconBackground)
    }
}
