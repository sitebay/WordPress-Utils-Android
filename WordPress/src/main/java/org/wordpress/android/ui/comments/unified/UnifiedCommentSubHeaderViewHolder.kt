package org.sitebay.android.ui.comments.unified

import android.view.ViewGroup
import org.sitebay.android.databinding.CommentListSubheaderBinding
import org.sitebay.android.ui.comments.unified.UnifiedCommentListItem.SubHeader
import org.sitebay.android.util.viewBinding

class UnifiedCommentSubHeaderViewHolder(
    parent: ViewGroup
) : UnifiedCommentListViewHolder<CommentListSubheaderBinding>(
        parent.viewBinding(CommentListSubheaderBinding::inflate)
) {
    fun bind(item: SubHeader) = with(binding) {
        label.text = item.label
    }
}
