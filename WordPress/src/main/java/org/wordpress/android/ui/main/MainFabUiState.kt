package org.sitebay.android.ui.main

import androidx.annotation.StringRes

data class MainFabUiState(
    val isFabVisible: Boolean,
    val isFabTooltipVisible: Boolean,
    @StringRes val CreateContentMessageId: Int,
    val isFocusPointVisible: Boolean = false
)
