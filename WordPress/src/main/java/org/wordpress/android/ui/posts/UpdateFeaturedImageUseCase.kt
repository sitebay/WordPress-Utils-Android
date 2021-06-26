package org.sitebay.android.ui.posts

import org.sitebay.android.fluxc.model.PostImmutableModel
import org.sitebay.android.fluxc.model.PostModel
import org.sitebay.android.ui.posts.EditPostRepository.UpdatePostResult
import org.sitebay.android.ui.posts.EditPostRepository.UpdatePostResult.Updated
import javax.inject.Inject

class UpdateFeaturedImageUseCase @Inject constructor() {
    fun updateFeaturedImage(
        featuredImageId: Long,
        editPostRepository: EditPostRepository,
        onPostFeaturedImageUpdated: (PostImmutableModel) -> Unit
    ) {
        editPostRepository.updateAsync({ postModel: PostModel ->
            postModel.setFeaturedImageId(featuredImageId)
            true
        }) { postModel: PostImmutableModel, result: UpdatePostResult ->
            if (result === Updated) {
                onPostFeaturedImageUpdated.invoke(postModel)
            }
        }
    }
}
