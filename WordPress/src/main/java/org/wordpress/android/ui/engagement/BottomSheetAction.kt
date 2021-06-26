package org.sitebay.android.ui.engagement

sealed class BottomSheetAction {
    object ShowBottomSheet : BottomSheetAction()
    object HideBottomSheet : BottomSheetAction()
}
