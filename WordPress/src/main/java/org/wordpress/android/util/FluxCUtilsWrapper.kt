package org.sitebay.android.util

import android.content.Context
import android.net.Uri
import dagger.Reusable
import org.sitebay.android.fluxc.model.MediaModel
import org.sitebay.android.fluxc.store.MediaStore
import org.sitebay.android.util.helpers.MediaFile
import javax.inject.Inject

/**
 * Injectable wrapper around FluxCUtilsWrapper.
 *
 * FluxCUtilsWrapper interface is consisted of static methods, which make the client code difficult to test/mock.
 * Main purpose of this wrapper is to make testing easier.
 *
 */
@Reusable
class FluxCUtilsWrapper @Inject constructor(private val appContext: Context, private val mediaStore: MediaStore) {
    fun mediaModelFromLocalUri(
        uri: Uri,
        mimeType: String?,
        localSiteId: Int
    ): MediaModel? = FluxCUtils.mediaModelFromLocalUri(appContext, uri, mimeType, mediaStore, localSiteId)

    fun mediaFileFromMediaModel(mediaModel: MediaModel?): MediaFile? =
            FluxCUtils.mediaFileFromMediaModel(mediaModel)
}
