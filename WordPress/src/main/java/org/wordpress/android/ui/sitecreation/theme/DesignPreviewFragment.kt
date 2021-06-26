package org.sitebay.android.ui.sitecreation.theme

import androidx.lifecycle.ViewModelProvider
import org.sitebay.android.R
import org.sitebay.android.ui.layoutpicker.LayoutPreviewFragment

/**
 * Implements the Home Page Picker Design Preview UI
 */
class DesignPreviewFragment : LayoutPreviewFragment() {
    companion object {
        const val DESIGN_PREVIEW_TAG = "DESIGN_PREVIEW_TAG"

        fun newInstance() = DesignPreviewFragment()
    }

    override fun getViewModel() =
            ViewModelProvider(requireActivity(), viewModelFactory).get(HomePagePickerViewModel::class.java)

    override fun getChooseButtonText() = R.string.hpp_choose_button
}
