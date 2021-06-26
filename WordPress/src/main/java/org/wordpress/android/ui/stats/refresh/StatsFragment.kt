package org.sitebay.android.ui.stats.refresh

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayout.Tab
import dagger.android.support.DaggerFragment
import org.sitebay.android.R
import org.sitebay.android.WordPress
import org.sitebay.android.databinding.StatsFragmentBinding
import org.sitebay.android.ui.ScrollableViewInitializedListener
import org.sitebay.android.ui.pages.SnackbarMessageHolder
import org.sitebay.android.ui.stats.refresh.lists.StatsListFragment
import org.sitebay.android.ui.stats.refresh.lists.StatsListViewModel.StatsSection
import org.sitebay.android.ui.stats.refresh.lists.StatsListViewModel.StatsSection.ANNUAL_STATS
import org.sitebay.android.ui.stats.refresh.lists.StatsListViewModel.StatsSection.DAYS
import org.sitebay.android.ui.stats.refresh.lists.StatsListViewModel.StatsSection.DETAIL
import org.sitebay.android.ui.stats.refresh.lists.StatsListViewModel.StatsSection.INSIGHTS
import org.sitebay.android.ui.stats.refresh.lists.StatsListViewModel.StatsSection.MONTHS
import org.sitebay.android.ui.stats.refresh.lists.StatsListViewModel.StatsSection.WEEKS
import org.sitebay.android.ui.stats.refresh.lists.StatsListViewModel.StatsSection.YEARS
import org.sitebay.android.ui.stats.refresh.utils.StatsSiteProvider.SiteUpdateResult
import org.sitebay.android.ui.utils.UiHelpers
import org.sitebay.android.util.WPSwipeToRefreshHelper
import org.sitebay.android.util.helpers.SwipeToRefreshHelper
import org.sitebay.android.viewmodel.observeEvent
import org.sitebay.android.widgets.WPSnackbar
import javax.inject.Inject

private val statsSections = listOf(INSIGHTS, DAYS, WEEKS, MONTHS, YEARS)

class StatsFragment : DaggerFragment(R.layout.stats_fragment), ScrollableViewInitializedListener {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var uiHelpers: UiHelpers
    private lateinit var viewModel: StatsViewModel
    private lateinit var swipeToRefreshHelper: SwipeToRefreshHelper
    private val selectedTabListener: SelectedTabListener
        get() = SelectedTabListener(viewModel)

    private var restorePreviousSearch = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val nonNullActivity = requireActivity()
        with(StatsFragmentBinding.bind(view)) {
            with(nonNullActivity as AppCompatActivity) {
                setSupportActionBar(toolbar)
                supportActionBar?.let {
                    it.setHomeButtonEnabled(true)
                    it.setDisplayHomeAsUpEnabled(true)
                }
            }
            initializeViewModels(nonNullActivity, savedInstanceState == null, savedInstanceState)
            initializeViews(nonNullActivity)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(WordPress.LOCAL_SITE_ID, activity?.intent?.getIntExtra(WordPress.LOCAL_SITE_ID, 0) ?: 0)
        viewModel.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    private fun StatsFragmentBinding.initializeViews(activity: FragmentActivity) {
        statsPager.adapter = StatsPagerAdapter(activity, childFragmentManager)
        tabLayout.setupWithViewPager(statsPager)
        statsPager.pageMargin = resources.getDimensionPixelSize(R.dimen.margin_extra_large)
        tabLayout.addOnTabSelectedListener(selectedTabListener)

        swipeToRefreshHelper = WPSwipeToRefreshHelper.buildSwipeToRefreshHelper(pullToRefresh) {
            viewModel.onPullToRefresh()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun StatsFragmentBinding.initializeViewModels(
        activity: FragmentActivity,
        isFirstStart: Boolean,
        savedInstanceState: Bundle?
    ) {
        viewModel = ViewModelProvider(activity, viewModelFactory).get(StatsViewModel::class.java)

        viewModel.onRestoreInstanceState(savedInstanceState)

        setupObservers(activity)

        viewModel.start(activity.intent)

        if (!isFirstStart) {
            restorePreviousSearch = true
        }

        statsPager.setOnTouchListener { _, event ->
            swipeToRefreshHelper.setEnabled(false)
            if (event.action == MotionEvent.ACTION_UP) {
                swipeToRefreshHelper.setEnabled(true)
            }
            return@setOnTouchListener false
        }
    }

    private fun StatsFragmentBinding.setupObservers(activity: FragmentActivity) {
        viewModel.isRefreshing.observe(viewLifecycleOwner, {
            it?.let { isRefreshing ->
                swipeToRefreshHelper.isRefreshing = isRefreshing
            }
        })

        viewModel.showSnackbarMessage.observe(viewLifecycleOwner, { holder ->
            showSnackbar(activity, holder)
        })

        viewModel.toolbarHasShadow.observe(viewLifecycleOwner, { hasShadow ->
            appBarLayout.showShadow(hasShadow == true)
        })

        viewModel.siteChanged.observeEvent(viewLifecycleOwner, { siteUpdateResult ->
            when (siteUpdateResult) {
                is SiteUpdateResult.SiteConnected -> viewModel.onSiteChanged()
                is SiteUpdateResult.NotConnectedJetpackSite -> getActivity()?.finish()
            }
        })

        viewModel.hideToolbar.observeEvent(viewLifecycleOwner, { hideToolbar ->
            appBarLayout.setExpanded(!hideToolbar, true)
        })

        viewModel.selectedSection.observe(viewLifecycleOwner, { selectedSection ->
            selectedSection?.let {
                handleSelectedSection(selectedSection)
            }
        })
    }

    private fun StatsFragmentBinding.handleSelectedSection(
        selectedSection: StatsSection
    ) {
        val position = when (selectedSection) {
            INSIGHTS -> 0
            DAYS -> 1
            WEEKS -> 2
            MONTHS -> 3
            YEARS -> 4
            DETAIL -> null
            ANNUAL_STATS -> null
        }
        position?.let {
            if (statsPager.currentItem != position) {
                tabLayout.removeOnTabSelectedListener(selectedTabListener)
                statsPager.setCurrentItem(position, false)
                tabLayout.addOnTabSelectedListener(selectedTabListener)
            }
        }
    }

    private fun showSnackbar(
        activity: FragmentActivity,
        holder: SnackbarMessageHolder?
    ) {
        val parent = activity.findViewById<View>(R.id.coordinatorLayout)
        if (holder != null && parent != null) {
            if (holder.buttonTitle == null) {
                WPSnackbar.make(
                        parent,
                        uiHelpers.getTextOfUiString(requireContext(), holder.message),
                        Snackbar.LENGTH_LONG
                ).show()
            } else {
                val snackbar = WPSnackbar.make(
                        parent,
                        uiHelpers.getTextOfUiString(requireContext(), holder.message),
                        Snackbar.LENGTH_LONG
                )
                snackbar.setAction(uiHelpers.getTextOfUiString(requireContext(), holder.buttonTitle)) {
                    holder.buttonAction()
                }
                snackbar.show()
            }
        }
    }

    override fun onScrollableViewInitialized(containerId: Int) {
        StatsFragmentBinding.bind(requireView()).appBarLayout.liftOnScrollTargetViewId = containerId
    }
}

class StatsPagerAdapter(val context: Context, val fm: FragmentManager) : FragmentPagerAdapter(
        fm,
        BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
) {
    override fun getCount(): Int = statsSections.size

    override fun getItem(position: Int): Fragment {
        return StatsListFragment.newInstance(statsSections[position])
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return context.getString(statsSections[position].titleRes)
    }
}

private class SelectedTabListener(val viewModel: StatsViewModel) : OnTabSelectedListener {
    override fun onTabReselected(tab: Tab?) {
    }

    override fun onTabUnselected(tab: Tab?) {
    }

    override fun onTabSelected(tab: Tab) {
        viewModel.onSectionSelected(statsSections[tab.position])
    }
}
