package org.sitebay.android.ui.reader.repository.usecases

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.sitebay.android.datasets.wrappers.ReaderPostTableWrapper
import org.sitebay.android.models.ReaderTag
import org.sitebay.android.modules.IO_THREAD
import javax.inject.Inject
import javax.inject.Named

class GetNumPostsForTagUseCase @Inject constructor(
    @Named(IO_THREAD) private val ioDispatcher: CoroutineDispatcher,
    private val readerPostTableWrapper: ReaderPostTableWrapper
) {
    suspend fun get(readerTag: ReaderTag): Int =
            withContext(ioDispatcher) {
                readerPostTableWrapper.getNumPostsWithTag(readerTag)
            }
}
