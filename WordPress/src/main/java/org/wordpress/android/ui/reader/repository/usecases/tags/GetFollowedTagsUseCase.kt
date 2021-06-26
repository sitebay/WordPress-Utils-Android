package org.sitebay.android.ui.reader.repository.usecases.tags

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.sitebay.android.datasets.wrappers.ReaderTagTableWrapper
import org.sitebay.android.models.ReaderTagList
import org.sitebay.android.modules.IO_THREAD
import javax.inject.Inject
import javax.inject.Named

class GetFollowedTagsUseCase @Inject constructor(
    @Named(IO_THREAD) private val ioDispatcher: CoroutineDispatcher,
    private val readerTagTableWrapper: ReaderTagTableWrapper
) {
    suspend fun get(): ReaderTagList = withContext(ioDispatcher) {
        readerTagTableWrapper.getFollowedTags()
    }
}
