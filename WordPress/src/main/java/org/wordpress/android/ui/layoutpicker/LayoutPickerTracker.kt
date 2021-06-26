package org.sitebay.android.ui.layoutpicker

interface LayoutPickerTracker {
    fun trackPreviewModeChanged(mode: String)

    fun trackThumbnailModeTapped(mode: String)

    fun trackPreviewModeTapped(mode: String)

    fun trackPreviewLoading(template: String, mode: String)

    fun trackPreviewLoaded(template: String, mode: String)

    fun trackPreviewViewed(template: String, mode: String)

    fun trackNoNetworkErrorShown(message: String)

    fun trackErrorShown(message: String)

    fun filterSelected(filter: String, selectedFilters: List<String>)

    fun filterDeselected(filter: String, selectedFilters: List<String>)
}
