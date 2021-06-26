package org.sitebay.android.ui.pages

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.sitebay.android.R
import org.sitebay.android.WordPress
import org.sitebay.android.databinding.PagesListFragmentBinding
import org.sitebay.android.ui.utils.UiHelpers
import org.sitebay.android.util.DisplayUtils
import org.sitebay.android.viewmodel.pages.PagesViewModel
import org.sitebay.android.viewmodel.pages.SearchListViewModel
import org.sitebay.android.widgets.RecyclerItemDecoration
import javax.inject.Inject

class SearchListFragment : Fragment(R.layout.pages_list_fragment) {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: SearchListViewModel
    private var linearLayoutManager: LinearLayoutManager? = null
    @Inject lateinit var uiHelper: UiHelpers

    private val listStateKey = "list_state"

    companion object {
        fun newInstance(): SearchListFragment {
            return SearchListFragment()
        }
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

    override fun onSaveInstanceState(outState: Bundle) {
        linearLayoutManager?.let {
            outState.putParcelable(listStateKey, it.onSaveInstanceState())
        }
        super.onSaveInstanceState(outState)
    }

    private fun PagesListFragmentBinding.initializeViewModels(activity: FragmentActivity) {
        val pagesViewModel = ViewModelProvider(activity, viewModelFactory).get(PagesViewModel::class.java)

        viewModel = ViewModelProvider(this@SearchListFragment, viewModelFactory).get(SearchListViewModel::class.java)
        viewModel.start(pagesViewModel)

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
        recyclerView.id = R.id.pages_search_recycler_view_id
    }

    private fun PagesListFragmentBinding.setupObservers() {
        viewModel.searchResult.observe(viewLifecycleOwner, Observer { data ->
            data?.let { setSearchResult(data) }
        })
    }

    private fun PagesListFragmentBinding.setSearchResult(pages: List<PageItem>) {
        val adapter: PageSearchAdapter
        if (recyclerView.adapter == null) {
            adapter = PageSearchAdapter(
                    { action, page -> viewModel.onMenuAction(action, page) },
                    { page -> viewModel.onItemTapped(page) }, uiHelper)
            recyclerView.adapter = adapter
        } else {
            adapter = recyclerView.adapter as PageSearchAdapter
        }
        adapter.update(pages)
    }
}
