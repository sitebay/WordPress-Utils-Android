package org.sitebay.android.ui.comments.unified

import android.content.Context
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import org.sitebay.android.WordPress
import org.sitebay.android.ui.comments.unified.UnifiedCommentListItem.Comment
import org.sitebay.android.ui.comments.unified.UnifiedCommentListItem.CommentListItemType.COMMENT
import org.sitebay.android.ui.comments.unified.UnifiedCommentListItem.CommentListItemType.SUB_HEADER
import org.sitebay.android.ui.comments.unified.UnifiedCommentListItem.SubHeader
import org.sitebay.android.ui.utils.UiHelpers
import org.sitebay.android.util.GravatarUtilsWrapper
import org.sitebay.android.util.image.ImageManager
import org.sitebay.android.viewmodel.ResourceProvider
import javax.inject.Inject

class UnifiedCommentListAdapter(context: Context) :
        PagingDataAdapter<UnifiedCommentListItem, UnifiedCommentListViewHolder<*>>(
                diffCallback
        ) {
    @Inject lateinit var imageManager: ImageManager
    @Inject lateinit var uiHelpers: UiHelpers
    @Inject lateinit var commentListUiUtils: CommentListUiUtils
    @Inject lateinit var resourceProvider: ResourceProvider
    @Inject lateinit var gravatarUtilsWrapper: GravatarUtilsWrapper

    init {
        (context.applicationContext as WordPress).component().inject(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UnifiedCommentListViewHolder<*> {
        return when (viewType) {
            SUB_HEADER.ordinal -> UnifiedCommentSubHeaderViewHolder(parent)
            COMMENT.ordinal -> UnifiedCommentViewHolder(
                    parent,
                    imageManager,
                    uiHelpers,
                    commentListUiUtils,
                    resourceProvider,
                    gravatarUtilsWrapper
            )
            else -> throw IllegalArgumentException("Unexpected view holder in UnifiedCommentListAdapter")
        }
    }

    override fun onBindViewHolder(holder: UnifiedCommentListViewHolder<*>, position: Int) {
        if (holder is UnifiedCommentSubHeaderViewHolder) {
            holder.bind((getItem(position) as SubHeader))
        } else if (holder is UnifiedCommentViewHolder) {
            holder.bind(getItem(position) as Comment)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position)!!.type.ordinal
    }

    companion object {
        private val diffCallback = UnifiedCommentsListDiffCallback()
    }
}
