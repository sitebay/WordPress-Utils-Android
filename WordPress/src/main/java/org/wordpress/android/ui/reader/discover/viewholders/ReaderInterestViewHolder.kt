package org.sitebay.android.ui.reader.discover.viewholders

import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources.getColorStateList
import androidx.recyclerview.widget.RecyclerView
import org.sitebay.android.databinding.ReaderInterestItemBinding
import org.sitebay.android.ui.reader.discover.ReaderCardUiState.ReaderInterestsCardUiState.ReaderInterestUiState
import org.sitebay.android.ui.utils.UiHelpers
import org.sitebay.android.util.viewBinding

class ReaderInterestViewHolder(
    private val uiHelpers: UiHelpers,
    parent: ViewGroup,
    private val binding: ReaderInterestItemBinding = parent.viewBinding(ReaderInterestItemBinding::inflate)
) : RecyclerView.ViewHolder(binding.root) {
    fun onBind(uiState: ReaderInterestUiState) = with(binding) {
        uiHelpers.setTextOrHide(interest, uiState.interest)
        interest.setOnClickListener { uiState.onClicked.invoke(uiState.interest) }

        with(uiState.chipStyle) {
            interest.setChipStrokeColorResource(chipStrokeColorResId)
            interest.setChipBackgroundColorResource(chipFillColorResId)
            interest.setTextColor(getColorStateList(interest.context, chipFontColorResId))
        }
    }
}
