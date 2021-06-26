package org.sitebay.android.ui.mlp

import org.sitebay.android.analytics.AnalyticsTracker
import org.sitebay.android.ui.layoutpicker.LayoutPickerTracker
import org.sitebay.android.ui.mlp.ModalLayoutPickerTracker.PROPERTY.FILTER
import org.sitebay.android.ui.mlp.ModalLayoutPickerTracker.PROPERTY.LOCATION
import org.sitebay.android.ui.mlp.ModalLayoutPickerTracker.PROPERTY.PREVIEW_MODE
import org.sitebay.android.ui.mlp.ModalLayoutPickerTracker.PROPERTY.SELECTED_FILTERS
import org.sitebay.android.ui.mlp.ModalLayoutPickerTracker.PROPERTY.TEMPLATE
import org.sitebay.android.util.analytics.AnalyticsTrackerWrapper
import javax.inject.Inject
import javax.inject.Singleton

private const val LAYOUT_ERROR_CONTEXT = "layout"
private const val PAGE_PICKER_LOCATION = "page_picker"

@Singleton
class ModalLayoutPickerTracker @Inject constructor(val tracker: AnalyticsTrackerWrapper) : LayoutPickerTracker {
    private enum class PROPERTY(val key: String) {
        TEMPLATE("template"),
        PREVIEW_MODE("preview_mode"),
        LOCATION("location"),
        FILTER("filter"),
        SELECTED_FILTERS("selected_filters")
    }

    override fun trackPreviewModeChanged(mode: String) {
        tracker.track(
                AnalyticsTracker.Stat.LAYOUT_PICKER_PREVIEW_MODE_CHANGED,
                mapOf(PREVIEW_MODE.key to mode)
        )
    }

    override fun trackThumbnailModeTapped(mode: String) {
        tracker.track(
                AnalyticsTracker.Stat.LAYOUT_PICKER_THUMBNAIL_MODE_BUTTON_TAPPED,
                mapOf(PREVIEW_MODE.key to mode)
        )
    }

    override fun trackPreviewModeTapped(mode: String) {
        tracker.track(
                AnalyticsTracker.Stat.LAYOUT_PICKER_PREVIEW_MODE_BUTTON_TAPPED,
                mapOf(PREVIEW_MODE.key to mode)
        )
    }

    override fun trackPreviewLoading(template: String, mode: String) {
        tracker.track(
                AnalyticsTracker.Stat.LAYOUT_PICKER_PREVIEW_LOADING,
                mapOf(TEMPLATE.key to template, PREVIEW_MODE.key to mode)
        )
    }

    override fun trackPreviewLoaded(template: String, mode: String) {
        tracker.track(
                AnalyticsTracker.Stat.LAYOUT_PICKER_PREVIEW_LOADED,
                mapOf(TEMPLATE.key to template, PREVIEW_MODE.key to mode)
        )
    }

    override fun trackPreviewViewed(template: String, mode: String) {
        tracker.track(
                AnalyticsTracker.Stat.LAYOUT_PICKER_PREVIEW_VIEWED,
                mapOf(TEMPLATE.key to template, PREVIEW_MODE.key to mode)
        )
    }

    override fun trackNoNetworkErrorShown(message: String) {
        tracker.track(
                AnalyticsTracker.Stat.LAYOUT_PICKER_ERROR_SHOWN,
                LAYOUT_ERROR_CONTEXT,
                "internet_unavailable_error",
                message
        )
    }

    override fun trackErrorShown(message: String) {
        tracker.track(
                AnalyticsTracker.Stat.LAYOUT_PICKER_ERROR_SHOWN,
                LAYOUT_ERROR_CONTEXT,
                "unknown",
                message
        )
    }

    override fun filterSelected(filter: String, selectedFilters: List<String>) {
        tracker.track(
                AnalyticsTracker.Stat.CATEGORY_FILTER_SELECTED,
                mapOf(
                        LOCATION.key to PAGE_PICKER_LOCATION,
                        FILTER.key to filter,
                        SELECTED_FILTERS.key to selectedFilters.joinToString()
                )
        )
    }

    override fun filterDeselected(filter: String, selectedFilters: List<String>) {
        tracker.track(
                AnalyticsTracker.Stat.CATEGORY_FILTER_DESELECTED,
                mapOf(
                        LOCATION.key to PAGE_PICKER_LOCATION,
                        FILTER.key to filter,
                        SELECTED_FILTERS.key to selectedFilters.joinToString()
                )
        )
    }
}
