package org.sitebay.android.editor.gutenberg

import androidx.lifecycle.LiveData

enum class DialogVisibility { Showing, Hidden }

interface DialogVisibilityProvider {
    val savingInProgressDialogVisibility: LiveData<DialogVisibility>
}
