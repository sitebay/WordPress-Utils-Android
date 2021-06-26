package org.sitebay.android.ui.reader.views

import org.sitebay.android.ui.reader.views.uistates.FollowButtonUiState

sealed class ReaderTagHeaderViewUiState {
    data class ReaderTagHeaderUiState(
        val title: String,
        val followButtonUiState: FollowButtonUiState
    ) : ReaderTagHeaderViewUiState()
}
