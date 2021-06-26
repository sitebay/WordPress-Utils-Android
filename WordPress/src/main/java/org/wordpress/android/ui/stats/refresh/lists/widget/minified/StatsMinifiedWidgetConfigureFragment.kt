package org.sitebay.android.ui.stats.refresh.lists.widget.minified

import android.app.Activity.RESULT_OK
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import dagger.android.support.DaggerFragment
import org.sitebay.android.R
import org.sitebay.android.analytics.AnalyticsTracker.Stat.STATS_WIDGET_ADDED
import org.sitebay.android.databinding.StatsWidgetConfigureFragmentBinding
import org.sitebay.android.fluxc.store.SiteStore
import org.sitebay.android.ui.prefs.AppPrefsWrapper
import org.sitebay.android.ui.stats.refresh.lists.widget.configuration.StatsColorSelectionViewModel
import org.sitebay.android.ui.stats.refresh.lists.widget.configuration.StatsDataTypeSelectionViewModel
import org.sitebay.android.ui.stats.refresh.lists.widget.configuration.StatsSiteSelectionViewModel
import org.sitebay.android.ui.stats.refresh.lists.widget.configuration.StatsWidgetColorSelectionDialogFragment
import org.sitebay.android.ui.stats.refresh.lists.widget.configuration.StatsWidgetDataTypeSelectionDialogFragment
import org.sitebay.android.ui.stats.refresh.lists.widget.configuration.StatsWidgetSiteSelectionDialogFragment
import org.sitebay.android.ui.stats.refresh.utils.trackMinifiedWidget
import org.sitebay.android.util.ToastUtils
import org.sitebay.android.util.analytics.AnalyticsTrackerWrapper
import org.sitebay.android.util.image.ImageManager
import org.sitebay.android.util.mergeNotNull
import org.sitebay.android.viewmodel.observeEvent
import javax.inject.Inject

class StatsMinifiedWidgetConfigureFragment : DaggerFragment(R.layout.stats_widget_configure_fragment) {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var minifiedWidgetUpdater: MinifiedWidgetUpdater
    @Inject lateinit var appPrefsWrapper: AppPrefsWrapper
    @Inject lateinit var siteStore: SiteStore
    @Inject lateinit var imageManager: ImageManager
    @Inject lateinit var analyticsTrackerWrapper: AnalyticsTrackerWrapper
    private lateinit var viewModel: StatsMinifiedWidgetConfigureViewModel
    private lateinit var siteSelectionViewModel: StatsSiteSelectionViewModel
    private lateinit var colorSelectionViewModel: StatsColorSelectionViewModel
    private lateinit var dataTypeSelectionViewModel: StatsDataTypeSelectionViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val nonNullActivity = requireActivity()
        viewModel = ViewModelProvider(nonNullActivity, viewModelFactory)
                .get(StatsMinifiedWidgetConfigureViewModel::class.java)
        siteSelectionViewModel = ViewModelProvider(nonNullActivity, viewModelFactory)
                .get(StatsSiteSelectionViewModel::class.java)
        colorSelectionViewModel = ViewModelProvider(nonNullActivity, viewModelFactory)
                .get(StatsColorSelectionViewModel::class.java)
        dataTypeSelectionViewModel = ViewModelProvider(nonNullActivity, viewModelFactory)
                .get(StatsDataTypeSelectionViewModel::class.java)
        nonNullActivity.setResult(AppCompatActivity.RESULT_CANCELED)

        val appWidgetId = nonNullActivity.intent?.extras?.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            nonNullActivity.finish()
            return
        }

        viewModel.start(appWidgetId, siteSelectionViewModel, colorSelectionViewModel, dataTypeSelectionViewModel)
        with(StatsWidgetConfigureFragmentBinding.bind(view)) {
            siteContainer.setOnClickListener {
                siteSelectionViewModel.openSiteDialog()
            }
            colorContainer.setOnClickListener {
                colorSelectionViewModel.openColorDialog()
            }
            dataTypeContainer.visibility = View.VISIBLE
            dataTypeContainer.setOnClickListener {
                dataTypeSelectionViewModel.openDataTypeDialog()
            }

            addWidgetButton.setOnClickListener {
                viewModel.addWidget()
            }

            siteSelectionViewModel.dialogOpened.observeEvent(viewLifecycleOwner, {
                StatsWidgetSiteSelectionDialogFragment().show(requireFragmentManager(), "stats_site_selection_fragment")
            })

            colorSelectionViewModel.dialogOpened.observeEvent(viewLifecycleOwner, {
                StatsWidgetColorSelectionDialogFragment().show(
                        requireFragmentManager(),
                        "stats_view_mode_selection_fragment"
                )
            })

            dataTypeSelectionViewModel.dialogOpened.observeEvent(viewLifecycleOwner, {
                StatsWidgetDataTypeSelectionDialogFragment().show(
                        requireFragmentManager(),
                        "stats_data_type_selection_fragment"
                )
            })

            mergeNotNull(
                    siteSelectionViewModel.notification,
                    colorSelectionViewModel.notification,
                    dataTypeSelectionViewModel.notification
            ).observeEvent(
                    viewLifecycleOwner,
                    {
                        ToastUtils.showToast(activity, it)
                    })

            viewModel.settingsModel.observe(viewLifecycleOwner, { uiModel ->
                uiModel?.let {
                    if (uiModel.siteTitle != null) {
                        siteValue.text = uiModel.siteTitle
                    }
                    colorValue.setText(uiModel.color.title)
                    dataTypeValue.setText(uiModel.dataType.title)
                    addWidgetButton.isEnabled = uiModel.buttonEnabled
                }
            })
        }
        viewModel.widgetAdded.observeEvent(viewLifecycleOwner, {
            analyticsTrackerWrapper.trackMinifiedWidget(STATS_WIDGET_ADDED)
            minifiedWidgetUpdater.updateAppWidget(requireContext(), appWidgetId = appWidgetId)
            val resultValue = Intent()
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            activity?.setResult(RESULT_OK, resultValue)
            activity?.finish()
        })
    }
}
