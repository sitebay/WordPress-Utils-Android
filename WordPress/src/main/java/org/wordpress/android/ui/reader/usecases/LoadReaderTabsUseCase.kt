package org.sitebay.android.ui.reader.usecases

import dagger.Reusable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.sitebay.android.datasets.ReaderTagTable
import org.sitebay.android.models.ReaderTagList
import org.sitebay.android.modules.BG_THREAD
import org.sitebay.android.ui.reader.utils.ReaderUtils
import org.sitebay.android.ui.reader.utils.ReaderUtilsWrapper
import javax.inject.Inject
import javax.inject.Named

/**
 * Loads list of tags that should be displayed as tabs in the entry-point Reader screen.
 */
@Reusable
class LoadReaderTabsUseCase @Inject constructor(
    @Named(BG_THREAD) private val bgDispatcher: CoroutineDispatcher,
    private val readerUtilsWrapper: ReaderUtilsWrapper
) {
    suspend fun loadTabs(): ReaderTagList {
        return withContext(bgDispatcher) {
            val tagList = ReaderTagTable.getDefaultTags()

            /* Creating custom tag lists isn't supported anymore. However, we need to keep the support here
            for users who created custom lists in the past.*/
            tagList.addAll(ReaderTagTable.getCustomListTags())

            tagList.addAll(ReaderTagTable.getBookmarkTags()) // Add "Saved" tab manually

            // Add "Following" tab manually when on self-hosted site
            if (!tagList.containsFollowingTag()) {
                tagList.add(readerUtilsWrapper.getDefaultTagFromDbOrCreateInMemory())
            }

            ReaderUtils.getOrderedTagsList(tagList, ReaderUtils.getDefaultTagInfo())
        }
    }
}
