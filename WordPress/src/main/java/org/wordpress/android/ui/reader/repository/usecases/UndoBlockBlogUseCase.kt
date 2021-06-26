package org.sitebay.android.ui.reader.repository.usecases

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.sitebay.android.modules.BG_THREAD
import org.sitebay.android.ui.reader.actions.ReaderBlogActions
import org.sitebay.android.ui.reader.actions.ReaderBlogActions.BlockedBlogResult
import org.sitebay.android.ui.reader.tracker.ReaderTracker
import javax.inject.Inject
import javax.inject.Named

class UndoBlockBlogUseCase @Inject constructor(
    private val readerTracker: ReaderTracker,
    @Named(BG_THREAD) private val bgDispatcher: CoroutineDispatcher
) {
    suspend fun undoBlockBlog(
        blockedBlogData: BlockedBlogResult,
        source: String
    ) {
        withContext(bgDispatcher) {
            ReaderBlogActions.undoBlockBlogFromReader(
                    blockedBlogData,
                    source,
                    readerTracker
            )
        }
    }
}
