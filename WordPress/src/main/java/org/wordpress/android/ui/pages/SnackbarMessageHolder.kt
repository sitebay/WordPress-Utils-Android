package org.sitebay.android.ui.pages

import org.sitebay.android.ui.utils.UiString

data class SnackbarMessageHolder(
    val message: UiString,
    val buttonTitle: UiString? = null,
    val buttonAction: () -> Unit = {},
    val onDismissAction: () -> Unit = {}
)
