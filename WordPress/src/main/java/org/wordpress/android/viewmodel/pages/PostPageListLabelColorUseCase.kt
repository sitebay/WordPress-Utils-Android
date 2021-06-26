package org.sitebay.android.viewmodel.pages

import androidx.annotation.ColorRes
import org.sitebay.android.R
import org.sitebay.android.fluxc.model.PostModel
import org.sitebay.android.fluxc.model.post.PostStatus
import org.sitebay.android.fluxc.model.post.PostStatus.PENDING
import org.sitebay.android.fluxc.model.post.PostStatus.PRIVATE
import org.sitebay.android.viewmodel.pages.PostModelUploadUiStateUseCase.PostUploadUiState
import org.sitebay.android.viewmodel.pages.PostModelUploadUiStateUseCase.PostUploadUiState.UploadFailed
import org.sitebay.android.viewmodel.pages.PostModelUploadUiStateUseCase.PostUploadUiState.UploadQueued
import org.sitebay.android.viewmodel.pages.PostModelUploadUiStateUseCase.PostUploadUiState.UploadWaitingForConnection
import org.sitebay.android.viewmodel.pages.PostModelUploadUiStateUseCase.PostUploadUiState.UploadingMedia
import org.sitebay.android.viewmodel.pages.PostModelUploadUiStateUseCase.PostUploadUiState.UploadingPost
import javax.inject.Inject

const val ERROR_COLOR = R.color.error
const val PROGRESS_INFO_COLOR = R.color.neutral_50
const val STATE_INFO_COLOR = R.color.warning_dark

class PostPageListLabelColorUseCase @Inject constructor() {
    @ColorRes fun getLabelsColor(
        post: PostModel,
        uploadUiState: PostUploadUiState,
        hasUnhandledConflicts: Boolean,
        hasUnhandledAutoSave: Boolean
    ): Int? {
        return getLabelColor(
                PostStatus.fromPost(post),
                post.isLocalDraft,
                post.isLocallyChanged,
                uploadUiState,
                hasUnhandledConflicts,
                hasUnhandledAutoSave
        )
    }

    @ColorRes private fun getLabelColor(
        postStatus: PostStatus,
        isLocalDraft: Boolean,
        isLocallyChanged: Boolean,
        uploadUiState: PostUploadUiState,
        hasUnhandledConflicts: Boolean,
        hasAutoSave: Boolean
    ): Int? {
        val isError = (uploadUiState is UploadFailed && !uploadUiState.isEligibleForAutoUpload) ||
                hasUnhandledConflicts
        val isProgressInfo = uploadUiState is UploadingPost || uploadUiState is UploadingMedia ||
                uploadUiState is UploadQueued
        val isStateInfo = (uploadUiState is UploadFailed && uploadUiState.isEligibleForAutoUpload) ||
                isLocalDraft || isLocallyChanged || postStatus == PRIVATE || postStatus == PENDING ||
                uploadUiState is UploadWaitingForConnection || hasAutoSave

        return when {
            isError -> ERROR_COLOR
            isProgressInfo -> PROGRESS_INFO_COLOR
            isStateInfo -> STATE_INFO_COLOR
            else -> null
        }
    }
}
