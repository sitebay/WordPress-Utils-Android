package org.sitebay.android.ui.posts.prepublishing.home.usecases

import org.sitebay.android.fluxc.model.PostModel
import org.sitebay.android.fluxc.model.post.PostStatus
import org.sitebay.android.fluxc.model.post.PostStatus.SCHEDULED
import org.sitebay.android.ui.posts.EditPostRepository
import org.sitebay.android.util.DateTimeUtilsWrapper
import javax.inject.Inject

class PublishPostImmediatelyUseCase @Inject constructor(private val dateTimeUtilsWrapper: DateTimeUtilsWrapper) {
    fun updatePostToPublishImmediately(
        editPostRepository: EditPostRepository,
        isNewPost: Boolean
    ) {
        editPostRepository.updateAsync({ postModel: PostModel ->
            if (postModel.status == SCHEDULED.toString()) {
                postModel.setDateCreated(dateTimeUtilsWrapper.currentTimeInIso8601())
            }
            // when the post is a Draft, Publish Now is shown as the Primary Action but if it's already Published then
            // Update Now is shown.
            if (isNewPost) {
                postModel.setStatus(PostStatus.DRAFT.toString())
            } else {
                postModel.setStatus(PostStatus.PUBLISHED.toString())
            }
            true
        })
    }
}
