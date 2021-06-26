package org.sitebay.android.ui.comments.unified

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.collect
import org.sitebay.android.R
import org.sitebay.android.WordPress
import org.sitebay.android.databinding.CommentListFragmentBinding
import org.sitebay.android.ui.comments.unified.UnifiedCommentListViewModel.CommentsListUiModel
import javax.inject.Inject

class UnifiedCommentListFragment : Fragment(R.layout.comment_list_fragment) {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: UnifiedCommentListViewModel
    private lateinit var adapter: UnifiedCommentListAdapter

    private var binding: CommentListFragmentBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity().application as WordPress).component().inject(this)
        viewModel = ViewModelProvider(this, viewModelFactory).get(UnifiedCommentListViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = CommentListFragmentBinding.bind(view).apply {
            setupContentViews()
            setupObservers()
        }
    }

    private fun CommentListFragmentBinding.setupContentViews() {
        val layoutManager = LinearLayoutManager(context)
        commentsRecyclerView.layoutManager = layoutManager
        commentsRecyclerView.addItemDecoration(UnifiedCommentListItemDecoration(commentsRecyclerView.context))

        adapter = UnifiedCommentListAdapter(requireContext())
        commentsRecyclerView.adapter = adapter
    }

    private fun CommentListFragmentBinding.setupObservers() {
        lifecycleScope.launchWhenStarted {
            viewModel.uiState.collect { uiState ->
                setupCommentsList(uiState.commentsListUiModel)
            }
        }
    }

    private fun CommentListFragmentBinding.setupCommentsList(commentsListUiModel: CommentsListUiModel) {
        when (commentsListUiModel) {
            is CommentsListUiModel.Data -> {
                adapter.submitData(lifecycle, commentsListUiModel.pagingData)
            }
            else -> {
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    companion object {
        fun newInstance(): UnifiedCommentListFragment {
            val args = Bundle()
            val fragment = UnifiedCommentListFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
