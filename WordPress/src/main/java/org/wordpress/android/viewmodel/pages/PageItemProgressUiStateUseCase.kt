package org.sitebay.android.viewmodel.pages

import org.sitebay.android.ui.prefs.AppPrefsWrapper
import org.sitebay.android.viewmodel.pages.PostModelUploadUiStateUseCase.PostUploadUiState
import org.sitebay.android.viewmodel.pages.PostModelUploadUiStateUseCase.PostUploadUiState.UploadQueued
import org.sitebay.android.viewmodel.pages.PostModelUploadUiStateUseCase.PostUploadUiState.UploadingMedia
import org.sitebay.android.viewmodel.pages.PostModelUploadUiStateUseCase.PostUploadUiState.UploadingPost
import org.sitebay.android.viewmodel.uistate.ProgressBarUiState
import javax.inject.Inject

typealias ShouldShowOverlay = Boolean

class PageItemProgressUiStateUseCase @Inject constructor(
    private val appPrefsWrapper: AppPrefsWrapper
) {
    fun getProgressStateForPage(
        uploadUiState: PostUploadUiState
    ): Pair<ProgressBarUiState, ShouldShowOverlay> {
        val shouldShowOverlay = shouldShowOverlay(uploadUiState)
        return Pair(getProgressBarState(uploadUiState), shouldShowOverlay)
    }

    /**
     * Copied from PostListItemUiStateHelper since the behavior is similar for the Page List UI State.
     */
    private fun getProgressBarState(
        uploadUiState: PostUploadUiState
    ): ProgressBarUiState {
        return if (shouldShowProgress(uploadUiState)) {
            if (uploadUiState is UploadingMedia) {
                ProgressBarUiState.Determinate(uploadUiState.progress)
            } else {
                ProgressBarUiState.Indeterminate
            }
        } else {
            ProgressBarUiState.Hidden
        }
    }

    /**
     * Copied from PostListItemUiStateHelper since the behavior is similar for the Page List UI State.
     */
    private fun shouldShowProgress(
        uploadUiState: PostUploadUiState
    ): Boolean {
        return uploadUiState is UploadingPost || uploadUiState is UploadingMedia ||
                uploadUiState is UploadQueued
    }

    private fun shouldShowOverlay(uploadUiState: PostUploadUiState): Boolean {
        // show overlay when post upload is in progress or (media upload is in progress and the user is not using Aztec)
        return (uploadUiState is UploadingPost ||
                (!appPrefsWrapper.isAztecEditorEnabled && uploadUiState is UploadingMedia))
    }
}
