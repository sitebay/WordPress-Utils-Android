package org.sitebay.android.ui.reader.discover.viewholders

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.sitebay.android.databinding.ReaderRecommendedBlogItemBinding
import org.sitebay.android.ui.reader.discover.ReaderCardUiState.ReaderRecommendedBlogsCardUiState.ReaderRecommendedBlogUiState
import org.sitebay.android.ui.utils.UiHelpers
import org.sitebay.android.util.image.ImageManager
import org.sitebay.android.util.image.ImageType.BLAVATAR_CIRCULAR
import org.sitebay.android.util.viewBinding

class ReaderRecommendedBlogViewHolder(
    parent: ViewGroup,
    private val imageManager: ImageManager,
    private val uiHelpers: UiHelpers,
    private val binding: ReaderRecommendedBlogItemBinding =
            parent.viewBinding(ReaderRecommendedBlogItemBinding::inflate)
) : RecyclerView.ViewHolder(binding.root) {
    fun onBind(uiState: ReaderRecommendedBlogUiState) = with(binding) {
        with(uiState) {
            siteName.text = name
            siteUrl.text = url
            uiHelpers.setTextOrHide(siteDescription, description)
            siteFollowIcon.apply {
                setIsFollowed(isFollowed)
                contentDescription = context.getString(followContentDescription.stringRes)
                setOnClickListener {
                    onFollowClicked(uiState)
                }
            }
            updateBlogImage(iconUrl)
            root.setOnClickListener {
                onItemClicked(blogId, feedId, isFollowed)
            }
        }
    }

    private fun updateBlogImage(iconUrl: String?) = with(binding) {
        if (iconUrl != null) {
            imageManager.loadIntoCircle(
                    imageView = siteIcon,
                    imageType = BLAVATAR_CIRCULAR,
                    imgUrl = iconUrl
            )
        } else {
            imageManager.cancelRequestAndClearImageView(siteIcon)
        }
    }
}
