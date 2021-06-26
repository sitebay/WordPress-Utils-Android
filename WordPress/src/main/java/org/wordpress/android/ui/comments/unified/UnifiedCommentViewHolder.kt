package org.sitebay.android.ui.comments.unified

import android.text.TextUtils
import android.view.ViewGroup
import org.sitebay.android.R
import org.sitebay.android.databinding.CommentListItemBinding
import org.sitebay.android.ui.comments.unified.UnifiedCommentListItem.Comment
import org.sitebay.android.ui.utils.UiHelpers
import org.sitebay.android.util.GravatarUtils
import org.sitebay.android.util.GravatarUtilsWrapper
import org.sitebay.android.util.image.ImageManager
import org.sitebay.android.util.image.ImageType.AVATAR_WITH_BACKGROUND
import org.sitebay.android.util.viewBinding
import org.sitebay.android.viewmodel.ResourceProvider

class UnifiedCommentViewHolder(
    parent: ViewGroup,
    private val imageManager: ImageManager,
    private val uiHelpers: UiHelpers,
    private val commentListUiUtils: CommentListUiUtils,
    private val resourceProvider: ResourceProvider,
    private val gravatarUtilsWrapper: GravatarUtilsWrapper
) : UnifiedCommentListViewHolder<CommentListItemBinding>(parent.viewBinding(CommentListItemBinding::inflate)) {
    fun bind(item: Comment) = with(binding) {
        title.text = commentListUiUtils.formatCommentTitle(item.authorName, item.postTitle, title.context)
        comment.text = commentListUiUtils.formatCommentContent(item.content, comment.context)

        if (item.isSelected) {
            imageManager.cancelRequestAndClearImageView(avatar)
        } else {
            imageManager.loadIntoCircle(
                    avatar,
                    AVATAR_WITH_BACKGROUND,
                    getGravatarUrl(item)
            )
        }

        uiHelpers.updateVisibility(imageCheckmark, item.isSelected)
        commentListUiUtils.toggleSelectedStateOfCommentListItem(layoutContainer, item.isSelected)

        uiHelpers.updateVisibility(statusIndicator, item.isPending)

        itemView.setOnClickListener {
            item.clickAction.onClick()
        }
        itemView.setOnLongClickListener {
            item.toggleAction.onToggle()
            true
        }
    }

    fun getGravatarUrl(comment: Comment): String {
        return if (!TextUtils.isEmpty(comment.authorAvatarUrl)) {
            gravatarUtilsWrapper.fixGravatarUrl(
                    comment.authorAvatarUrl,
                    resourceProvider.getDimensionPixelSize(R.dimen.avatar_sz_medium)
            )
        } else if (!TextUtils.isEmpty(comment.authorEmail)) {
            GravatarUtils.gravatarFromEmail(
                    comment.authorEmail,
                    resourceProvider.getDimensionPixelSize(R.dimen.avatar_sz_medium)
            )
        } else {
            ""
        }
    }
}
