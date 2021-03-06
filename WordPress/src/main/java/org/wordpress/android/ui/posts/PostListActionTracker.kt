package org.sitebay.android.ui.posts

import org.sitebay.android.analytics.AnalyticsTracker.Stat
import org.sitebay.android.fluxc.model.PostModel
import org.sitebay.android.fluxc.model.SiteModel
import org.sitebay.android.util.analytics.AnalyticsUtils
import org.sitebay.android.widgets.PostListButtonType
import org.sitebay.android.widgets.PostListButtonType.BUTTON_CANCEL_PENDING_AUTO_UPLOAD
import org.sitebay.android.widgets.PostListButtonType.BUTTON_COPY
import org.sitebay.android.widgets.PostListButtonType.BUTTON_DELETE
import org.sitebay.android.widgets.PostListButtonType.BUTTON_DELETE_PERMANENTLY
import org.sitebay.android.widgets.PostListButtonType.BUTTON_EDIT
import org.sitebay.android.widgets.PostListButtonType.BUTTON_MORE
import org.sitebay.android.widgets.PostListButtonType.BUTTON_MOVE_TO_DRAFT
import org.sitebay.android.widgets.PostListButtonType.BUTTON_PREVIEW
import org.sitebay.android.widgets.PostListButtonType.BUTTON_PUBLISH
import org.sitebay.android.widgets.PostListButtonType.BUTTON_RETRY
import org.sitebay.android.widgets.PostListButtonType.BUTTON_SHOW_MOVE_TRASHED_POST_TO_DRAFT_DIALOG
import org.sitebay.android.widgets.PostListButtonType.BUTTON_STATS
import org.sitebay.android.widgets.PostListButtonType.BUTTON_SUBMIT
import org.sitebay.android.widgets.PostListButtonType.BUTTON_SYNC
import org.sitebay.android.widgets.PostListButtonType.BUTTON_TRASH
import org.sitebay.android.widgets.PostListButtonType.BUTTON_VIEW

fun trackPostListAction(site: SiteModel, buttonType: PostListButtonType, postData: PostModel, statsEvent: Stat) {
    val properties = HashMap<String, Any?>()
    if (!postData.isLocalDraft) {
        properties["post_id"] = postData.remotePostId
    }

    properties["action"] = when (buttonType) {
        BUTTON_EDIT -> {
            properties[AnalyticsUtils.HAS_GUTENBERG_BLOCKS_KEY] = PostUtils
                    .contentContainsGutenbergBlocks(postData.content)
            "edit"
        }
        BUTTON_RETRY -> "retry"
        BUTTON_SUBMIT -> "submit"
        BUTTON_VIEW -> "view"
        BUTTON_PREVIEW -> "preview"
        BUTTON_STATS -> "stats"
        BUTTON_TRASH -> "trash"
        BUTTON_COPY -> "copy"
        BUTTON_DELETE,
        BUTTON_DELETE_PERMANENTLY -> "delete"
        BUTTON_PUBLISH -> "publish"
        BUTTON_SYNC -> "sync"
        BUTTON_MORE -> "more"
        BUTTON_MOVE_TO_DRAFT -> "move_to_draft"
        BUTTON_CANCEL_PENDING_AUTO_UPLOAD -> "cancel_pending_auto_upload"
        BUTTON_SHOW_MOVE_TRASHED_POST_TO_DRAFT_DIALOG -> "show_move_trashed_post_to_draft_post_dialog"
    }

    AnalyticsUtils.trackWithSiteDetails(statsEvent, site, properties)
}
