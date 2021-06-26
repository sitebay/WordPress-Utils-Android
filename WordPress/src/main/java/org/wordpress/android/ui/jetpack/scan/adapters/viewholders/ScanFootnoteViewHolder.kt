package org.sitebay.android.ui.jetpack.scan.adapters.viewholders

import android.text.method.LinkMovementMethod
import android.view.View
import android.view.ViewGroup

import org.sitebay.android.databinding.ScanListFootnoteItemBinding

import org.sitebay.android.ui.jetpack.common.JetpackListItemState
import org.sitebay.android.ui.jetpack.common.viewholders.JetpackViewHolder
import org.sitebay.android.ui.jetpack.scan.ScanListItemState.FootnoteState
import org.sitebay.android.ui.utils.UiHelpers
import org.sitebay.android.util.ColorUtils
import org.sitebay.android.util.image.ImageManager

class ScanFootnoteViewHolder(
    private val imageManager: ImageManager,
    private val uiHelpers: UiHelpers,
    parent: ViewGroup
) : JetpackViewHolder<ScanListFootnoteItemBinding>(
        parent,
        ScanListFootnoteItemBinding::inflate
) {
    init {
        with(binding.footnote) {
            linksClickable = true
            isClickable = true
            movementMethod = LinkMovementMethod.getInstance()
        }
    }

    override fun onBind(itemUiState: JetpackListItemState) {
        with(binding) {
            val state = itemUiState as FootnoteState

            footnote.text = uiHelpers.getTextOfUiString(itemView.context, state.text)
            footnote.visibility = if (state.isVisible) View.VISIBLE else View.GONE

            state.iconResId?.let {
                icon.visibility = if (state.isVisible) View.VISIBLE else View.GONE

                if (state.iconColorResId == null) {
                    imageManager.load(icon, it)
                } else {
                    ColorUtils.setImageResourceWithTint(
                            icon,
                            it,
                            state.iconColorResId
                    )
                }

                state.onIconClick?.let {
                    icon.isClickable = true
                    icon.setOnClickListener { state.onIconClick.invoke() }
                }
            }
        }
    }
}
