package org.sitebay.android.ui.pages

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.sitebay.android.R
import org.sitebay.android.WordPress
import org.sitebay.android.databinding.PagesListFragmentBinding
import org.sitebay.android.fluxc.Dispatcher
import org.sitebay.android.ui.ViewPagerFragment
import org.sitebay.android.ui.quickstart.QuickStartEvent
import org.sitebay.android.ui.utils.UiHelpers
import org.sitebay.android.util.DisplayUtils
import org.sitebay.android.util.QuickStartUtilsWrapper
import org.sitebay.android.util.image.ImageManager
import org.sitebay.android.viewmodel.pages.PageListViewModel
import org.sitebay.android.viewmodel.pages.PageListViewModel.PageListType
import org.sitebay.android.viewmodel.pages.PagesViewModel
import org.sitebay.android.widgets.RecyclerItemDecoration
import org.sitebay.android.widgets.WPDialogSnackbar
import javax.inject.Inject

class PageListFragment : ViewPagerFragment(R.layout.pages_list_fragment) {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject internal lateinit var imageManager: ImageManager
    @Inject internal lateinit var uiHelper: UiHelpers
    @Inject lateinit var dispatcher: Dispatcher
    @Inject lateinit var quickStartUtilsWrapper: QuickStartUtilsWrapper
    private lateinit var viewModel: PageListViewModel
    private var linearLayoutManager: LinearLayoutManager? = null
    private var snackbar: WPDialogSnackbar? = null
    private var binding: PagesListFragmentBinding? = null

    private val listStateKey = "list_state"

    companion object {
        private const val typeKey = "type_key"

        fun newInstance(listType: PageListType): PageListFragment {
            val fragment = PageListFragment()
            val bundle = Bundle()
            bundle.putSerializable(typeKey, listType)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun getScrollableViewForUniqueIdProvision(): View? {
        return binding?.recyclerView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val nonNullActivity = requireActivity()
        (nonNullActivity.application as? WordPress)?.component()?.inject(this)
        with(PagesListFragmentBinding.bind(view)) {
            initializeViews(savedInstanceState)
            initializeViewModels(nonNullActivity)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        linearLayoutManager?.let {
            outState.putParcelable(listStateKey, it.onSaveInstanceState())
        }
        super.onSaveInstanceState(outState)
    }

    private fun PagesListFragmentBinding.initializeViewModels(activity: FragmentActivity) {
        val pagesViewModel = ViewModelProvider(activity, viewModelFactory).get(PagesViewModel::class.java)

        val listType = arguments?.getSerializable(typeKey) as PageListType
        viewModel = ViewModelProvider(this@PageListFragment, viewModelFactory)
                .get(listType.name, PageListViewModel::class.java)

        viewModel.start(listType, pagesViewModel)

        setupObservers()
    }

    private fun PagesListFragmentBinding.initializeViews(savedInstanceState: Bundle?) {
        val layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        savedInstanceState?.getParcelable<Parcelable>(listStateKey)?.let {
            layoutManager.onRestoreInstanceState(it)
        }

        linearLayoutManager = layoutManager
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.addItemDecoration(RecyclerItemDecoration(0, DisplayUtils.dpToPx(activity, 1)))
    }

    private fun PagesListFragmentBinding.setupObservers() {
        viewModel.pages.observe(viewLifecycleOwner, Observer { data ->
            data?.let { setPages(data.first, data.second, data.third) }
        })

        viewModel.scrollToPosition.observe(viewLifecycleOwner, Observer { position ->
            position?.let {
                val smoothScroller = object : LinearSmoothScroller(context) {
                    override fun getVerticalSnapPreference(): Int {
                        return SNAP_TO_START
                    }
                }.apply { targetPosition = position }
                recyclerView.layoutManager?.startSmoothScroll(smoothScroller)
            }
        })

        viewModel.quickStartEvent.observe(viewLifecycleOwner, Observer { event ->
            if (event == null) {
                snackbar?.dismiss()
                snackbar = null
            } else {
                showSnackbar()
            }
        })
    }

    private fun PagesListFragmentBinding.setPages(
        pages: List<PageItem>,
        isSitePhotonCapable: Boolean,
        isSitePrivateAt: Boolean
    ) {
        val adapter: PageListAdapter
        if (recyclerView.adapter == null) {
            adapter = PageListAdapter(
                    { action, page -> viewModel.onMenuAction(action, page, requireContext()) },
                    { page -> viewModel.onItemTapped(page, requireContext()) },
                    { viewModel.onEmptyListNewPageButtonTapped() },
                    isSitePhotonCapable,
                    isSitePrivateAt,
                    imageManager,
                    uiHelper
            )
            recyclerView.adapter = adapter
        } else {
            adapter = recyclerView.adapter as PageListAdapter
        }
        adapter.update(pages)
    }

    fun scrollToPage(localPageId: Int) {
        viewModel.onScrollToPageRequested(localPageId)
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    fun onEvent(event: QuickStartEvent) {
        if (!isAdded || view == null) {
            return
        }

        EventBus.getDefault().removeStickyEvent(event)
        viewModel.onQuickStartEvent(event)
    }

    fun showSnackbar() {
        view?.post {
            val title = quickStartUtilsWrapper.stylizeQuickStartPrompt(
                    requireContext(),
                    R.string.quick_start_dialog_edit_homepage_message_pages_short,
                    R.drawable.ic_homepage_16dp
            )

            snackbar = WPDialogSnackbar.make(
                    requireView().findViewById(R.id.page_list_layout), title,
                    resources.getInteger(R.integer.quick_start_snackbar_duration_ms)
            )
            snackbar?.show()
        }
    }
}
