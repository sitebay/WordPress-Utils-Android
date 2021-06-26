package org.sitebay.android.ui.posts.editor.media

import android.text.TextUtils
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.sitebay.android.fluxc.Dispatcher
import org.sitebay.android.fluxc.generated.MediaActionBuilder
import org.sitebay.android.fluxc.store.MediaStore
import org.sitebay.android.modules.BG_THREAD
import org.sitebay.android.ui.uploads.UploadServiceFacade
import org.sitebay.android.util.AppLog
import org.sitebay.android.util.MediaUtilsWrapper
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named

class RemoveMediaUseCase @Inject constructor(
    private val mediaStore: MediaStore,
    private val dispatcher: Dispatcher,
    private val mediaUtils: MediaUtilsWrapper,
    private val uploadService: UploadServiceFacade,
    @Named(BG_THREAD) private val bgDispatcher: CoroutineDispatcher
) {
    suspend fun removeMediaIfNotUploading(mediaIds: List<String>) = withContext(bgDispatcher) {
        for (mediaId in mediaIds) {
            if (!TextUtils.isEmpty(mediaId)) {
                // make sure the MediaModel exists
                val mediaModel = try {
                    mediaStore.getMediaWithLocalId(Integer.valueOf(mediaId)) ?: continue
                } catch (e: NumberFormatException) {
                    AppLog.e(AppLog.T.MEDIA, "Invalid media id: $mediaId")
                    continue
                }

                // also make sure it's not being uploaded anywhere else (maybe on some other Post,
                // simultaneously)
                if (mediaModel.uploadState != null &&
                        mediaUtils.isLocalFile(mediaModel.uploadState.toLowerCase(Locale.ROOT)) &&
                        !uploadService.isPendingOrInProgressMediaUpload(mediaModel)) {
                    dispatcher.dispatch(MediaActionBuilder.newRemoveMediaAction(mediaModel))
                }
            }
        }
    }
}
