package org.sitebay.android.ui.stories.usecase

import android.content.Context
import org.sitebay.android.R
import org.sitebay.android.ui.posts.EditPostRepository
import org.sitebay.android.ui.stories.StoryRepositoryWrapper
import javax.inject.Inject

class SetUntitledStoryTitleIfTitleEmptyUseCase @Inject constructor(
    private val storyRepositoryWrapper: StoryRepositoryWrapper,
    private val updateStoryPostTitleUseCase: UpdateStoryPostTitleUseCase,
    private val context: Context
) {
    fun setUntitledStoryTitleIfTitleEmpty(editPostRepository: EditPostRepository) {
        if (editPostRepository.title.isEmpty()) {
            val untitledStoryTitle = context.resources.getString(R.string.untitled)
            storyRepositoryWrapper.setCurrentStoryTitle(untitledStoryTitle)
            updateStoryPostTitleUseCase.updateStoryTitle(untitledStoryTitle, editPostRepository)
        }
    }
}
