package org.sitebay.android.ui.posts.editor.media

import android.net.Uri
import org.sitebay.android.fluxc.model.MediaModel
import org.sitebay.android.fluxc.model.PostImmutableModel
import org.sitebay.android.ui.posts.EditPostActivity.OnPostUpdatedFromUIListener
import org.sitebay.android.util.helpers.MediaFile

interface EditorMediaListener {
    fun appendMediaFiles(mediaFiles: Map<String, MediaFile>)
    fun syncPostObjectWithUiAndSaveIt(listener: OnPostUpdatedFromUIListener? = null)
    fun advertiseImageOptimization(listener: () -> Unit)
    fun onMediaModelsCreatedFromOptimizedUris(oldUriToMediaFiles: Map<Uri, MediaModel>)
    fun getImmutablePost(): PostImmutableModel
}
