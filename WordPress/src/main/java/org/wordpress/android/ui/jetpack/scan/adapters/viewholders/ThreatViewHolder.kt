package org.sitebay.android.ui.jetpack.scan.adapters.viewholders

import android.view.ViewGroup
import org.sitebay.android.databinding.ScanListThreatItemBinding
import org.sitebay.android.ui.jetpack.common.JetpackListItemState
import org.sitebay.android.ui.jetpack.common.viewholders.JetpackViewHolder
import org.sitebay.android.ui.jetpack.scan.ScanListItemState.ThreatItemState
import org.sitebay.android.ui.utils.UiHelpers
import org.sitebay.android.util.getColorFromAttribute
import org.sitebay.android.util.setVisible

class ThreatViewHolder(
    private val uiHelpers: UiHelpers,
    parent: ViewGroup
) : JetpackViewHolder<ScanListThreatItemBinding>(
        parent,
        ScanListThreatItemBinding::inflate
) {
    override fun onBind(itemUiState: JetpackListItemState) = with(binding) {
        val threatItemState = itemUiState as ThreatItemState
        with(threatItemState) {
            with(uiHelpers) {
                setTextOrHide(threatHeader, header)
                setTextOrHide(threatSubHeader, subHeader)
            }
            threatSubHeader.setTextColor(threatSubHeader.context.getColorFromAttribute(subHeaderColor))
            threatIcon.setImageResource(icon)
            threatIcon.setBackgroundResource(iconBackground)
            threatIcon.setVisible(isIconVisible)
            loading.setVisible(isLoadingVisible)
            root.setOnClickListener { onClick.invoke() }
        }
    }
}
