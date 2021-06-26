package org.sitebay.android.ui.reader.discover.viewholders

import android.view.ViewGroup
import org.sitebay.android.R
import org.sitebay.android.databinding.ReaderRecommendedBlogsCardBinding
import org.sitebay.android.ui.reader.discover.ReaderCardUiState
import org.sitebay.android.ui.reader.discover.ReaderCardUiState.ReaderRecommendedBlogsCardUiState
import org.sitebay.android.ui.reader.discover.ReaderRecommendedBlogsAdapter
import org.sitebay.android.ui.utils.UiHelpers
import org.sitebay.android.ui.utils.addItemDivider
import org.sitebay.android.util.AppLog
import org.sitebay.android.util.image.ImageManager
import org.sitebay.android.util.viewBinding

class ReaderRecommendedBlogsCardViewHolder(
    parentView: ViewGroup,
    imageManager: ImageManager,
    uiHelpers: UiHelpers
) : ReaderViewHolder<ReaderRecommendedBlogsCardBinding>(
        parentView.viewBinding(ReaderRecommendedBlogsCardBinding::inflate)
) {
    private val recommendedBlogsAdapter = ReaderRecommendedBlogsAdapter(imageManager, uiHelpers)

    init {
        with(binding) {
            recommendedBlogs.adapter = recommendedBlogsAdapter
            parentView.context.getDrawable(R.drawable.default_list_divider)?.let {
                recommendedBlogs.addItemDivider(it)
            } ?: AppLog.w(AppLog.T.READER, "Discover list divider null")
        }
    }

    override fun onBind(uiState: ReaderCardUiState) {
        uiState as ReaderRecommendedBlogsCardUiState
        recommendedBlogsAdapter.submitList(uiState.blogs)
    }
}
