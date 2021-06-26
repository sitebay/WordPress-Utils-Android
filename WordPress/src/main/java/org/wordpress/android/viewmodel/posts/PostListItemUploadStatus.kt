package org.sitebay.android.viewmodel.posts

import org.sitebay.android.fluxc.store.UploadStore.UploadError

data class PostListItemUploadStatus(
    val uploadError: UploadError?,
    val mediaUploadProgress: Int,
    val isUploading: Boolean,
    val isUploadingOrQueued: Boolean,
    val isQueued: Boolean,
    val isUploadFailed: Boolean,
    val hasInProgressMediaUpload: Boolean,
    val hasPendingMediaUpload: Boolean,
    val isEligibleForAutoUpload: Boolean,
    val uploadWillPushChanges: Boolean
)
