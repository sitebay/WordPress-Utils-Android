package org.sitebay.android.ui.posts

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import org.sitebay.android.R
import org.sitebay.android.WordPress
import org.sitebay.android.databinding.PostPrepublishingHomeFragmentBinding
import org.sitebay.android.fluxc.model.SiteModel
import org.sitebay.android.ui.posts.EditPostSettingsFragment.EditPostActivityHook
import org.sitebay.android.ui.utils.UiHelpers
import org.sitebay.android.util.image.ImageManager
import org.sitebay.android.viewmodel.observeEvent
import javax.inject.Inject

class PrepublishingHomeFragment : Fragment(R.layout.post_prepublishing_home_fragment) {
    @Inject lateinit var uiHelpers: UiHelpers
    @Inject lateinit var imageManager: ImageManager

    @Inject internal lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: PrepublishingHomeViewModel

    private var actionClickedListener: PrepublishingActionClickedListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireNotNull(activity).application as WordPress).component().inject(this)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        actionClickedListener = parentFragment as PrepublishingActionClickedListener
    }

    override fun onDetach() {
        super.onDetach()
        actionClickedListener = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(PostPrepublishingHomeFragmentBinding.bind(view)) {
            actionsRecyclerView.layoutManager = LinearLayoutManager(requireActivity())
            actionsRecyclerView.adapter = PrepublishingHomeAdapter(requireActivity())

            initViewModel()
        }
    }

    override fun onResume() {
        val isStoryPost = checkNotNull(arguments?.getBoolean(IS_STORY_POST)) {
            "arguments can't be null."
        }
        if (isStoryPost) {
            requireActivity().window.decorView.requestLayout()
        }
        super.onResume()
    }

    private fun PostPrepublishingHomeFragmentBinding.initViewModel() {
        viewModel = ViewModelProvider(this@PrepublishingHomeFragment, viewModelFactory)
                .get(PrepublishingHomeViewModel::class.java)

        viewModel.storyTitleUiState.observe(viewLifecycleOwner, { storyTitleUiState ->
            uiHelpers.updateVisibility(storyTitleHeaderView, true)
            storyTitleHeaderView.init(uiHelpers, imageManager, storyTitleUiState)
        })

        viewModel.uiState.observe(viewLifecycleOwner, { uiState ->
            (actionsRecyclerView.adapter as PrepublishingHomeAdapter).update(uiState)
        })

        viewModel.onActionClicked.observeEvent(viewLifecycleOwner, { actionType ->
            actionClickedListener?.onActionClicked(actionType)
        })

        viewModel.onSubmitButtonClicked.observeEvent(viewLifecycleOwner, { publishPost ->
            actionClickedListener?.onSubmitButtonClicked(publishPost)
        })

        val isStoryPost = checkNotNull(arguments?.getBoolean(IS_STORY_POST)) {
            "arguments can't be null."
        }

        viewModel.start(getEditPostRepository(), getSite(), isStoryPost)
    }

    private fun getSite(): SiteModel {
        val editPostActivityHook = requireNotNull(getEditPostActivityHook()) {
            "EditPostActivityHook shouldn't be null."
        }

        return editPostActivityHook.site
    }

    private fun getEditPostRepository(): EditPostRepository {
        val editPostActivityHook = requireNotNull(getEditPostActivityHook()) {
            "This is possibly null because it's " +
                    "called during config changes."
        }

        return editPostActivityHook.editPostRepository
    }

    private fun getEditPostActivityHook(): EditPostActivityHook? {
        val activity = activity ?: return null
        return if (activity is EditPostActivityHook) {
            activity
        } else {
            throw RuntimeException("$activity must implement EditPostActivityHook")
        }
    }

    companion object {
        const val TAG = "prepublishing_home_fragment_tag"
        const val IS_STORY_POST = "prepublishing_home_fragment_is_story_post"

        fun newInstance(isStoryPost: Boolean) =
                PrepublishingHomeFragment().apply {
                    arguments = Bundle().apply {
                        putBoolean(IS_STORY_POST, isStoryPost)
                    }
                }
    }
}
