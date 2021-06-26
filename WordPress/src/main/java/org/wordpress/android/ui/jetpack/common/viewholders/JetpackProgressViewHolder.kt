package org.sitebay.android.ui.jetpack.common.viewholders

import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
import org.sitebay.android.databinding.JetpackListProgressItemBinding
import org.sitebay.android.ui.jetpack.common.JetpackListItemState
import org.sitebay.android.ui.jetpack.common.JetpackListItemState.ProgressState
import org.sitebay.android.ui.utils.UiHelpers
import org.sitebay.android.util.setVisible

class JetpackProgressViewHolder(
    private val uiHelpers: UiHelpers,
    parent: ViewGroup
) : JetpackViewHolder<JetpackListProgressItemBinding>(
        parent,
        JetpackListProgressItemBinding::inflate
) {
    override fun onBind(itemUiState: JetpackListItemState) = with(binding) {
        val state = itemUiState as ProgressState
        updateItemViewVisibility(state)
        updateProgressBar(state)
        updateProgressStateLabel(state)
        uiHelpers.setTextOrHide(progressLabel, state.progressLabel)
        uiHelpers.setTextOrHide(progressInfoLabel, state.progressInfoLabel)
    }

    private fun updateItemViewVisibility(state: ProgressState) {
        with(binding.root) {
            setVisible(state.isVisible)
            layoutParams = if (state.isVisible) {
                LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            } else {
                LayoutParams(0, 0)
            }
        }
    }

    private fun updateProgressStateLabel(state: ProgressState) = with(binding) {
        uiHelpers.setTextOrHide(progressStateLabel, state.progressStateLabel)
        progressStateLabel.textAlignment = state.progressStateLabelTextAlignment
    }

    private fun updateProgressBar(state: ProgressState) = with(binding) {
        progressBar.isIndeterminate = state.isIndeterminate
        progressBar.progress = state.progress
    }
}
