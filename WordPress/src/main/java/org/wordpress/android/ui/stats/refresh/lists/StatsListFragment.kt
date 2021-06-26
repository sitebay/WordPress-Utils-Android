package org.sitebay.android.ui.stats.refresh.lists

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import org.sitebay.android.R
import org.sitebay.android.WordPress
import org.sitebay.android.databinding.StatsListFragmentBinding
import org.sitebay.android.ui.ViewPagerFragment
import org.sitebay.android.ui.stats.refresh.lists.StatsListViewModel.StatsSection
import org.sitebay.android.ui.stats.refresh.lists.StatsListViewModel.UiModel
import org.sitebay.android.ui.stats.refresh.lists.StatsListViewModel.UiModel.Empty
import org.sitebay.android.ui.stats.refresh.lists.StatsListViewModel.UiModel.Error
import org.sitebay.android.ui.stats.refresh.lists.StatsListViewModel.UiModel.Success
import org.sitebay.android.ui.stats.refresh.lists.detail.DetailListViewModel
import org.sitebay.android.ui.stats.refresh.utils.StatsDateFormatter
import org.sitebay.android.ui.stats.refresh.utils.StatsNavigator
import org.sitebay.android.ui.stats.refresh.utils.drawDateSelector
import org.sitebay.android.util.image.ImageManager
import org.sitebay.android.util.setVisible
import org.sitebay.android.viewmodel.observeEvent
import javax.inject.Inject

class StatsListFragment : ViewPagerFragment(R.layout.stats_list_fragment) {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var imageManager: ImageManager
    @Inject lateinit var statsDateFormatter: StatsDateFormatter
    @Inject lateinit var navigator: StatsNavigator
    private lateinit var viewModel: StatsListViewModel

    private var layoutManager: LayoutManager? = null
    private var binding: StatsListFragmentBinding? = null

    private val listStateKey = "list_state"

    companion object {
        const val LIST_TYPE = "type_key"

        fun newInstance(section: StatsSection): StatsListFragment {
            val fragment = StatsListFragment()
            val bundle = Bundle()
            bundle.putSerializable(LIST_TYPE, section)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity().application as WordPress).component().inject(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        layoutManager?.let {
            outState.putParcelable(listStateKey, it.onSaveInstanceState())
        }
        (activity?.intent?.getSerializableExtra(LIST_TYPE) as? StatsSection)?.let { sectionFromIntent ->
            outState.putSerializable(LIST_TYPE, sectionFromIntent)
        }
        super.onSaveInstanceState(outState)
    }

    private fun StatsListFragmentBinding.initializeViews(savedInstanceState: Bundle?) {
        val columns = resources.getInteger(R.integer.stats_number_of_columns)
        val layoutManager: LayoutManager = if (columns == 1) {
            LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        } else {
            StaggeredGridLayoutManager(columns, StaggeredGridLayoutManager.VERTICAL)
        }
        savedInstanceState?.getParcelable<Parcelable>(listStateKey)?.let {
            layoutManager.onRestoreInstanceState(it)
        }

        this@StatsListFragment.layoutManager = layoutManager
        recyclerView.layoutManager = this@StatsListFragment.layoutManager
        recyclerView.addItemDecoration(
                StatsListItemDecoration(
                        resources.getDimensionPixelSize(R.dimen.stats_list_card_horizontal_spacing),
                        resources.getDimensionPixelSize(R.dimen.stats_list_card_top_spacing),
                        resources.getDimensionPixelSize(R.dimen.stats_list_card_bottom_spacing),
                        resources.getDimensionPixelSize(R.dimen.stats_list_card_first_spacing),
                        resources.getDimensionPixelSize(R.dimen.stats_list_card_last_spacing),
                        columns
                )
        )

        emptyView.statsEmptyView.button.setOnClickListener {
            viewModel.onEmptyInsightsButtonClicked()
        }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!recyclerView.canScrollVertically(1) && dy != 0) {
                    viewModel.onScrolledToBottom()
                }
            }
        })

        dateSelector.nextDateButton.setOnClickListener {
            viewModel.onNextDateSelected()
        }

        dateSelector.previousDateButton.setOnClickListener {
            viewModel.onPreviousDateSelected()
        }

        errorView.statsErrorView.button.setOnClickListener {
            viewModel.onRetryClick()
        }
    }

    override fun getScrollableViewForUniqueIdProvision(): View {
        return binding!!.recyclerView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val nonNullActivity = requireActivity()
        with(StatsListFragmentBinding.bind(view)) {
            binding = this
            initializeViews(savedInstanceState)
            initializeViewModels(nonNullActivity)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun StatsListFragmentBinding.initializeViewModels(activity: FragmentActivity) {
        val statsSection = arguments?.getSerializable(LIST_TYPE) as? StatsSection
                ?: activity.intent?.getSerializableExtra(LIST_TYPE) as? StatsSection
                ?: StatsSection.INSIGHTS

        val viewModelClass = when (statsSection) {
            StatsSection.DETAIL -> DetailListViewModel::class.java
            StatsSection.ANNUAL_STATS,
            StatsSection.INSIGHTS -> InsightsListViewModel::class.java
            StatsSection.DAYS -> DaysListViewModel::class.java
            StatsSection.WEEKS -> WeeksListViewModel::class.java
            StatsSection.MONTHS -> MonthsListViewModel::class.java
            StatsSection.YEARS -> YearsListViewModel::class.java
        }

        viewModel = ViewModelProvider(this@StatsListFragment, viewModelFactory)
                .get(statsSection.name, viewModelClass)

        setupObservers(activity)
        viewModel.start()
    }

    private fun StatsListFragmentBinding.setupObservers(activity: FragmentActivity) {
        viewModel.uiModel.observe(viewLifecycleOwner, {
            showUiModel(it)
        })

        viewModel.dateSelectorData.observe(viewLifecycleOwner, { dateSelectorUiModel ->
            drawDateSelector(dateSelectorUiModel)
        })

        viewModel.navigationTarget.observeEvent(viewLifecycleOwner, { target ->
            navigator.navigate(activity, target)
        })

        viewModel.selectedDate.observe(viewLifecycleOwner, { event ->
            if (event != null) {
                viewModel.onDateChanged(event.selectedSection)
            }
        })

        viewModel.listSelected.observe(viewLifecycleOwner, {
            viewModel.onListSelected()
        })

        viewModel.typesChanged.observeEvent(viewLifecycleOwner, {
            viewModel.onTypesChanged()
        })

        viewModel.scrollTo?.observeEvent(viewLifecycleOwner, { statsType ->
            (recyclerView.adapter as? StatsBlockAdapter)?.let { adapter ->
                recyclerView.smoothScrollToPosition(adapter.positionOf(statsType))
            }
        })
    }

    private fun StatsListFragmentBinding.showUiModel(
        it: UiModel?
    ) {
        when (it) {
            is Success -> {
                updateInsights(it.data)
            }
            is Error, null -> {
                recyclerView.visibility = View.GONE
                errorView.statsErrorView.visibility = View.VISIBLE
                emptyView.statsEmptyView.visibility = View.GONE
            }
            is Empty -> {
                recyclerView.visibility = View.GONE
                emptyView.statsEmptyView.visibility = View.VISIBLE
                errorView.statsErrorView.visibility = View.GONE
                emptyView.statsEmptyView.title.setText(it.title)
                if (it.subtitle != null) {
                    emptyView.statsEmptyView.subtitle.setText(it.subtitle)
                } else {
                    emptyView.statsEmptyView.subtitle.text = ""
                }
                if (it.image != null) {
                    emptyView.statsEmptyView.image.setImageResource(it.image)
                } else {
                    emptyView.statsEmptyView.image.setImageDrawable(null)
                }
                emptyView.statsEmptyView.button.setVisible(it.showButton)
            }
        }
    }

    private fun StatsListFragmentBinding.updateInsights(statsState: List<StatsBlock>) {
        recyclerView.visibility = View.VISIBLE
        errorView.statsErrorView.visibility = View.GONE
        emptyView.statsEmptyView.visibility = View.GONE

        val adapter: StatsBlockAdapter
        if (recyclerView.adapter == null) {
            adapter = StatsBlockAdapter(imageManager)
            recyclerView.adapter = adapter
        } else {
            adapter = recyclerView.adapter as StatsBlockAdapter
        }

        val layoutManager = recyclerView.layoutManager
        val recyclerViewState = layoutManager?.onSaveInstanceState()
        adapter.update(statsState)
        layoutManager?.onRestoreInstanceState(recyclerViewState)
    }
}
