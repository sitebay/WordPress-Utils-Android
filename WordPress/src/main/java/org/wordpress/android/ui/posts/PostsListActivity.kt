package org.sitebay.android.ui.posts

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.Menu
import android.view.MenuItem
import android.view.MenuItem.OnActionExpandListener
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.google.android.material.snackbar.Snackbar
import org.sitebay.android.R
import org.sitebay.android.WordPress
import org.sitebay.android.databinding.PostListActivityBinding
import org.sitebay.android.fluxc.Dispatcher
import org.sitebay.android.fluxc.model.LocalOrRemoteId.LocalId
import org.sitebay.android.fluxc.model.SiteModel
import org.sitebay.android.fluxc.store.SiteStore
import org.sitebay.android.push.NotificationType
import org.sitebay.android.push.NotificationsProcessingService.ARG_NOTIFICATION_TYPE
import org.sitebay.android.ui.ActivityId
import org.sitebay.android.ui.ActivityLauncher
import org.sitebay.android.ui.LocaleAwareActivity
import org.sitebay.android.ui.PagePostCreationSourcesDetail.STORY_FROM_POSTS_LIST
import org.sitebay.android.ui.RequestCodes
import org.sitebay.android.ui.ScrollableViewInitializedListener
import org.sitebay.android.ui.main.MainActionListItem.ActionType
import org.sitebay.android.ui.notifications.SystemNotificationsTracker
import org.sitebay.android.ui.pages.SnackbarMessageHolder
import org.sitebay.android.ui.photopicker.MediaPickerLauncher
import org.sitebay.android.ui.posts.BasicFragmentDialog.BasicDialogNegativeClickInterface
import org.sitebay.android.ui.posts.BasicFragmentDialog.BasicDialogOnDismissByOutsideTouchInterface
import org.sitebay.android.ui.posts.BasicFragmentDialog.BasicDialogPositiveClickInterface
import org.sitebay.android.ui.posts.EditPostSettingsFragment.EditPostActivityHook
import org.sitebay.android.ui.posts.PostListType.SEARCH
import org.sitebay.android.ui.posts.PrepublishingBottomSheetFragment.Companion.newInstance
import org.sitebay.android.ui.posts.adapters.AuthorSelectionAdapter
import org.sitebay.android.ui.posts.prepublishing.PrepublishingBottomSheetListener
import org.sitebay.android.ui.stories.StoriesMediaPickerResultHandler
import org.sitebay.android.ui.uploads.UploadActionUseCase
import org.sitebay.android.ui.uploads.UploadUtilsWrapper
import org.sitebay.android.ui.utils.UiHelpers
import org.sitebay.android.ui.utils.UiString
import org.sitebay.android.util.AppLog
import org.sitebay.android.util.SnackbarItem
import org.sitebay.android.util.SnackbarSequencer
import org.sitebay.android.util.redirectContextClickToLongPressListener
import org.sitebay.android.util.setLiftOnScrollTargetViewIdAndRequestLayout
import org.sitebay.android.viewmodel.observeEvent
import org.sitebay.android.viewmodel.posts.PostListCreateMenuViewModel
import javax.inject.Inject

const val EXTRA_TARGET_POST_LOCAL_ID = "targetPostLocalId"
const val STATE_KEY_PREVIEW_STATE = "stateKeyPreviewState"
const val STATE_KEY_BOTTOMSHEET_POST_ID = "stateKeyBottomSheetPostId"

class PostsListActivity : LocaleAwareActivity(),
        EditPostActivityHook,
        PrepublishingBottomSheetListener,
        BasicDialogPositiveClickInterface,
        BasicDialogNegativeClickInterface,
        BasicDialogOnDismissByOutsideTouchInterface,
        ScrollableViewInitializedListener {
    @Inject internal lateinit var siteStore: SiteStore
    @Inject internal lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject internal lateinit var uiHelpers: UiHelpers
    @Inject internal lateinit var remotePreviewLogicHelper: RemotePreviewLogicHelper
    @Inject internal lateinit var previewStateHelper: PreviewStateHelper
    @Inject internal lateinit var progressDialogHelper: ProgressDialogHelper
    @Inject internal lateinit var dispatcher: Dispatcher
    @Inject internal lateinit var uploadActionUseCase: UploadActionUseCase
    @Inject internal lateinit var snackbarSequencer: SnackbarSequencer
    @Inject internal lateinit var uploadUtilsWrapper: UploadUtilsWrapper
    @Inject internal lateinit var systemNotificationTracker: SystemNotificationsTracker
    @Inject internal lateinit var editPostRepository: EditPostRepository
    @Inject internal lateinit var mediaPickerLauncher: MediaPickerLauncher
    @Inject internal lateinit var storiesMediaPickerResultHandler: StoriesMediaPickerResultHandler

    private lateinit var site: SiteModel
    private lateinit var binding: PostListActivityBinding

    override fun getSite() = site
    override fun getEditPostRepository() = editPostRepository

    private lateinit var viewModel: PostListMainViewModel
    private lateinit var postListCreateMenuViewModel: PostListCreateMenuViewModel

    private lateinit var postsPagerAdapter: PostsPagerAdapter
    private lateinit var searchActionButton: MenuItem
    private lateinit var toggleViewLayoutMenuItem: MenuItem

    private var restorePreviousSearch = false

    private var progressDialog: ProgressDialog? = null

    private var onPageChangeListener: OnPageChangeListener = object : OnPageChangeListener {
        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

        override fun onPageSelected(position: Int) {
            viewModel.onTabChanged(position)
        }

        override fun onPageScrollStateChanged(state: Int) {}
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (!intent.hasExtra(WordPress.SITE)) {
            AppLog.e(AppLog.T.POSTS, "PostListActivity started without a site.")
            finish()
            return
        }
        restartWhenSiteHasChanged(intent)
        loadIntentData(intent)
    }

    private fun restartWhenSiteHasChanged(intent: Intent) {
        val site = intent.getSerializableExtra(WordPress.SITE) as SiteModel
        if (site.id != this.site.id) {
            finish()
            startActivity(intent)
            return
        }
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as WordPress).component().inject(this)
        with(PostListActivityBinding.inflate(layoutInflater)) {
            setContentView(root)
            binding = this

            site = if (savedInstanceState == null) {
                checkNotNull(intent.getSerializableExtra(WordPress.SITE) as? SiteModel) {
                    "SiteModel cannot be null, check the PendingIntent starting PostsListActivity"
                }
            } else {
                restorePreviousSearch = true
                savedInstanceState.getSerializable(WordPress.SITE) as SiteModel
            }

            val initPreviewState = if (savedInstanceState == null) {
                PostListRemotePreviewState.NONE
            } else {
                PostListRemotePreviewState.fromInt(savedInstanceState.getInt(STATE_KEY_PREVIEW_STATE, 0))
            }

            val currentBottomSheetPostId = if (savedInstanceState == null) {
                LocalId(0)
            } else {
                LocalId(savedInstanceState.getInt(STATE_KEY_BOTTOMSHEET_POST_ID, 0))
            }

            setupActionBar()
            setupContent()
            initViewModel(initPreviewState, currentBottomSheetPostId)
            initCreateMenuViewModel()
            loadIntentData(intent)
        }
    }

    private fun setupActionBar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        title = getString(R.string.my_site_btn_blog_posts)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun PostListActivityBinding.setupContent() {
        val authorSelectionAdapter = AuthorSelectionAdapter(this@PostsListActivity)
        postListAuthorSelection.adapter = authorSelectionAdapter

        postListAuthorSelection.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>) {}

            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                viewModel.updateAuthorFilterSelection(id)
            }
        }

        // Just a safety measure - there shouldn't by any existing listeners since this method is called just once.
        postPager.clearOnPageChangeListeners()

        // this method call needs to be below `clearOnPageChangeListeners` as it internally adds an OnPageChangeListener
        tabLayout.setupWithViewPager(postPager)
        postPager.addOnPageChangeListener(onPageChangeListener)
        fabButton.setOnClickListener {
            viewModel.fabClicked()
        }

        fabButton.setOnLongClickListener {
            viewModel.onFabLongPressed()
            return@setOnLongClickListener true
        }

        fabButton.redirectContextClickToLongPressListener()

        fabTooltip.setOnClickListener {
            postListCreateMenuViewModel.onTooltipTapped()
        }

        postsPagerAdapter = PostsPagerAdapter(POST_LIST_PAGES, site, supportFragmentManager)
        postPager.adapter = postsPagerAdapter
    }

    private fun PostListActivityBinding.initCreateMenuViewModel() {
        postListCreateMenuViewModel = ViewModelProvider(this@PostsListActivity, viewModelFactory)
                .get(PostListCreateMenuViewModel::class.java)

        postListCreateMenuViewModel.isBottomSheetShowing.observeEvent(this@PostsListActivity, { isBottomSheetShowing ->
            var createMenuFragment = supportFragmentManager.findFragmentByTag(PostListCreateMenuFragment.TAG)
            if (createMenuFragment == null) {
                if (isBottomSheetShowing) {
                    createMenuFragment = PostListCreateMenuFragment.newInstance()
                    createMenuFragment.show(supportFragmentManager, PostListCreateMenuFragment.TAG)
                }
            } else {
                if (!isBottomSheetShowing) {
                    createMenuFragment as PostListCreateMenuFragment
                    createMenuFragment.dismiss()
                }
            }
        })

        postListCreateMenuViewModel.fabUiState.observe(this@PostsListActivity, { fabUiState ->
            val message = resources.getString(fabUiState.CreateContentMessageId)

            if (fabUiState.isFabTooltipVisible) {
                fabTooltip.setMessage(message)
                fabTooltip.show()
            } else {
                fabTooltip.hide()
            }

            fabButton.contentDescription = message
        })

        postListCreateMenuViewModel.createAction.observe(this@PostsListActivity, { createAction ->
            when (createAction) {
                ActionType.CREATE_NEW_POST -> viewModel.newPost()
                ActionType.CREATE_NEW_STORY -> viewModel.newStoryPost()
                ActionType.CREATE_NEW_PAGE -> Unit
                ActionType.NO_ACTION -> Unit
                null -> Unit
            }
        })

        postListCreateMenuViewModel.start(site)
    }

    private fun PostListActivityBinding.initViewModel(
        initPreviewState: PostListRemotePreviewState,
        currentBottomSheetPostId: LocalId
    ) {
        viewModel = ViewModelProvider(this@PostsListActivity, viewModelFactory).get(PostListMainViewModel::class.java)
        viewModel.start(site, initPreviewState, currentBottomSheetPostId, editPostRepository, this@PostsListActivity)

        viewModel.viewState.observe(this@PostsListActivity, { state ->
            state?.let {
                loadViewState(state)
            }
        })

        viewModel.postListAction.observe(this@PostsListActivity, { postListAction ->
            postListAction?.let { action ->
                handlePostListAction(
                        this@PostsListActivity,
                        action,
                        remotePreviewLogicHelper,
                        previewStateHelper,
                        mediaPickerLauncher
                )
            }
        })
        viewModel.selectTab.observe(this@PostsListActivity, { tabIndex ->
            tabIndex?.let {
                tabLayout.getTabAt(tabIndex)?.select()
            }
        })
        viewModel.scrollToLocalPostId.observe(this@PostsListActivity, { targetLocalPostId ->
            targetLocalPostId?.let {
                postsPagerAdapter.getItemAtPosition(postPager.currentItem)?.scrollToTargetPost(targetLocalPostId)
            }
        })
        viewModel.snackBarMessage.observe(this@PostsListActivity, {
            it?.let { snackBarHolder -> showSnackBar(snackBarHolder) }
        })
        viewModel.toastMessage.observe(this@PostsListActivity, {
            it?.show(this@PostsListActivity)
        })
        viewModel.previewState.observe(this@PostsListActivity, {
            progressDialog = progressDialogHelper.updateProgressDialogState(
                    this@PostsListActivity,
                    progressDialog,
                    it.progressDialogUiState,
                    uiHelpers
            )
        })
        setupActions()
        viewModel.openPrepublishingBottomSheet.observeEvent(this@PostsListActivity, {
            val fragment = supportFragmentManager.findFragmentByTag(PrepublishingBottomSheetFragment.TAG)
            if (fragment == null) {
                val prepublishingFragment = newInstance(
                        site = site,
                        isPage = editPostRepository.isPage,
                        isStoryPost = false
                )
                prepublishingFragment.show(supportFragmentManager, PrepublishingBottomSheetFragment.TAG)
            }
        })

        setupFabEvents()
    }

    private fun setupActions() {
        viewModel.dialogAction.observe(this@PostsListActivity, {
            it?.show(this@PostsListActivity, supportFragmentManager, uiHelpers)
        })
        viewModel.postUploadAction.observe(this@PostsListActivity, {
            it?.let { uploadAction ->
                handleUploadAction(
                        uploadAction,
                        this@PostsListActivity,
                        findViewById(R.id.coordinator),
                        uploadActionUseCase,
                        uploadUtilsWrapper
                )
            }
        })
    }

    private fun PostListActivityBinding.setupFabEvents() {
        viewModel.onFabClicked.observeEvent(this@PostsListActivity, {
            postListCreateMenuViewModel.onFabClicked()
        })

        viewModel.onFabLongPressedForCreateMenu.observeEvent(this@PostsListActivity, {
            postListCreateMenuViewModel.onFabLongPressed()
            Toast.makeText(fabButton.context, R.string.create_post_story_fab_tooltip, Toast.LENGTH_SHORT).show()
        })

        viewModel.onFabLongPressedForPostList.observe(this@PostsListActivity, {
            if (fabButton.isHapticFeedbackEnabled) {
                fabButton.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            }
            Toast.makeText(fabButton.context, R.string.create_post_fab_tooltip, Toast.LENGTH_SHORT).show()
        })
    }

    private fun PostListActivityBinding.loadViewState(state: PostListMainViewState) {
        if (state.isFabVisible) {
            fabButton.show()
        } else {
            fabButton.hide()
        }

        val authorSelectionVisibility = if (state.isAuthorFilterVisible) View.VISIBLE else View.GONE
        postListAuthorSelection.visibility = authorSelectionVisibility
        postListTabLayoutFadingEdge.visibility = authorSelectionVisibility

        val tabLayoutPaddingStart =
                if (state.isAuthorFilterVisible) {
                    resources.getDimensionPixelSize(R.dimen.posts_list_tab_layout_fading_edge_width)
                } else 0
        tabLayout.setPaddingRelative(tabLayoutPaddingStart, 0, 0, 0)
        val authorSelectionAdapter = postListAuthorSelection.adapter as AuthorSelectionAdapter
        authorSelectionAdapter.updateItems(state.authorFilterItems)

        authorSelectionAdapter.getIndexOfSelection(state.authorFilterSelection)?.let { selectionIndex ->
            postListAuthorSelection.setSelection(selectionIndex)
        }
    }

    private fun showSnackBar(holder: SnackbarMessageHolder) {
        findViewById<View>(R.id.coordinator)?.let { parent ->
            snackbarSequencer.enqueue(
                    SnackbarItem(
                            SnackbarItem.Info(
                                    view = parent,
                                    textRes = holder.message,
                                    duration = Snackbar.LENGTH_LONG
                            ),
                            holder.buttonTitle?.let {
                                SnackbarItem.Action(
                                        textRes = holder.buttonTitle,
                                        clickListener = { holder.buttonAction() }
                                )
                            },
                            dismissCallback = { _, _ -> holder.onDismissAction() }
                    )
            )
        }
    }

    private fun loadIntentData(intent: Intent) {
        if (intent.hasExtra(ARG_NOTIFICATION_TYPE)) {
            val notificationType: NotificationType =
                    intent.getSerializableExtra(ARG_NOTIFICATION_TYPE) as NotificationType
            systemNotificationTracker.trackTappedNotification(notificationType)
        }

        val targetPostId = intent.getIntExtra(EXTRA_TARGET_POST_LOCAL_ID, -1)
        if (targetPostId != -1) {
            viewModel.showTargetPost(targetPostId)
        }
    }

    public override fun onResume() {
        super.onResume()
        ActivityId.trackLastActivity(ActivityId.POSTS)
        postListCreateMenuViewModel.onResume()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RequestCodes.EDIT_POST && resultCode == Activity.RESULT_OK) {
            if (data != null && EditPostActivity.checkToRestart(data)) {
                ActivityLauncher.editPostOrPageForResult(
                        data, this, site,
                        data.getIntExtra(EditPostActivity.EXTRA_POST_LOCAL_ID, 0)
                )

                // a restart will happen so, no need to continue here
                return
            }

            viewModel.handleEditPostResult(data)
        } else if (requestCode == RequestCodes.REMOTE_PREVIEW_POST) {
            viewModel.handleRemotePreviewClosing()
        } else if (requestCode == RequestCodes.PHOTO_PICKER &&
                resultCode == Activity.RESULT_OK &&
                data != null) {
            storiesMediaPickerResultHandler.handleMediaPickerResultForStories(data, this, site, STORY_FROM_POSTS_LIST)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        } else if (item.itemId == R.id.toggle_post_list_item_layout) {
            viewModel.toggleViewLayout()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menu?.let {
            menuInflater.inflate(R.menu.posts_list_toggle_view_layout, it)
            toggleViewLayoutMenuItem = it.findItem(R.id.toggle_post_list_item_layout)
            viewModel.viewLayoutTypeMenuUiState.observe(this, { menuUiState ->
                menuUiState?.let {
                    updateMenuIcon(menuUiState.iconRes, toggleViewLayoutMenuItem)
                    updateMenuTitle(menuUiState.title, toggleViewLayoutMenuItem)
                }
            })

            searchActionButton = it.findItem(R.id.toggle_post_search)

            initSearchFragment()
            binding.initSearchView()
        }
        return true
    }

    private fun initSearchFragment() {
        val searchFragmentTag = "search_fragment"

        var searchFragment = supportFragmentManager.findFragmentByTag(searchFragmentTag)

        if (searchFragment == null) {
            searchFragment = PostListFragment.newInstance(site, SEARCH)
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.search_container, searchFragment, searchFragmentTag)
                    .commit()
        }
    }

    private fun PostListActivityBinding.initSearchView() {
        searchActionButton.setOnActionExpandListener(object : OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                viewModel.onSearchExpanded(restorePreviousSearch)
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                viewModel.onSearchCollapsed()
                return true
            }
        })

        val searchView = searchActionButton.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                viewModel.onSearch(query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (restorePreviousSearch) {
                    restorePreviousSearch = false
                    searchView.setQuery(viewModel.searchQuery.value, false)
                } else {
                    viewModel.onSearch(newText)
                }
                return true
            }
        })

        viewModel.isSearchExpanded.observe(this@PostsListActivity, { isExpanded ->
            toggleViewLayoutMenuItem.isVisible = !isExpanded
            toggleSearch(isExpanded)
        })
    }

    private fun PostListActivityBinding.toggleSearch(isExpanded: Boolean) {
        val tabContainer = findViewById<View>(R.id.tabContainer)
        val searchContainer = findViewById<View>(R.id.search_container)

        if (isExpanded) {
            postPager.visibility = View.GONE
            tabContainer.visibility = View.GONE
            searchContainer.visibility = View.VISIBLE
            if (!searchActionButton.isActionViewExpanded) {
                searchActionButton.expandActionView()
            }
            appbarMain.setLiftOnScrollTargetViewIdAndRequestLayout(R.id.posts_search_recycler_view_id)
        } else {
            postPager.visibility = View.VISIBLE
            tabContainer.visibility = View.VISIBLE
            searchContainer.visibility = View.GONE
            if (searchActionButton.isActionViewExpanded) {
                searchActionButton.collapseActionView()
            }
            appbarMain.getTag(R.id.posts_non_search_recycler_view_id_tag_key)?.let {
                appbarMain.setLiftOnScrollTargetViewIdAndRequestLayout(it as Int)
            }
        }
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(WordPress.SITE, site)
        viewModel.previewState.value?.let {
            outState.putInt(STATE_KEY_PREVIEW_STATE, it.value)
        }
        viewModel.currentBottomSheetPostId?.let {
            outState.putInt(STATE_KEY_BOTTOMSHEET_POST_ID, it.value)
        }
    }

    // BasicDialogFragment Callbacks

    override fun onPositiveClicked(instanceTag: String) {
        viewModel.onPositiveClickedForBasicDialog(instanceTag)
    }

    override fun onNegativeClicked(instanceTag: String) {
        viewModel.onNegativeClickedForBasicDialog(instanceTag)
    }

    override fun onDismissByOutsideTouch(instanceTag: String) {
        viewModel.onDismissByOutsideTouchForBasicDialog(instanceTag)
    }

    // Menu PostListViewLayoutType handling

    private fun updateMenuIcon(@DrawableRes iconRes: Int, menuItem: MenuItem) {
        ContextCompat.getDrawable(this, iconRes)?.let { drawable ->
            menuItem.setIcon(drawable)
        }
    }

    private fun updateMenuTitle(title: UiString, menuItem: MenuItem): MenuItem? {
        return menuItem.setTitle(uiHelpers.getTextOfUiString(this@PostsListActivity, title))
    }

    override fun onSubmitButtonClicked(publishPost: PublishPost) {
        viewModel.onBottomSheetPublishButtonClicked()
    }

    override fun onScrollableViewInitialized(containerId: Int) {
        binding.appbarMain.setLiftOnScrollTargetViewIdAndRequestLayout(containerId)
        binding.appbarMain.setTag(R.id.posts_non_search_recycler_view_id_tag_key, containerId)
    }

    companion object {
        @JvmStatic
        fun buildIntent(context: Context, site: SiteModel): Intent {
            val intent = Intent(context, PostsListActivity::class.java)
            intent.putExtra(WordPress.SITE, site)
            return intent
        }
    }
}
