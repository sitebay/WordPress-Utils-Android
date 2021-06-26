package org.sitebay.android.ui.jetpack.scan.adapters.viewholders

import android.view.ViewGroup
import org.sitebay.android.databinding.ScanListThreatItemLoadingSkeletonBinding
import org.sitebay.android.ui.jetpack.common.JetpackListItemState
import org.sitebay.android.ui.jetpack.common.viewholders.JetpackViewHolder

class ThreatLoadingSkeletonViewHolder(
    parent: ViewGroup
) : JetpackViewHolder<ScanListThreatItemLoadingSkeletonBinding>(
        parent,
        ScanListThreatItemLoadingSkeletonBinding::inflate
) {
    override fun onBind(itemUiState: JetpackListItemState) {}
}
