package org.sitebay.android.ui.jetpack.scan

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import org.sitebay.android.R
import org.sitebay.android.WordPress
import org.sitebay.android.databinding.ScanFragmentBinding
import org.sitebay.android.fluxc.model.SiteModel
import org.sitebay.android.ui.ActivityLauncher
import org.sitebay.android.ui.accounts.HelpActivity.Origin.SCAN_SCREEN_HELP
import org.sitebay.android.ui.jetpack.scan.ScanNavigationEvents.OpenFixThreatsConfirmationDialog
import org.sitebay.android.ui.jetpack.scan.ScanNavigationEvents.ShowContactSupport
import org.sitebay.android.ui.jetpack.scan.ScanNavigationEvents.ShowJetpackSettings
import org.sitebay.android.ui.jetpack.scan.ScanNavigationEvents.ShowThreatDetails
import org.sitebay.android.ui.jetpack.scan.ScanViewModel.UiState.ContentUiState
import org.sitebay.android.ui.jetpack.scan.ScanViewModel.UiState.ErrorUiState
import org.sitebay.android.ui.jetpack.scan.ScanViewModel.UiState.FullScreenLoadingUiState
import org.sitebay.android.ui.jetpack.scan.adapters.HorizontalMarginItemDecoration
import org.sitebay.android.ui.jetpack.scan.adapters.ScanAdapter
import org.sitebay.android.ui.pages.SnackbarMessageHolder
import org.sitebay.android.ui.utils.UiHelpers
import org.sitebay.android.util.image.ImageManager
import org.sitebay.android.viewmodel.observeEvent
import org.sitebay.android.widgets.WPSnackbar
import javax.inject.Inject

class ScanFragment : Fragment(R.layout.scan_fragment) {
    @Inject lateinit var imageManager: ImageManager
    @Inject lateinit var uiHelpers: UiHelpers
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: ScanViewModel
    private var fixThreatsConfirmationDialog: AlertDialog? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(ScanFragmentBinding.bind(view)) {
            initDagger()
            initRecyclerView()
            initViewModel(getSite(savedInstanceState))
        }
    }

    private fun initDagger() {
        (requireActivity().application as WordPress).component()?.inject(this)
    }

    private fun ScanFragmentBinding.initRecyclerView() {
        recyclerView.itemAnimator = null
        recyclerView.addItemDecoration(
                HorizontalMarginItemDecoration(resources.getDimensionPixelSize(R.dimen.margin_extra_large))
        )
        recyclerView.setEmptyView(actionableEmptyView)
        initAdapter()
    }

    private fun ScanFragmentBinding.initAdapter() {
        recyclerView.adapter = ScanAdapter(imageManager, uiHelpers)
    }

    private fun ScanFragmentBinding.initViewModel(site: SiteModel) {
        viewModel = ViewModelProvider(this@ScanFragment, viewModelFactory).get(ScanViewModel::class.java)
        setupObservers()
        viewModel.start(site)
    }

    private fun ScanFragmentBinding.setupObservers() {
        viewModel.uiState.observe(
                viewLifecycleOwner,
                { uiState ->
                    uiHelpers.updateVisibility(progressBar, uiState.loadingVisible)
                    uiHelpers.updateVisibility(recyclerView, uiState.contentVisible)
                    uiHelpers.updateVisibility(actionableEmptyView, uiState.errorVisible)

                    when (uiState) {
                        is ContentUiState -> updateContentLayout(uiState)

                        is FullScreenLoadingUiState -> { // Do Nothing
                        }

                        is ErrorUiState.NoConnection,
                        is ErrorUiState.GenericRequestFailed,
                        is ErrorUiState.ScanRequestFailed -> updateErrorLayout(uiState as ErrorUiState)
                    }
                }
        )

        viewModel.snackbarEvents.observeEvent(viewLifecycleOwner, { it.showSnackbar() })

        viewModel.navigationEvents.observeEvent(
                viewLifecycleOwner,
                { events ->
                    when (events) {
                        is OpenFixThreatsConfirmationDialog -> showFixThreatsConfirmationDialog(events)

                        is ShowThreatDetails -> ActivityLauncher.viewThreatDetails(
                                this@ScanFragment,
                                events.siteModel,
                                events.threatId
                        )

                        is ShowContactSupport ->
                            ActivityLauncher.viewHelpAndSupport(requireContext(), SCAN_SCREEN_HELP, events.site, null)

                        is ShowJetpackSettings -> ActivityLauncher.openUrlExternal(context, events.url)
                    }
                }
        )
    }

    private fun ScanFragmentBinding.updateContentLayout(state: ContentUiState) {
        ((recyclerView.adapter) as ScanAdapter).update(state.items)
    }

    private fun ScanFragmentBinding.updateErrorLayout(state: ErrorUiState) {
        uiHelpers.setTextOrHide(actionableEmptyView.title, state.title)
        uiHelpers.setTextOrHide(actionableEmptyView.subtitle, state.subtitle)
        uiHelpers.setTextOrHide(actionableEmptyView.button, state.buttonText)
        actionableEmptyView.image.setImageResource(state.image)
        actionableEmptyView.button.setOnClickListener { state.action.invoke() }
    }

    private fun SnackbarMessageHolder.showSnackbar() {
        view?.let {
            val snackbar = WPSnackbar.make(
                    it,
                    uiHelpers.getTextOfUiString(requireContext(), message),
                    Snackbar.LENGTH_LONG
            )
            snackbar.show()
        }
    }

    private fun showFixThreatsConfirmationDialog(holder: OpenFixThreatsConfirmationDialog) {
        fixThreatsConfirmationDialog = MaterialAlertDialogBuilder(requireActivity())
                .setTitle(uiHelpers.getTextOfUiString(requireContext(), holder.title))
                .setMessage(uiHelpers.getTextOfUiString(requireContext(), holder.message))
                .setPositiveButton(holder.positiveButtonLabel) { _, _ -> holder.okButtonAction.invoke() }
                .setNegativeButton(holder.negativeButtonLabel) { _, _ -> fixThreatsConfirmationDialog?.dismiss() }
                .setCancelable(true)
                .create()
        fixThreatsConfirmationDialog?.show()
    }

    override fun onPause() {
        super.onPause()
        fixThreatsConfirmationDialog?.dismiss()
    }

    private fun getSite(savedInstanceState: Bundle?): SiteModel {
        return if (savedInstanceState == null) {
            requireActivity().intent.getSerializableExtra(WordPress.SITE) as SiteModel
        } else {
            savedInstanceState.getSerializable(WordPress.SITE) as SiteModel
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putSerializable(WordPress.SITE, viewModel.site)
        super.onSaveInstanceState(outState)
    }

    fun onNewIntent(intent: Intent?) {
        intent?.let {
            val threatId = intent.getLongExtra(ScanActivity.REQUEST_FIX_STATE, 0L)
            val messageRes = intent.getIntExtra(ScanActivity.REQUEST_SCAN_STATE, 0)
            if (threatId > 0L) {
                viewModel.onFixStateRequested(threatId)
            } else if (messageRes > 0) {
                viewModel.onScanStateRequestedWithMessage(messageRes)
            }
        }
    }

    companion object {
        const val ARG_THREAT_ID = "arg_threat_id"
    }
}
