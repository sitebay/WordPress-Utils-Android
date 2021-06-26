package org.sitebay.android.ui.jetpack.scan

import androidx.annotation.StringRes
import org.sitebay.android.R
import org.sitebay.android.fluxc.model.SiteModel
import org.sitebay.android.ui.utils.UiString

sealed class ScanNavigationEvents {
    data class ShowThreatDetails(val siteModel: SiteModel, val threatId: Long) : ScanNavigationEvents()

    class OpenFixThreatsConfirmationDialog(
        val title: UiString,
        val message: UiString,
        val okButtonAction: () -> Unit
    ) : ScanNavigationEvents() {
        @StringRes val positiveButtonLabel: Int = R.string.dialog_button_ok
        @StringRes val negativeButtonLabel: Int = R.string.dialog_button_cancel
    }

    data class ShowContactSupport(val site: SiteModel) : ScanNavigationEvents()

    data class ShowJetpackSettings(val url: String) : ScanNavigationEvents()
}
