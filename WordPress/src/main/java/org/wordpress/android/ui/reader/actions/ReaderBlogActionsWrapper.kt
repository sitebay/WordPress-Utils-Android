package org.sitebay.android.ui.reader.actions

import org.sitebay.android.ui.reader.actions.ReaderActions.ActionListener
import org.sitebay.android.ui.reader.actions.ReaderActions.UpdateBlogInfoListener
import org.sitebay.android.ui.reader.actions.ReaderBlogActions.BlockedBlogResult
import org.sitebay.android.ui.reader.tracker.ReaderTracker
import org.sitebay.android.ui.reader.utils.ReaderUtilsWrapper
import javax.inject.Inject

class ReaderBlogActionsWrapper @Inject constructor(
    private val readerUtilsWrapper: ReaderUtilsWrapper
) {
    fun blockBlogFromReaderLocal(
        blogId: Long,
        feedId: Long
    ): BlockedBlogResult = ReaderBlogActions.blockBlogFromReaderLocal(
            blogId,
            feedId
    )

    fun blockBlogFromReaderRemote(blockedBlogResult: BlockedBlogResult, actionListener: ActionListener?): Unit =
            ReaderBlogActions.blockBlogFromReaderRemote(blockedBlogResult, actionListener)

    @Suppress("LongParameterList")
    fun followBlog(
        blogId: Long,
        feedId: Long,
        isAskingToFollow: Boolean,
        actionListener: ActionListener,
        source: String,
        readerTracker: ReaderTracker
    ) = ReaderBlogActions.followBlog(
            blogId,
            feedId,
            isAskingToFollow,
            actionListener,
            source,
            readerTracker
    )

    fun updateBlogInfo(blogId: Long, feedId: Long, blogUrl: String?, infoListener: UpdateBlogInfoListener) =
            if (readerUtilsWrapper.isExternalFeed(blogId, feedId)) {
                ReaderBlogActions.updateFeedInfo(blogId, blogUrl, infoListener)
            } else {
                ReaderBlogActions.updateBlogInfo(blogId, blogUrl, infoListener)
            }
}
