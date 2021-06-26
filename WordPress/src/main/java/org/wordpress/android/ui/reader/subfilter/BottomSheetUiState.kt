package org.sitebay.android.ui.reader.subfilter

import org.sitebay.android.ui.utils.UiString

sealed class BottomSheetUiState(val isVisible: Boolean) {
    data class BottomSheetVisible(
        val title: UiString,
        val categories: List<SubfilterCategory>
    ) : BottomSheetUiState(true)
    object BottomSheetHidden : BottomSheetUiState(false)
}
