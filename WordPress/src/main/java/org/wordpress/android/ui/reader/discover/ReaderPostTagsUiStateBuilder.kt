package org.sitebay.android.ui.reader.discover

import dagger.Reusable
import org.sitebay.android.R
import org.sitebay.android.models.ReaderPost
import org.sitebay.android.ui.reader.discover.interests.TagUiState
import org.sitebay.android.util.DisplayUtils
import org.sitebay.android.viewmodel.ContextProvider
import org.sitebay.android.viewmodel.ResourceProvider
import javax.inject.Inject

@Reusable
class ReaderPostTagsUiStateBuilder @Inject constructor(
    private val contextProvider: ContextProvider,
    private val resourceProvider: ResourceProvider
) {
    private val maxWidthForChip: Int
        get() {
            val width = DisplayUtils.getDisplayPixelWidth(contextProvider.getContext()) -
                    resourceProvider.getDimensionPixelSize(R.dimen.reader_card_margin) * 2
            return (width * MAX_WIDTH_FACTOR).toInt()
        }

    fun mapPostTagsToTagUiStates(
        post: ReaderPost,
        onClick: (String) -> Unit
    ): List<TagUiState> {
        return post.tags.map {
            TagUiState(
                title = it.tagTitle,
                slug = it.tagSlug,
                onClick = onClick,
                maxWidth = maxWidthForChip
            )
        }
    }

    companion object {
        private const val MAX_WIDTH_FACTOR = 0.75
    }
}
