package org.sitebay.android.ui.posts.prepublishing.visibility.usecases

import org.sitebay.android.fluxc.model.PostImmutableModel
import org.sitebay.android.fluxc.model.PostModel
import org.sitebay.android.fluxc.model.post.PostStatus
import org.sitebay.android.ui.posts.EditPostRepository
import org.sitebay.android.ui.posts.EditPostRepository.UpdatePostResult
import org.sitebay.android.ui.posts.PostUtilsWrapper
import org.sitebay.android.util.DateTimeUtilsWrapper
import javax.inject.Inject

class UpdatePostStatusUseCase @Inject constructor(
    private val dateTimeUtilsWrapper: DateTimeUtilsWrapper,
    private val postUtilsWrapper: PostUtilsWrapper
) {
    fun updatePostStatus(
        postStatus: PostStatus,
        editPostRepository: EditPostRepository,
        onPostStatusUpdated: (PostImmutableModel) -> Unit
    ) {
        editPostRepository.updateAsync({ postModel: PostModel ->
            // we set the date to immediately if it's scheduled.
            if (postStatus == PostStatus.PRIVATE) {
                if (postUtilsWrapper.isPublishDateInTheFuture(postModel.dateCreated))
                    postModel.setDateCreated(dateTimeUtilsWrapper.currentTimeInIso8601())
            }

            postModel.setStatus(postStatus.toString())

            true
        }, { postModel, result ->
            if (result == UpdatePostResult.Updated) {
                onPostStatusUpdated.invoke(postModel)
            }
        })
    }
}
