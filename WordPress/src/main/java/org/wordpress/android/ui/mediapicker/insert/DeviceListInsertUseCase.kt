package org.sitebay.android.ui.mediapicker.insert

import kotlinx.coroutines.flow.flow
import org.sitebay.android.ui.mediapicker.MediaItem.Identifier
import org.sitebay.android.ui.mediapicker.MediaItem.Identifier.LocalUri
import org.sitebay.android.ui.mediapicker.insert.MediaInsertHandler.InsertModel
import org.sitebay.android.util.UriWrapper
import org.sitebay.android.util.WPMediaUtilsWrapper
import javax.inject.Inject

class DeviceListInsertUseCase(
    private val wpMediaUtilsWrapper: WPMediaUtilsWrapper,
    private val queueResults: Boolean
) : MediaInsertUseCase {
    override suspend fun insert(identifiers: List<Identifier>) = flow {
        val localUris = identifiers.mapNotNull { it as? LocalUri }
        emit(InsertModel.Progress(actionTitle))
        var failed = false
        val fetchedUris = localUris.mapNotNull { localUri ->
            val fetchedUri = wpMediaUtilsWrapper.fetchMedia(localUri.value.uri)
            if (fetchedUri == null) {
                failed = true
            }
            fetchedUri
        }
        if (failed) {
            emit(InsertModel.Error("Failed to fetch local media"))
        } else {
            emit(InsertModel.Success(fetchedUris.map { LocalUri(UriWrapper(it), queueResults) }))
        }
    }

    class DeviceListInsertUseCaseFactory
    @Inject constructor(
        private val wpMediaUtilsWrapper: WPMediaUtilsWrapper
    ) {
        fun build(queueResults: Boolean): DeviceListInsertUseCase {
            return DeviceListInsertUseCase(
                    wpMediaUtilsWrapper,
                    queueResults
            )
        }
    }
}
