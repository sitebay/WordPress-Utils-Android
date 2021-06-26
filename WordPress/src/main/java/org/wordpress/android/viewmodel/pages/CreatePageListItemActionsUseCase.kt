package org.sitebay.android.viewmodel.pages

import org.sitebay.android.fluxc.model.SiteHomepageSettings.ShowOnFront
import org.sitebay.android.fluxc.model.SiteModel
import org.sitebay.android.ui.pages.PageItem.Action
import org.sitebay.android.ui.pages.PageItem.Action.CANCEL_AUTO_UPLOAD
import org.sitebay.android.ui.pages.PageItem.Action.COPY
import org.sitebay.android.ui.pages.PageItem.Action.DELETE_PERMANENTLY
import org.sitebay.android.ui.pages.PageItem.Action.MOVE_TO_DRAFT
import org.sitebay.android.ui.pages.PageItem.Action.MOVE_TO_TRASH
import org.sitebay.android.ui.pages.PageItem.Action.PUBLISH_NOW
import org.sitebay.android.ui.pages.PageItem.Action.SET_AS_HOMEPAGE
import org.sitebay.android.ui.pages.PageItem.Action.SET_AS_POSTS_PAGE
import org.sitebay.android.ui.pages.PageItem.Action.SET_PARENT
import org.sitebay.android.ui.pages.PageItem.Action.VIEW_PAGE
import org.sitebay.android.viewmodel.pages.PostModelUploadUiStateUseCase.PostUploadUiState
import org.sitebay.android.viewmodel.pages.PostModelUploadUiStateUseCase.PostUploadUiState.UploadFailed
import org.sitebay.android.viewmodel.pages.PostModelUploadUiStateUseCase.PostUploadUiState.UploadWaitingForConnection
import org.sitebay.android.viewmodel.pages.PageListViewModel.PageListType
import org.sitebay.android.viewmodel.pages.PageListViewModel.PageListType.DRAFTS
import org.sitebay.android.viewmodel.pages.PageListViewModel.PageListType.PUBLISHED
import org.sitebay.android.viewmodel.pages.PageListViewModel.PageListType.SCHEDULED
import org.sitebay.android.viewmodel.pages.PageListViewModel.PageListType.TRASHED
import javax.inject.Inject

class CreatePageListItemActionsUseCase @Inject constructor() {
    fun setupPageActions(
        listType: PageListType,
        uploadUiState: PostUploadUiState,
        siteModel: SiteModel,
        remoteId: Long
    ): Set<Action> {
        return when (listType) {
            SCHEDULED -> mutableSetOf(
                    VIEW_PAGE,
                    SET_PARENT,
                    MOVE_TO_DRAFT,
                    MOVE_TO_TRASH
            ).apply {
                if (canCancelPendingAutoUpload(uploadUiState)) {
                    add(CANCEL_AUTO_UPLOAD)
                }
            }
            PUBLISHED -> {
                mutableSetOf(
                        VIEW_PAGE,
                        COPY,
                        SET_PARENT
                ).apply {
                    if (siteModel.isUsingWpComRestApi &&
                            siteModel.showOnFront == ShowOnFront.PAGE.value &&
                            remoteId > 0) {
                        if (siteModel.pageOnFront != remoteId) {
                            add(SET_AS_HOMEPAGE)
                        }
                        if (siteModel.pageForPosts != remoteId) {
                            add(SET_AS_POSTS_PAGE)
                        }
                    }

                    if (siteModel.pageOnFront != remoteId && listType == PUBLISHED) {
                        add(MOVE_TO_DRAFT)
                        add(MOVE_TO_TRASH)
                    }

                    if (canCancelPendingAutoUpload(uploadUiState)) {
                        add(CANCEL_AUTO_UPLOAD)
                    }
                }
            }
            DRAFTS -> mutableSetOf(VIEW_PAGE, SET_PARENT, PUBLISH_NOW, MOVE_TO_TRASH, COPY).apply {
                if (canCancelPendingAutoUpload(uploadUiState)) {
                    add(CANCEL_AUTO_UPLOAD)
                }
            }
            TRASHED -> setOf(MOVE_TO_DRAFT, DELETE_PERMANENTLY)
        }
    }

    private fun canCancelPendingAutoUpload(uploadUiState: PostUploadUiState) =
            (uploadUiState is UploadWaitingForConnection ||
                    (uploadUiState is UploadFailed && uploadUiState.isEligibleForAutoUpload))
}
