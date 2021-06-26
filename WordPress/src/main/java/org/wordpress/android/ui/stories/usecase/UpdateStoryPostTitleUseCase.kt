package org.sitebay.android.ui.stories.usecase

import dagger.Reusable
import org.sitebay.android.ui.posts.EditPostRepository
import javax.inject.Inject

@Reusable
class UpdateStoryPostTitleUseCase @Inject constructor() {
    fun updateStoryTitle(storyTitle: String, editPostRepository: EditPostRepository) {
        editPostRepository.updateAsync({ postModel ->
            postModel.setTitle(storyTitle)
            true
        })
    }
}
