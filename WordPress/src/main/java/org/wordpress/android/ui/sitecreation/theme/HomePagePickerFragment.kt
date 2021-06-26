package org.sitebay.android.ui.sitecreation.theme

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import org.sitebay.android.R
import org.sitebay.android.WordPress
import org.sitebay.android.databinding.HomePagePickerFragmentBinding
import org.sitebay.android.ui.PreviewModeSelectorPopup
import org.sitebay.android.ui.layoutpicker.CategoriesAdapter
import org.sitebay.android.ui.layoutpicker.LayoutCategoryAdapter
import org.sitebay.android.ui.layoutpicker.LayoutPickerUiState
import org.sitebay.android.ui.layoutpicker.LayoutPickerViewModel.DesignPreviewAction.Dismiss
import org.sitebay.android.ui.layoutpicker.LayoutPickerViewModel.DesignPreviewAction.Show
import org.sitebay.android.ui.sitecreation.theme.DesignPreviewFragment.Companion.DESIGN_PREVIEW_TAG
import org.sitebay.android.ui.utils.UiHelpers
import org.sitebay.android.util.AniUtils
import org.sitebay.android.util.DisplayUtilsWrapper
import org.sitebay.android.util.ToastUtils
import org.sitebay.android.util.image.ImageManager
import org.sitebay.android.util.setVisible
import org.sitebay.android.viewmodel.observeEvent
import javax.inject.Inject

/**
 * Implements the Home Page Picker UI
 */
@Suppress("TooManyFunctions")
class HomePagePickerFragment : Fragment() {
    @Inject lateinit var imageManager: ImageManager
    @Inject lateinit var displayUtils: DisplayUtilsWrapper
    @Inject internal lateinit var uiHelper: UiHelpers
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: HomePagePickerViewModel
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
        return inflater.inflate(R.layout.home_page_picker_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(HomePagePickerFragmentBinding.bind(view)) {
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

            setupUi()
            setupViewModel()
            setupActionListeners()
            previewModeSelectorPopup = PreviewModeSelectorPopup(
                    requireActivity(),
                    homePagePickerTitlebar.previewTypeSelectorButton
            )
        }
    }

    private fun HomePagePickerFragmentBinding.setupUi() {
        homePagePickerTitlebar.title.visibility = if (isPhoneLandscape()) View.VISIBLE else View.INVISIBLE
        modalLayoutPickerHeaderSection.modalLayoutPickerTitleRow?.header?.setText(R.string.hpp_title)
        modalLayoutPickerHeaderSection.modalLayoutPickerSubtitleRow?.description?.setText(R.string.hpp_subtitle)
    }

    private fun HomePagePickerFragmentBinding.setupViewModel() {
        viewModel = ViewModelProvider(requireActivity(), viewModelFactory)
                .get(HomePagePickerViewModel::class.java)

        viewModel.uiState.observe(viewLifecycleOwner, { uiState ->
            setHeaderVisibility(uiState.isHeaderVisible)
            setDescriptionVisibility(uiState.isDescriptionVisible)
            setContentVisibility(uiState.loadingSkeletonVisible, uiState.errorViewVisible)
            setToolbarVisibility(uiState.isToolbarVisible)
            when (uiState) {
                is LayoutPickerUiState.Loading -> { // Nothing more to do here
                }
                is LayoutPickerUiState.Content -> {
                    (categoriesRecyclerView.adapter as CategoriesAdapter).setData(uiState.categories)
                    (layoutsRecyclerView.adapter as? LayoutCategoryAdapter)?.update(uiState.layoutCategories)
                }
                is LayoutPickerUiState.Error -> {
                    uiState.toast?.let { ToastUtils.showToast(requireContext(), it) }
                }
            }
        })

        viewModel.onPreviewActionPressed.observe(viewLifecycleOwner, { action ->
            activity?.supportFragmentManager?.let { fm ->
                when (action) {
                    is Show -> {
                        val previewFragment = DesignPreviewFragment.newInstance()
                        previewFragment.show(fm, DESIGN_PREVIEW_TAG)
                    }
                    is Dismiss -> {
                        (fm.findFragmentByTag(DESIGN_PREVIEW_TAG) as? DesignPreviewFragment)?.dismiss()
                    }
                }
            }
        })

        viewModel.onThumbnailModeButtonPressed.observe(viewLifecycleOwner, {
            previewModeSelectorPopup.show(viewModel)
        })

        viewModel.onCategorySelectionChanged.observeEvent(viewLifecycleOwner, {
            layoutsRecyclerView.smoothScrollToPosition(0)
        })

        viewModel.start(displayUtils.isTablet())
    }

    private fun HomePagePickerFragmentBinding.setHeaderVisibility(visible: Boolean) {
        uiHelper.fadeInfadeOutViews(
                homePagePickerTitlebar.title,
                modalLayoutPickerHeaderSection.modalLayoutPickerTitleRow?.header,
                visible
        )
    }

    /**
     * Sets the header description visibility
     * @param visible if true the description is visible else invisible
     */
    private fun HomePagePickerFragmentBinding.setDescriptionVisibility(visible: Boolean) {
        modalLayoutPickerHeaderSection.modalLayoutPickerSubtitleRow?.description?.visibility =
                if (visible) View.VISIBLE else View.INVISIBLE
    }

    private fun HomePagePickerFragmentBinding.setContentVisibility(skeleton: Boolean, error: Boolean) {
        modalLayoutPickerCategoriesSkeleton.categoriesSkeleton.setVisible(skeleton)
        categoriesRecyclerView.setVisible(!skeleton && !error)
        modalLayoutPickerLayoutsSkeleton.layoutsSkeleton.setVisible(skeleton)
        layoutsRecyclerView.setVisible(!skeleton && !error)
        errorView.setVisible(error)
    }

    private fun HomePagePickerFragmentBinding.setToolbarVisibility(visible: Boolean) {
        AniUtils.animateBottomBar(homePagePickerBottomToolbar.bottomToolbar, visible)
    }

    private fun HomePagePickerFragmentBinding.setupActionListeners() {
        homePagePickerBottomToolbar.previewButton.setOnClickListener { viewModel.onPreviewTapped() }
        homePagePickerBottomToolbar.chooseButton.setOnClickListener { viewModel.onChooseTapped() }
        homePagePickerTitlebar.skipButton.setOnClickListener { viewModel.onSkippedTapped() }
        errorView.button.setOnClickListener { viewModel.onRetryClicked() }
        homePagePickerTitlebar.backButton.setOnClickListener { viewModel.onBackPressed() }
        homePagePickerTitlebar.previewTypeSelectorButton.setOnClickListener { viewModel.onThumbnailModePressed() }
        setScrollListener()
    }

    private fun HomePagePickerFragmentBinding.setScrollListener() {
        if (isPhoneLandscape()) return // Always visible
        val scrollThreshold = resources.getDimension(R.dimen.picker_header_scroll_snap_threshold).toInt()
        appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
            viewModel.onAppBarOffsetChanged(verticalOffset, scrollThreshold)
        })
        viewModel.onAppBarOffsetChanged(0, scrollThreshold)
    }

    private fun isPhoneLandscape() = displayUtils.isLandscapeBySize() && !displayUtils.isTablet()
}
