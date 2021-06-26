package org.sitebay.android.ui.jetpack.scan.history

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import org.sitebay.android.R
import org.sitebay.android.WordPress
import org.sitebay.android.databinding.ScanHistoryListFragmentBinding
import org.sitebay.android.fluxc.model.SiteModel
import org.sitebay.android.ui.ActivityLauncher
import org.sitebay.android.ui.ViewPagerFragment
import org.sitebay.android.ui.jetpack.scan.ScanListItemState
import org.sitebay.android.ui.jetpack.scan.adapters.ScanAdapter
import org.sitebay.android.ui.jetpack.scan.history.ScanHistoryListViewModel.ScanHistoryUiState.ContentUiState
import org.sitebay.android.ui.jetpack.scan.history.ScanHistoryListViewModel.ScanHistoryUiState.EmptyUiState.EmptyHistory
import org.sitebay.android.ui.jetpack.scan.history.ScanHistoryViewModel.ScanHistoryTabType
import org.sitebay.android.ui.utils.UiHelpers
import org.sitebay.android.util.image.ImageManager
import org.sitebay.android.viewmodel.observeEvent
import javax.inject.Inject

class ScanHistoryListFragment : ViewPagerFragment(R.layout.scan_history_list_fragment) {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var imageManager: ImageManager
    @Inject lateinit var uiHelpers: UiHelpers

    private lateinit var viewModel: ScanHistoryListViewModel
    private lateinit var parentViewModel: ScanHistoryViewModel

    private var binding: ScanHistoryListFragmentBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = ScanHistoryListFragmentBinding.bind(view).apply {
            initDagger()
            initRecyclerView()
            initViewModel(getSite(savedInstanceState), getTabType())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun initDagger() {
        (requireActivity().application as WordPress).component()?.inject(this)
    }

    private fun ScanHistoryListFragmentBinding.initRecyclerView() {
        recyclerView.adapter = ScanAdapter(imageManager, uiHelpers)
        recyclerView.itemAnimator = null
    }

    private fun ScanHistoryListFragmentBinding.initViewModel(site: SiteModel, tabType: ScanHistoryTabType) {
        viewModel = ViewModelProvider(
                this@ScanHistoryListFragment,
                viewModelFactory
        ).get(ScanHistoryListViewModel::class.java)
        parentViewModel = ViewModelProvider(parentFragment as ViewModelStoreOwner, viewModelFactory).get(
                ScanHistoryViewModel::class.java
        )
        viewModel.start(tabType, site, parentViewModel)
        setupObservers()
    }

    private fun ScanHistoryListFragmentBinding.setupObservers() {
        viewModel.uiState.observe(viewLifecycleOwner, {
            uiHelpers.updateVisibility(actionableEmptyView, it.emptyVisibility)
            uiHelpers.updateVisibility(recyclerView, it.contentVisibility)
            uiHelpers.updateVisibility(actionableEmptyView.button, false)
            when (it) {
                EmptyHistory -> { // no-op
                }
                is ContentUiState -> refreshContentScreen(it.items)
            }
        })
        viewModel.navigation.observeEvent(viewLifecycleOwner, {
            ActivityLauncher.viewThreatDetails(this@ScanHistoryListFragment, it.siteModel, it.threatId)
        })
    }

    private fun ScanHistoryListFragmentBinding.refreshContentScreen(items: List<ScanListItemState>) {
        ((recyclerView.adapter) as ScanAdapter).update(items)
    }

    private fun getSite(savedInstanceState: Bundle?): SiteModel {
        return if (savedInstanceState == null) {
            requireActivity().intent.getSerializableExtra(WordPress.SITE) as SiteModel
        } else {
            savedInstanceState.getSerializable(WordPress.SITE) as SiteModel
        }
    }

    private fun getTabType(): ScanHistoryTabType = requireNotNull(arguments?.getParcelable(ARG_TAB_TYPE))

    override fun getScrollableViewForUniqueIdProvision(): View? = binding?.recyclerView

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putSerializable(WordPress.SITE, viewModel.site)
        super.onSaveInstanceState(outState)
    }

    companion object {
        private const val ARG_TAB_TYPE = "arg_tab_type"

        fun newInstance(tabType: ScanHistoryTabType): ScanHistoryListFragment {
            val newBundle = Bundle().apply {
                putParcelable(ARG_TAB_TYPE, tabType)
            }
            return ScanHistoryListFragment().apply { arguments = newBundle }
        }
    }
}
