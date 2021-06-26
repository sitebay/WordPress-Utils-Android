package org.sitebay.android.ui.posts

import dagger.Reusable
import org.sitebay.android.fluxc.model.PostImmutableModel
import org.sitebay.android.fluxc.model.PostModel
import org.sitebay.android.fluxc.model.SiteModel
import org.sitebay.android.fluxc.model.post.PostStatus
import org.sitebay.android.ui.reader.utils.DateProvider
import javax.inject.Inject

/**
 * Injectable wrapper around PostUtils.
 *
 * PostUtils interface is consisted of static methods, which make the client code difficult to test/mock. Main purpose
 * of this wrapper is to make testing easier.
 *
 */
@Reusable
class PostUtilsWrapper @Inject constructor(private val dateProvider: DateProvider) {
    fun isPublishable(post: PostImmutableModel) = PostUtils.isPublishable(post)

    fun isPostInConflictWithRemote(post: PostImmutableModel) =
            PostUtils.isPostInConflictWithRemote(post)

    fun isPostCurrentlyBeingEdited(post: PostImmutableModel) =
            PostUtils.isPostCurrentlyBeingEdited(post)

    fun shouldPublishImmediately(postStatus: PostStatus, dateCreated: String) =
            PostUtils.shouldPublishImmediately(postStatus, dateCreated)

    fun postHasEdits(oldPost: PostImmutableModel?, newPost: PostImmutableModel) =
            PostUtils.postHasEdits(oldPost, newPost)

    fun isMediaInGutenbergPostBody(postContent: String, localMediaId: String) =
            PostUtils.isMediaInGutenbergPostBody(postContent, localMediaId)

    fun contentContainsGutenbergBlocks(postContent: String): Boolean =
            PostUtils.contentContainsGutenbergBlocks(postContent)

    fun trackSavePostAnalytics(post: PostImmutableModel?, site: SiteModel) =
            PostUtils.trackSavePostAnalytics(post, site)

    fun preparePostForPublish(post: PostModel, site: SiteModel) =
            PostUtils.preparePostForPublish(post, site)

    fun isPublishDateInTheFuture(dateCreated: String) =
            PostUtils.isPublishDateInTheFuture(dateCreated, dateProvider.getCurrentDate())

    fun isPublishDateInThePast(dateCreated: String) =
            PostUtils.isPublishDateInThePast(dateCreated, dateProvider.getCurrentDate())

    fun shouldPublishImmediatelyOptionBeAvailable(status: PostStatus?) =
            PostUtils.shouldPublishImmediatelyOptionBeAvailable(status)
}
