package org.sitebay.android.ui.mlp

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import org.sitebay.android.R
import org.sitebay.android.WordPress
import org.sitebay.android.databinding.ModalLayoutPickerFragmentBinding
import org.sitebay.android.ui.FullscreenBottomSheetDialogFragment
import org.sitebay.android.ui.PreviewModeSelectorPopup
import org.sitebay.android.ui.layoutpicker.ButtonsUiState
import org.sitebay.android.ui.layoutpicker.CategoriesAdapter
import org.sitebay.android.ui.layoutpicker.LayoutCategoryAdapter
import org.sitebay.android.ui.layoutpicker.LayoutPickerUiState.Content
import org.sitebay.android.ui.layoutpicker.LayoutPickerUiState.Error
import org.sitebay.android.ui.layoutpicker.LayoutPickerUiState.Loading
import org.sitebay.android.ui.layoutpicker.LayoutPickerViewModel.DesignPreviewAction.Dismiss
import org.sitebay.android.ui.layoutpicker.LayoutPickerViewModel.DesignPreviewAction.Show
import org.sitebay.android.ui.mlp.BlockLayoutPreviewFragment.Companion.BLOCK_LAYOUT_PREVIEW_TAG
import org.sitebay.android.ui.utils.UiHelpers
import org.sitebay.android.util.DisplayUtils
import org.sitebay.android.util.setVisible
import org.sitebay.android.viewmodel.mlp.ModalLayoutPickerViewModel
import org.sitebay.android.viewmodel.observeEvent
import javax.inject.Inject

/**
 * Implements the Modal Layout Picker UI
 */
@Suppress("TooManyFunctions")
class ModalLayoutPickerFragment : FullscreenBottomSheetDialogFragment() {
    @Inject internal lateinit var uiHelper: UiHelpers
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: ModalLayoutPickerViewModel
    private lateinit var previewModeSelectorPopup: PreviewModeSelectorPopup

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity().applicationContext as WordPress).component().inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.modal_layout_picker_fragment, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(ModalLayoutPickerFragmentBinding.bind(view)) {
            categoriesRecyclerView.apply {
                layoutManager = LinearLayoutManager(
                        context,
                        RecyclerView.HORIZONTAL,
                        false
                )
                setRecycledViewPool(RecyclerView.RecycledViewPool())
                adapter = CategoriesAdapter()
                ViewCompat.setNestedScrollingEnabled(this, false)
            }

            layoutsRecyclerView.apply {
                layoutManager = LinearLayoutManager(requireActivity())
                adapter = LayoutCategoryAdapter()
            }

            modalLayoutPickerTitlebar.backButton.setOnClickListener {
                closeModal()
            }

            modalLayoutPickerBottomToolbar.createBlankPageButton.setOnClickListener {
                viewModel.onCreatePageClicked()
            }
            modalLayoutPickerBottomToolbar.createPageButton.setOnClickListener {
                viewModel.onCreatePageClicked()
            }
            modalLayoutPickerBottomToolbar.previewButton.setOnClickListener {
                viewModel.onPreviewTapped()
            }
            modalLayoutPickerBottomToolbar.retryButton.setOnClickListener {
                viewModel.onRetryClicked()
            }
            modalLayoutPickerTitlebar.previewTypeSelectorButton.setOnClickListener {
                viewModel.onThumbnailModePressed()
            }

            setScrollListener()

            setupViewModel(savedInstanceState)

            previewModeSelectorPopup = PreviewModeSelectorPopup(
                    requireActivity(),
                    modalLayoutPickerTitlebar.previewTypeSelectorButton
            )
        }
    }

    private fun ModalLayoutPickerFragmentBinding.setScrollListener() {
        if (DisplayUtils.isLandscape(requireContext())) return // Always visible
        val scrollThreshold = resources.getDimension(R.dimen.picker_header_scroll_snap_threshold).toInt()
        appBarLayout.addOnOffsetChangedListener(OnOffsetChangedListener { _, verticalOffset ->
            viewModel.onAppBarOffsetChanged(verticalOffset, scrollThreshold)
        })
    }

    private fun ModalLayoutPickerFragmentBinding.setupViewModel(savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(requireActivity(), viewModelFactory)
                .get(ModalLayoutPickerViewModel::class.java)

        viewModel.loadSavedState(savedInstanceState)

        viewModel.uiState.observe(this@ModalLayoutPickerFragment, { uiState ->
            setHeaderVisibility(uiState.isHeaderVisible)
            setDescriptionVisibility(uiState.isDescriptionVisible)
            setButtonsVisibility(uiState.buttonsUiState)
            setContentVisibility(uiState.loadingSkeletonVisible, uiState.errorViewVisible)
            when (uiState) {
                is Loading -> {
                }
                is Content -> {
                    (categoriesRecyclerView.adapter as CategoriesAdapter).setData(uiState.categories)
                    (layoutsRecyclerView.adapter as? LayoutCategoryAdapter)?.update(uiState.layoutCategories)
                }
                is Error -> {
                    uiState.title?.let { modalLayoutPickerError.actionableEmptyView.title.setText(it) }
                    uiState.subtitle?.let { modalLayoutPickerError.actionableEmptyView.subtitle.setText(it) }
                }
            }
        })

        viewModel.onThumbnailModeButtonPressed.observe(viewLifecycleOwner, {
            previewModeSelectorPopup.show(viewModel)
        })

        viewModel.onPreviewActionPressed.observe(viewLifecycleOwner, { action ->
            activity?.supportFragmentManager?.let { fm ->
                when (action) {
                    is Show -> {
                        val previewFragment = BlockLayoutPreviewFragment.newInstance()
                        previewFragment.show(fm, BLOCK_LAYOUT_PREVIEW_TAG)
                    }
                    is Dismiss -> {
                        (fm.findFragmentByTag(BLOCK_LAYOUT_PREVIEW_TAG) as? BlockLayoutPreviewFragment)?.dismiss()
                    }
                }
            }
        })

        viewModel.onCategorySelectionChanged.observeEvent(this@ModalLayoutPickerFragment, {
            layoutsRecyclerView.smoothScrollToPosition(0)
        })
    }

    private fun ModalLayoutPickerFragmentBinding.setHeaderVisibility(visible: Boolean) {
        uiHelper.fadeInfadeOutViews(
                modalLayoutPickerTitlebar.title,
                modalLayoutPickerHeaderSection.modalLayoutPickerTitleRow?.header,
                visible
        )
    }

    /**
     * Sets the header description visibility
     * @param visible if true the description is visible else invisible
     */
    private fun ModalLayoutPickerFragmentBinding.setDescriptionVisibility(visible: Boolean) {
        modalLayoutPickerHeaderSection.modalLayoutPickerSubtitleRow?.description?.visibility =
                if (visible) View.VISIBLE else View.INVISIBLE
    }

    private fun ModalLayoutPickerFragmentBinding.setButtonsVisibility(uiState: ButtonsUiState) {
        modalLayoutPickerBottomToolbar.createBlankPageButton.setVisible(uiState.createBlankPageVisible)
        modalLayoutPickerBottomToolbar.createPageButton.setVisible(uiState.createPageVisible)
        modalLayoutPickerBottomToolbar.previewButton.setVisible(uiState.previewVisible)
        modalLayoutPickerBottomToolbar.retryButton.setVisible(uiState.retryVisible)
        modalLayoutPickerBottomToolbar.createOrRetryContainer.setVisible(
                uiState.createBlankPageVisible || uiState.retryVisible
        )
    }

    private fun ModalLayoutPickerFragmentBinding.setContentVisibility(skeleton: Boolean, error: Boolean) {
        modalLayoutPickerCategoriesSkeleton.categoriesSkeleton.setVisible(skeleton)
        categoriesRecyclerView.setVisible(!skeleton && !error)
        modalLayoutPickerLayoutsSkeleton.layoutsSkeleton.setVisible(skeleton)
        layoutsRecyclerView.setVisible(!skeleton && !error)
        modalLayoutPickerError.errorLayout.setVisible(error)
    }

    override fun closeModal() {
        viewModel.dismiss()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewModel.writeToBundle(outState)
    }

    companion object {
        const val MODAL_LAYOUT_PICKER_TAG = "MODAL_LAYOUT_PICKER_TAG"
    }
}
