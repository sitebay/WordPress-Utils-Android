package org.sitebay.android.ui.sitecreation.misc

import org.sitebay.android.ui.utils.UiString

data class SiteCreationSearchInputUiState(
    val hint: UiString,
    val showProgress: Boolean,
    val showClearButton: Boolean,
    val showDivider: Boolean,
    val showKeyboard: Boolean
)
