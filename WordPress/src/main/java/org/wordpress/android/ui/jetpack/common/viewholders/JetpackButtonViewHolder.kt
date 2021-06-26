package org.sitebay.android.ui.jetpack.common.viewholders

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewbinding.ViewBinding
import com.google.android.material.button.MaterialButton
import org.sitebay.android.R
import org.sitebay.android.databinding.JetpackListButtonPrimaryItemBinding
import org.sitebay.android.databinding.JetpackListButtonSecondaryItemBinding
import org.sitebay.android.ui.jetpack.common.JetpackListItemState
import org.sitebay.android.ui.jetpack.common.JetpackListItemState.ActionButtonState
import org.sitebay.android.ui.utils.UiHelpers
import org.sitebay.android.util.setVisible
import org.sitebay.android.widgets.FlowLayout.LayoutParams

sealed class JetpackButtonViewHolder<T : ViewBinding>(
    parent: ViewGroup,
    inflateBinding: (LayoutInflater, ViewGroup, Boolean) -> T
) : JetpackViewHolder<T>(parent, inflateBinding) {
    class Primary(
        private val uiHelpers: UiHelpers,
        parent: ViewGroup
    ) : JetpackButtonViewHolder<JetpackListButtonPrimaryItemBinding>(
            parent,
            JetpackListButtonPrimaryItemBinding::inflate
    ) {
        override fun onBind(itemUiState: JetpackListItemState) {
            binding.button.updateState(binding.root, itemUiState as ActionButtonState, uiHelpers)
        }
    }

    class Secondary(
        private val uiHelpers: UiHelpers,
        parent: ViewGroup
    ) : JetpackButtonViewHolder<JetpackListButtonSecondaryItemBinding>(
            parent,
            JetpackListButtonSecondaryItemBinding::inflate
    ) {
        override fun onBind(itemUiState: JetpackListItemState) {
            binding.button.updateState(binding.root, itemUiState as ActionButtonState, uiHelpers)
        }
    }

    internal fun MaterialButton.updateState(root: View, buttonState: ActionButtonState, uiHelpers: UiHelpers) {
        updateItemViewVisibility(root, buttonState.isVisible)
        uiHelpers.setTextOrHide(this, buttonState.text)
        isEnabled = buttonState.isEnabled
        setOnClickListener { buttonState.onClick.invoke() }

        buttonState.iconRes?.let {
            iconGravity = MaterialButton.ICON_GRAVITY_TEXT_START
            icon = context.getDrawable(it)
            val resources = itemView.context.resources
            iconSize = resources.getDimensionPixelSize(R.dimen.jetpack_button_icon_size)
        }
    }

    private fun updateItemViewVisibility(root: View, isVisible: Boolean) {
        with(root) {
            setVisible(isVisible)
            layoutParams = if (isVisible) {
                ConstraintLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            } else {
                ConstraintLayout.LayoutParams(0, 0)
            }
        }
    }
}
