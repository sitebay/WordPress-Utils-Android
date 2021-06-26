package org.sitebay.android.ui.reader.discover

import dagger.Reusable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.sitebay.android.R
import org.sitebay.android.datasets.ReaderBlogTableWrapper
import org.sitebay.android.datasets.wrappers.ReaderPostTableWrapper
import org.sitebay.android.models.ReaderPost
import org.sitebay.android.modules.BG_THREAD
import org.sitebay.android.ui.reader.discover.ReaderPostCardAction.SecondaryAction
import org.sitebay.android.ui.reader.discover.ReaderPostCardAction.SpacerNoAction
import org.sitebay.android.ui.reader.discover.ReaderPostCardActionType.BLOCK_SITE
import org.sitebay.android.ui.reader.discover.ReaderPostCardActionType.FOLLOW
import org.sitebay.android.ui.reader.discover.ReaderPostCardActionType.REPORT_POST
import org.sitebay.android.ui.reader.discover.ReaderPostCardActionType.SHARE
import org.sitebay.android.ui.reader.discover.ReaderPostCardActionType.SITE_NOTIFICATIONS
import org.sitebay.android.ui.reader.discover.ReaderPostCardActionType.TOGGLE_SEEN_STATUS
import org.sitebay.android.ui.reader.discover.ReaderPostCardActionType.VISIT_SITE
import org.sitebay.android.ui.reader.utils.ReaderUtilsWrapper
import org.sitebay.android.ui.utils.UiString.UiStringRes
import org.sitebay.android.util.config.SeenUnseenWithCounterFeatureConfig
import javax.inject.Inject
import javax.inject.Named

@Suppress("TooManyFunctions")
@Reusable
class ReaderPostMoreButtonUiStateBuilder @Inject constructor(
    private val readerPostTableWrapper: ReaderPostTableWrapper,
    private val readerBlogTableWrapper: ReaderBlogTableWrapper,
    private val readerUtilsWrapper: ReaderUtilsWrapper,
    private val seenUnseenWithCounterFeatureConfig: SeenUnseenWithCounterFeatureConfig,
    @Named(BG_THREAD) private val bgDispatcher: CoroutineDispatcher
) {
    suspend fun buildMoreMenuItems(
        post: ReaderPost,
        onButtonClicked: (Long, Long, ReaderPostCardActionType) -> Unit
    ): List<ReaderPostCardAction> {
        return withContext(bgDispatcher) {
            buildMoreMenuItemsBlocking(post, onButtonClicked)
        }
    }

    fun buildMoreMenuItemsBlocking(
        post: ReaderPost,
        onButtonClicked: (Long, Long, ReaderPostCardActionType) -> Unit
    ): MutableList<ReaderPostCardAction> {
        val menuItems = mutableListOf<ReaderPostCardAction>()
        val isPostFollowed = readerPostTableWrapper.isPostFollowed(post)

        menuItems.add(buildVisitSite(onButtonClicked))
        checkAndAddMenuItemForSiteNotifications(menuItems, isPostFollowed, post, onButtonClicked)
        checkAndAddMenuItemForPostSeenUnseen(menuItems, post, onButtonClicked)
        menuItems.add(buildShare(onButtonClicked))
        menuItems.add(buildFollow(isPostFollowed, onButtonClicked))
        menuItems.add(SpacerNoAction())
        checkAndAddMenuItemForBlockSite(menuItems, isPostFollowed, onButtonClicked)
        menuItems.add(buildReportPost(onButtonClicked))

        return menuItems
    }

    private fun buildVisitSite(onButtonClicked: (Long, Long, ReaderPostCardActionType) -> Unit) =
            SecondaryAction(
                    type = VISIT_SITE,
                    label = UiStringRes(R.string.reader_label_visit),
                    labelColor = R.attr.colorOnSurface,
                    iconRes = R.drawable.ic_globe_white_24dp,
                    iconColor = R.attr.wpColorOnSurfaceMedium,
                    onClicked = onButtonClicked
            )

    private fun checkAndAddMenuItemForSiteNotifications(
        menuItems: MutableList<ReaderPostCardAction>,
        isPostFollowed: Boolean,
        post: ReaderPost,
        onButtonClicked: (Long, Long, ReaderPostCardActionType) -> Unit
    ) {
        if (isPostFollowed) {
            // When post not from external feed then show notifications option.
            if (!readerUtilsWrapper.isExternalFeed(post.blogId, post.feedId)) {
                menuItems.add(buildSiteNotifications(
                        readerBlogTableWrapper.isNotificationsEnabled(post.blogId), onButtonClicked))
            }
        }
    }

    private fun buildSiteNotifications(
        isNotificationsEnabled: Boolean,
        onButtonClicked: (Long, Long, ReaderPostCardActionType) -> Unit
    ): SecondaryAction =
            if (isNotificationsEnabled) {
                SecondaryAction(
                        type = SITE_NOTIFICATIONS,
                        label = UiStringRes(R.string.reader_btn_notifications_off),
                        labelColor = R.attr.wpColorOnSurfaceMedium,
                        iconRes = R.drawable.ic_bell_white_24dp,
                        isSelected = true,
                        onClicked = onButtonClicked
                )
            } else {
                SecondaryAction(
                        type = SITE_NOTIFICATIONS,
                        label = UiStringRes(R.string.reader_btn_notifications_on),
                        labelColor = R.attr.colorOnSurface,
                        iconRes = R.drawable.ic_bell_white_24dp,
                        iconColor = R.attr.wpColorOnSurfaceMedium,
                        isSelected = false,
                        onClicked = onButtonClicked
                )
            }

    private fun checkAndAddMenuItemForPostSeenUnseen(
        menuItems: MutableList<ReaderPostCardAction>,
        post: ReaderPost,
        onButtonClicked: (Long, Long, ReaderPostCardActionType) -> Unit
    ) {
        if (seenUnseenWithCounterFeatureConfig.isEnabled()) {
            if (post.isSeenSupported) {
                menuItems.add(buildPostSeenUnseen(readerPostTableWrapper.isPostSeen(post), onButtonClicked))
            }
        }
    }

    private fun buildPostSeenUnseen(
        isPostSeen: Boolean,
        onButtonClicked: (Long, Long, ReaderPostCardActionType) -> Unit
    ): SecondaryAction =
        if (isPostSeen) {
            SecondaryAction(
                    type = TOGGLE_SEEN_STATUS,
                    label = UiStringRes(R.string.reader_menu_mark_as_unseen),
                    labelColor = R.attr.colorOnSurface,
                    iconRes = R.drawable.ic_not_visible_white_24dp,
                    iconColor = R.attr.wpColorOnSurfaceMedium,
                    isSelected = false,
                    onClicked = onButtonClicked
            )
        } else {
            SecondaryAction(
                    type = TOGGLE_SEEN_STATUS,
                    label = UiStringRes(R.string.reader_menu_mark_as_seen),
                    labelColor = R.attr.colorOnSurface,
                    iconRes = R.drawable.ic_visible_white_24dp,
                    iconColor = R.attr.wpColorOnSurfaceMedium,
                    isSelected = false,
                    onClicked = onButtonClicked
            )
        }

    private fun buildShare(onButtonClicked: (Long, Long, ReaderPostCardActionType) -> Unit) =
        SecondaryAction(
        type = SHARE,
        label = UiStringRes(R.string.reader_btn_share),
        labelColor = R.attr.colorOnSurface,
        iconRes = R.drawable.ic_share_white_24dp,
        iconColor = R.attr.wpColorOnSurfaceMedium,
        onClicked = onButtonClicked
    )

    private fun buildFollow(isPostFollowed: Boolean, onButtonClicked: (Long, Long, ReaderPostCardActionType) -> Unit) =
        if (isPostFollowed) {
            SecondaryAction(
                    type = FOLLOW,
                    label = UiStringRes(R.string.reader_btn_unfollow),
                    labelColor = R.attr.wpColorOnSurfaceMedium,
                    iconRes = R.drawable.ic_reader_following_white_24dp,
                    isSelected = true,
                    onClicked = onButtonClicked
            )
        } else {
            SecondaryAction(
                    type = FOLLOW,
                    label = UiStringRes(R.string.reader_btn_follow),
                    labelColor = R.attr.colorSecondary,
                    iconRes = R.drawable.ic_reader_follow_white_24dp,
                    isSelected = false,
                    onClicked = onButtonClicked
            )
        }

    private fun checkAndAddMenuItemForBlockSite(
        menuItems: MutableList<ReaderPostCardAction>,
        isPostFollowed: Boolean,
        onButtonClicked: (Long, Long, ReaderPostCardActionType) -> Unit
    ) {
        if (!isPostFollowed) menuItems.add(buildBlockSite(onButtonClicked))
    }

    private fun buildBlockSite(onButtonClicked: (Long, Long, ReaderPostCardActionType) -> Unit) =
            SecondaryAction(
                    type = BLOCK_SITE,
                    label = UiStringRes(R.string.reader_menu_block_blog),
                    labelColor = R.attr.wpColorError,
                    iconRes = R.drawable.ic_block_white_24dp,
                    iconColor = R.attr.wpColorError,
                    onClicked = onButtonClicked
            )

    private fun buildReportPost(onButtonClicked: (Long, Long, ReaderPostCardActionType) -> Unit) =
            SecondaryAction(
                    type = REPORT_POST,
                    label = UiStringRes(R.string.reader_menu_report_post),
                    labelColor = R.attr.wpColorError,
                    iconRes = R.drawable.ic_block_white_24dp,
                    iconColor = R.attr.wpColorError,
                    onClicked = onButtonClicked
            )
}
