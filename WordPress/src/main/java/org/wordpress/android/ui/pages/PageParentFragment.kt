package org.sitebay.android.ui.pages

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.MenuItem.OnActionExpandListener
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.sitebay.android.R
import org.sitebay.android.WordPress
import org.sitebay.android.databinding.PageParentFragmentBinding
import org.sitebay.android.fluxc.model.SiteModel
import org.sitebay.android.util.DisplayUtils
import org.sitebay.android.viewmodel.pages.PageParentViewModel
import org.sitebay.android.widgets.RecyclerItemDecoration
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class PageParentFragment : Fragment(R.layout.page_parent_fragment), CoroutineScope {
    private var job: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: PageParentViewModel

    private val listStateKey = "list_state"

    private var linearLayoutManager: LinearLayoutManager? = null
    private var saveButton: MenuItem? = null
    private lateinit var searchAction: MenuItem

    private var pageId: Long? = null
    private var restorePreviousSearch = false
    private var binding: PageParentFragmentBinding? = null

    companion object {
        fun newInstance(): PageParentFragment {
            return PageParentFragment()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            activity?.onBackPressed()
            return true
        } else if (item.itemId == R.id.save_parent) {
            viewModel.onSaveButtonTapped()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun returnParentChoiceAndExit() {
        val result = Intent()
        result.putExtra(EXTRA_PAGE_REMOTE_ID_KEY, pageId)
        result.putExtra(EXTRA_PAGE_PARENT_ID_KEY, viewModel.currentParent.id)
        activity?.setResult(Activity.RESULT_OK, result)
        activity?.onBackPressed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.page_parent_menu, menu)

        saveButton = menu.findItem(R.id.save_parent)
        viewModel.isSaveButtonVisible.value?.let { saveButton?.isVisible = it }
        searchAction = checkNotNull(menu.findItem(R.id.action_search)) {
            "Menu does not contain mandatory search item"
        }

        binding!!.initializeSearchView()
    }

    private fun PageParentFragmentBinding.initializeSearchView() {
        searchAction.setOnActionExpandListener(object : OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                viewModel.onSearchExpanded(restorePreviousSearch)
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                viewModel.onSearchCollapsed()
                return true
            }
        })

        val searchView = searchAction.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                viewModel.onSearch(query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (restorePreviousSearch) {
                    restorePreviousSearch = false
                    searchView.setQuery(viewModel.lastSearchQuery, false)
                } else {
                    viewModel.onSearch(newText)
                }
                return true
            }
        })

        val searchEditFrame = searchAction.actionView.findViewById<LinearLayout>(R.id.search_edit_frame)
        (searchEditFrame.layoutParams as LinearLayout.LayoutParams)
                .apply { this.leftMargin = DisplayUtils.dpToPx(activity, -8) }

        viewModel.isSearchExpanded.observe(this@PageParentFragment, Observer {
            if (it == true) {
                showSearchList(searchAction)
            } else {
                hideSearchList(searchAction)
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pageId = activity?.intent?.getLongExtra(EXTRA_PAGE_REMOTE_ID_KEY, 0)

        val nonNullPageId = checkNotNull(pageId)
        val nonNullActivity = requireActivity()

        (nonNullActivity.application as? WordPress)?.component()?.inject(this)
        with(PageParentFragmentBinding.bind(view)) {
            binding = this
            initializeViews(nonNullActivity, savedInstanceState)
            initializeViewModels(nonNullActivity, nonNullPageId, savedInstanceState == null)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun PageParentFragmentBinding.initializeViews(activity: FragmentActivity, savedInstanceState: Bundle?) {
        val layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        savedInstanceState?.getParcelable<Parcelable>(listStateKey)?.let {
            layoutManager.onRestoreInstanceState(it)
        }

        linearLayoutManager = layoutManager
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.addItemDecoration(RecyclerItemDecoration(0, DisplayUtils.dpToPx(activity, 1)))

        val searchFragment = PageParentSearchFragment.newInstance()
        activity.supportFragmentManager
                .beginTransaction()
                .replace(R.id.frameSearch, searchFragment)
                .commit()
    }

    private fun PageParentFragmentBinding.initializeViewModels(
        activity: FragmentActivity,
        pageId: Long,
        isFirstStart: Boolean
    ) {
        viewModel = ViewModelProvider(activity, viewModelFactory)
                .get(PageParentViewModel::class.java)

        setupObservers()

        if (isFirstStart) {
            val site = activity.intent?.getSerializableExtra(WordPress.SITE) as SiteModel?
            val nonNullSite = checkNotNull(site)
            viewModel.start(nonNullSite, pageId)
        } else {
            restorePreviousSearch = true
        }
    }

    private fun PageParentFragmentBinding.setupObservers() {
        viewModel.pages.observe(viewLifecycleOwner, { pages ->
            pages?.let { setPages(pages) }
        })

        viewModel.isSaveButtonVisible.observe(viewLifecycleOwner, { isVisible ->
            isVisible?.let { saveButton?.isVisible = isVisible }
        })

        viewModel.saveParent.observe(viewLifecycleOwner, {
            returnParentChoiceAndExit()
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        linearLayoutManager?.let { outState.putParcelable(listStateKey, it.onSaveInstanceState()) }
        super.onSaveInstanceState(outState)
    }

    private fun PageParentFragmentBinding.setPages(pages: List<PageItem>) {
        val adapter: PageParentAdapter
        if (recyclerView.adapter == null) {
            adapter = PageParentAdapter({ page -> viewModel.onParentSelected(page) }, this@PageParentFragment)
            recyclerView.adapter = adapter
        } else {
            adapter = recyclerView.adapter as PageParentAdapter
        }
        adapter.update(pages)
    }

    private fun PageParentFragmentBinding.hideSearchList(myActionMenuItem: MenuItem) {
        recyclerView.visibility = View.VISIBLE
        frameSearch.visibility = View.GONE
        if (myActionMenuItem.isActionViewExpanded) {
            myActionMenuItem.collapseActionView()
        }
        /**Force the recyclerview to redraw if selection has changed while in search mode*/
        if (viewModel.currentParent != viewModel.initialParent) {
            recyclerView.adapter?.notifyDataSetChanged()
        }
    }

    private fun PageParentFragmentBinding.showSearchList(myActionMenuItem: MenuItem) {
        frameSearch.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        if (!myActionMenuItem.isActionViewExpanded) {
            myActionMenuItem.expandActionView()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
