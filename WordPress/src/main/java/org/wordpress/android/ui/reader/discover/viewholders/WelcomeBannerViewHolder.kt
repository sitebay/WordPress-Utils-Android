package org.sitebay.android.ui.reader.discover.viewholders

import android.view.ViewGroup
import org.sitebay.android.databinding.ReaderCardviewWelcomeBannerBinding
import org.sitebay.android.ui.reader.discover.ReaderCardUiState
import org.sitebay.android.ui.reader.discover.ReaderCardUiState.ReaderWelcomeBannerCardUiState
import org.sitebay.android.util.viewBinding

class WelcomeBannerViewHolder(
    parentView: ViewGroup
) : ReaderViewHolder<ReaderCardviewWelcomeBannerBinding>(
        parentView.viewBinding(ReaderCardviewWelcomeBannerBinding::inflate)
) {
    override fun onBind(uiState: ReaderCardUiState) = with(binding) {
        val state = uiState as ReaderWelcomeBannerCardUiState
        welcomeTitle.setText(state.titleRes)
    }
}
