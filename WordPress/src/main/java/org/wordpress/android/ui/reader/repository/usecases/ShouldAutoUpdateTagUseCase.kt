package org.sitebay.android.ui.reader.repository.usecases

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.sitebay.android.datasets.wrappers.ReaderTagTableWrapper
import org.sitebay.android.models.ReaderTag
import org.sitebay.android.modules.IO_THREAD
import javax.inject.Inject
import javax.inject.Named

class ShouldAutoUpdateTagUseCase @Inject constructor(
    @Named(IO_THREAD) private val ioDispatcher: CoroutineDispatcher,
    private val readerTagTableWrapper: ReaderTagTableWrapper
) {
    suspend fun get(readerTag: ReaderTag): Boolean =
            withContext(ioDispatcher) {
                readerTagTableWrapper.shouldAutoUpdateTag(readerTag)
            }
}
