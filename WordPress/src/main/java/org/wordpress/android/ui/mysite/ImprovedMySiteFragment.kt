package org.sitebay.android.ui.mysite

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.TooltipCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import com.google.android.material.snackbar.Snackbar
import com.yalantis.ucrop.UCrop
import com.yalantis.ucrop.UCrop.Options
import com.yalantis.ucrop.UCropActivity
import org.sitebay.android.R
import org.sitebay.android.R.attr
import org.sitebay.android.WordPress
import org.sitebay.android.databinding.NewMySiteFragmentBinding
import org.sitebay.android.ui.ActivityLauncher
import org.sitebay.android.ui.RequestCodes
import org.sitebay.android.ui.TextInputDialogFragment
import org.sitebay.android.ui.domains.DomainRegistrationActivity.DomainRegistrationPurpose.CTA_DOMAIN_CREDIT_REDEMPTION
import org.sitebay.android.ui.domains.DomainRegistrationResultFragment.Companion.RESULT_REGISTERED_DOMAIN_EMAIL
import org.sitebay.android.ui.main.SitePickerActivity
import org.sitebay.android.ui.main.WPMainActivity
import org.sitebay.android.ui.main.utils.MeGravatarLoader
import org.sitebay.android.ui.mysite.MySiteViewModel.State
import org.sitebay.android.ui.mysite.SiteIconUploadHandler.ItemUploadedModel
import org.sitebay.android.ui.mysite.SiteNavigationAction.AddNewSite
import org.sitebay.android.ui.mysite.SiteNavigationAction.AddNewStory
import org.sitebay.android.ui.mysite.SiteNavigationAction.AddNewStoryWithMediaIds
import org.sitebay.android.ui.mysite.SiteNavigationAction.AddNewStoryWithMediaUris
import org.sitebay.android.ui.mysite.SiteNavigationAction.ConnectJetpackForStats
import org.sitebay.android.ui.mysite.SiteNavigationAction.OpenActivityLog
import org.sitebay.android.ui.mysite.SiteNavigationAction.OpenAdmin
import org.sitebay.android.ui.mysite.SiteNavigationAction.OpenBackup
import org.sitebay.android.ui.mysite.SiteNavigationAction.OpenComments
import org.sitebay.android.ui.mysite.SiteNavigationAction.OpenCropActivity
import org.sitebay.android.ui.mysite.SiteNavigationAction.OpenDomainRegistration
import org.sitebay.android.ui.mysite.SiteNavigationAction.OpenJetpackSettings
import org.sitebay.android.ui.mysite.SiteNavigationAction.OpenMeScreen
import org.sitebay.android.ui.mysite.SiteNavigationAction.OpenMedia
import org.sitebay.android.ui.mysite.SiteNavigationAction.OpenMediaPicker
import org.sitebay.android.ui.mysite.SiteNavigationAction.OpenPages
import org.sitebay.android.ui.mysite.SiteNavigationAction.OpenPeople
import org.sitebay.android.ui.mysite.SiteNavigationAction.OpenPlan
import org.sitebay.android.ui.mysite.SiteNavigationAction.OpenPlugins
import org.sitebay.android.ui.mysite.SiteNavigationAction.OpenPosts
import org.sitebay.android.ui.mysite.SiteNavigationAction.OpenScan
import org.sitebay.android.ui.mysite.SiteNavigationAction.OpenSharing
import org.sitebay.android.ui.mysite.SiteNavigationAction.OpenSite
import org.sitebay.android.ui.mysite.SiteNavigationAction.OpenSitePicker
import org.sitebay.android.ui.mysite.SiteNavigationAction.OpenSiteSettings
import org.sitebay.android.ui.mysite.SiteNavigationAction.OpenStats
import org.sitebay.android.ui.mysite.SiteNavigationAction.OpenStories
import org.sitebay.android.ui.mysite.SiteNavigationAction.OpenThemes
import org.sitebay.android.ui.mysite.SiteNavigationAction.OpenUnifiedComments
import org.sitebay.android.ui.mysite.SiteNavigationAction.StartWPComLoginForJetpackStats
import org.sitebay.android.ui.mysite.dynamiccards.DynamicCardMenuFragment
import org.sitebay.android.ui.mysite.dynamiccards.DynamicCardMenuViewModel
import org.sitebay.android.ui.pages.SnackbarMessageHolder
import org.sitebay.android.ui.photopicker.MediaPickerConstants
import org.sitebay.android.ui.photopicker.MediaPickerLauncher
import org.sitebay.android.ui.photopicker.PhotoPickerActivity.PhotoPickerMediaSource
import org.sitebay.android.ui.posts.BasicDialogViewModel
import org.sitebay.android.ui.posts.BasicDialogViewModel.BasicDialogModel
import org.sitebay.android.ui.uploads.UploadService
import org.sitebay.android.ui.uploads.UploadUtilsWrapper
import org.sitebay.android.ui.utils.UiHelpers
import org.sitebay.android.ui.utils.UiString.UiStringText
import org.sitebay.android.util.AppLog
import org.sitebay.android.util.AppLog.T.MAIN
import org.sitebay.android.util.AppLog.T.UTILS
import org.sitebay.android.util.QuickStartUtilsWrapper
import org.sitebay.android.util.SnackbarItem
import org.sitebay.android.util.SnackbarItem.Action
import org.sitebay.android.util.SnackbarItem.Info
import org.sitebay.android.util.SnackbarSequencer
import org.sitebay.android.util.UriWrapper
import org.sitebay.android.util.getColorFromAttribute
import org.sitebay.android.util.image.ImageManager
import org.sitebay.android.util.image.ImageType.USER
import org.sitebay.android.util.setVisible
import org.sitebay.android.viewmodel.observeEvent
import java.io.File
import javax.inject.Inject

class ImprovedMySiteFragment : Fragment(R.layout.new_my_site_fragment),
        TextInputDialogFragment.Callback {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var imageManager: ImageManager
    @Inject lateinit var uiHelpers: UiHelpers
    @Inject lateinit var snackbarSequencer: SnackbarSequencer
    @Inject lateinit var meGravatarLoader: MeGravatarLoader
    @Inject lateinit var mediaPickerLauncher: MediaPickerLauncher
    @Inject lateinit var uploadUtilsWrapper: UploadUtilsWrapper
    @Inject lateinit var quickStartUtils: QuickStartUtilsWrapper
    private lateinit var viewModel: MySiteViewModel
    private lateinit var dialogViewModel: BasicDialogViewModel
    private lateinit var dynamicCardMenuViewModel: DynamicCardMenuViewModel

    private var binding: NewMySiteFragmentBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity().application as WordPress).component().inject(this)
        viewModel = ViewModelProvider(this, viewModelFactory).get(MySiteViewModel::class.java)
        dialogViewModel = ViewModelProvider(requireActivity(), viewModelFactory)
                .get(BasicDialogViewModel::class.java)
        dynamicCardMenuViewModel = ViewModelProvider(requireActivity(), viewModelFactory)
                .get(DynamicCardMenuViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = NewMySiteFragmentBinding.bind(view).apply {
            setupToolbar()
            setupContentViews(savedInstanceState)
            setupObservers()
        }
    }

    private fun NewMySiteFragmentBinding.setupToolbar() {
        toolbarMain.let { toolbar ->
            toolbar.inflateMenu(R.menu.my_site_menu)
            toolbar.menu.findItem(R.id.me_item)?.let { meMenu ->
                meMenu.actionView.let { actionView ->
                    actionView.setOnClickListener { viewModel.onAvatarPressed() }
                    TooltipCompat.setTooltipText(actionView, meMenu.title)
                }
            }
        }

        val avatar = root.findViewById<ImageView>(R.id.avatar)

        appbarMain.addOnOffsetChangedListener(OnOffsetChangedListener { appBarLayout, verticalOffset ->
            val maxOffset = appBarLayout.totalScrollRange
            val currentOffset = maxOffset + verticalOffset

            val percentage = ((currentOffset.toFloat() / maxOffset.toFloat()) * 100).toInt()
            avatar?.let { avatar ->
                val minSize = avatar.minimumHeight
                val maxSize = avatar.maxHeight
                val modifierPx = (minSize.toFloat() - maxSize.toFloat()) * (percentage.toFloat() / 100) * -1
                val modifierPercentage = modifierPx / minSize
                val newScale = 1 + modifierPercentage

                avatar.scaleX = newScale
                avatar.scaleY = newScale
            }
        })
    }

    private fun NewMySiteFragmentBinding.setupContentViews(savedInstanceState: Bundle?) {
        actionableEmptyView.button.setOnClickListener { viewModel.onAddSitePressed() }

        val layoutManager = LinearLayoutManager(activity)

        savedInstanceState?.getParcelable<Parcelable>(KEY_LIST_STATE)?.let {
            layoutManager.onRestoreInstanceState(it)
        }

        recyclerView.layoutManager = layoutManager

        val adapter = MySiteAdapter(imageManager, uiHelpers)

        savedInstanceState?.getBundle(KEY_NESTED_LISTS_STATES)?.let {
            adapter.onRestoreInstanceState(it)
        }

        recyclerView.adapter = adapter
    }

    private fun NewMySiteFragmentBinding.setupObservers() {
        viewModel.uiModel.observe(viewLifecycleOwner, { uiModel ->
            loadGravatar(uiModel.accountAvatarUrl)
            when (val state = uiModel.state) {
                is State.SiteSelected -> loadData(state.items)
                is State.NoSites -> loadEmptyView(state.shouldShowImage)
            }
        })
        viewModel.onScrollTo.observeEvent(viewLifecycleOwner, {
            (recyclerView.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(it, 0)
        })
        viewModel.onBasicDialogShown.observeEvent(viewLifecycleOwner, { model ->
            dialogViewModel.showDialog(requireActivity().supportFragmentManager,
                    BasicDialogModel(
                            model.tag,
                            getString(model.title),
                            getString(model.message),
                            getString(model.positiveButtonLabel),
                            model.negativeButtonLabel?.let { label -> getString(label) },
                            model.cancelButtonLabel?.let { label -> getString(label) }
                    ))
        })
        viewModel.onTextInputDialogShown.observeEvent(viewLifecycleOwner, { model ->
            val inputDialog = TextInputDialogFragment.newInstance(
                    getString(model.title),
                    model.initialText,
                    getString(model.hint),
                    model.isMultiline,
                    model.isInputEnabled,
                    model.callbackId
            )
            inputDialog.setTargetFragment(this@ImprovedMySiteFragment, 0)
            inputDialog.show(parentFragmentManager, TextInputDialogFragment.TAG)
        })
        viewModel.onDynamicCardMenuShown.observeEvent(viewLifecycleOwner, { dynamicCardMenuModel ->
            ((parentFragmentManager.findFragmentByTag(dynamicCardMenuModel.id) as? DynamicCardMenuFragment)
                    ?: DynamicCardMenuFragment.newInstance(
                            dynamicCardMenuModel.cardType,
                            dynamicCardMenuModel.isPinned
                    ))
                    .show(parentFragmentManager, dynamicCardMenuModel.id)
        })
        viewModel.onNavigation.observeEvent(viewLifecycleOwner, { handleNavigationAction(it) })
        viewModel.onSnackbarMessage.observeEvent(viewLifecycleOwner, { showSnackbar(it) })
        viewModel.onQuickStartMySitePrompts.observeEvent(viewLifecycleOwner, { activeTutorialPrompt ->
            val message = quickStartUtils.stylizeThemedQuickStartPrompt(
                    requireContext(),
                    activeTutorialPrompt.shortMessagePrompt,
                    activeTutorialPrompt.iconId
            )
            showSnackbar(SnackbarMessageHolder(UiStringText(message)))
        })
        viewModel.onMediaUpload.observeEvent(viewLifecycleOwner, { UploadService.uploadMedia(requireActivity(), it) })
        dialogViewModel.onInteraction.observeEvent(viewLifecycleOwner, { viewModel.onDialogInteraction(it) })
        dynamicCardMenuViewModel.onInteraction.observeEvent(viewLifecycleOwner, { interaction ->
            viewModel.onQuickStartMenuInteraction(interaction)
        })
        viewModel.onUploadedItem.observeEvent(viewLifecycleOwner, { handleUploadedItem(it) })
    }

    @Suppress("ComplexMethod")
    private fun handleNavigationAction(action: SiteNavigationAction) = when (action) {
        is OpenMeScreen -> ActivityLauncher.viewMeActivityForResult(activity)
        is OpenSitePicker -> ActivityLauncher.showSitePickerForResult(activity, action.site)
        is OpenSite -> ActivityLauncher.viewCurrentSite(activity, action.site, true)
        is OpenMediaPicker -> mediaPickerLauncher.showSiteIconPicker(this@ImprovedMySiteFragment, action.site)
        is OpenCropActivity -> startCropActivity(action.imageUri)
        is OpenActivityLog -> ActivityLauncher.viewActivityLogList(activity, action.site)
        is OpenBackup -> ActivityLauncher.viewBackupList(activity, action.site)
        is OpenScan -> ActivityLauncher.viewScan(activity, action.site)
        is OpenPlan -> ActivityLauncher.viewBlogPlans(activity, action.site)
        is OpenPosts -> ActivityLauncher.viewCurrentBlogPosts(requireActivity(), action.site)
        is OpenPages -> ActivityLauncher.viewCurrentBlogPages(requireActivity(), action.site)
        is OpenAdmin -> ActivityLauncher.viewBlogAdmin(activity, action.site)
        is OpenPeople -> ActivityLauncher.viewCurrentBlogPeople(activity, action.site)
        is OpenSharing -> ActivityLauncher.viewBlogSharing(activity, action.site)
        is OpenSiteSettings -> ActivityLauncher.viewBlogSettingsForResult(activity, action.site)
        is OpenThemes -> ActivityLauncher.viewCurrentBlogThemes(activity, action.site)
        is OpenPlugins -> ActivityLauncher.viewPluginBrowser(activity, action.site)
        is OpenMedia -> ActivityLauncher.viewCurrentBlogMedia(activity, action.site)
        is OpenComments -> ActivityLauncher.viewCurrentBlogComments(activity, action.site)
        is OpenUnifiedComments -> ActivityLauncher.viewUnifiedComments(activity, action.site)
        is OpenStats -> ActivityLauncher.viewBlogStats(activity, action.site)
        is ConnectJetpackForStats -> ActivityLauncher.viewConnectJetpackForStats(activity, action.site)
        is StartWPComLoginForJetpackStats -> ActivityLauncher.loginForJetpackStats(this@ImprovedMySiteFragment)
        is OpenJetpackSettings -> ActivityLauncher.viewJetpackSecuritySettings(activity, action.site)
        is OpenStories -> ActivityLauncher.viewStories(activity, action.site, action.event)
        is AddNewStory -> ActivityLauncher.addNewStoryForResult(activity, action.site, action.source)
        is AddNewStoryWithMediaIds -> ActivityLauncher.addNewStoryWithMediaIdsForResult(
                activity,
                action.site,
                action.source,
                action.mediaIds.toLongArray()
        )
        is AddNewStoryWithMediaUris -> ActivityLauncher.addNewStoryWithMediaUrisForResult(
                activity,
                action.site,
                action.source,
                action.mediaUris.toTypedArray()
        )
        is OpenDomainRegistration -> ActivityLauncher.viewDomainRegistrationActivityForResult(
                activity,
                action.site,
                CTA_DOMAIN_CREDIT_REDEMPTION
        )
        is AddNewSite -> SitePickerActivity.addSite(activity, action.isSignedInWpCom)
    }

    private fun handleUploadedItem(itemUploadedModel: ItemUploadedModel) = when (itemUploadedModel) {
        is ItemUploadedModel.PostUploaded -> {
            uploadUtilsWrapper.onPostUploadedSnackbarHandler(
                    activity,
                    requireActivity().findViewById(R.id.coordinator),
                    isError = true,
                    isFirstTimePublish = false,
                    post = itemUploadedModel.post,
                    errorMessage = itemUploadedModel.errorMessage,
                    site = itemUploadedModel.site
            )
        }
        is ItemUploadedModel.MediaUploaded -> {
            uploadUtilsWrapper.onMediaUploadedSnackbarHandler(
                    activity,
                    requireActivity().findViewById(R.id.coordinator),
                    isError = true,
                    mediaList = itemUploadedModel.media,
                    site = itemUploadedModel.site,
                    messageForUser = itemUploadedModel.errorMessage
            )
        }
    }

    private fun startCropActivity(imageUri: UriWrapper) {
        val context = activity ?: return
        val options = Options()
        options.setShowCropGrid(false)
        options.setStatusBarColor(context.getColorFromAttribute(android.R.attr.statusBarColor))
        options.setToolbarColor(context.getColorFromAttribute(R.attr.wpColorAppBar))
        options.setToolbarWidgetColor(context.getColorFromAttribute(attr.colorOnSurface))
        options.setAllowedGestures(UCropActivity.SCALE, UCropActivity.NONE, UCropActivity.NONE)
        options.setHideBottomControls(true)
        UCrop.of(imageUri.uri, Uri.fromFile(File(context.cacheDir, "cropped_for_site_icon.jpg")))
                .withAspectRatio(1f, 1f)
                .withOptions(options)
                .start(requireActivity(), this)
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
    }

    override fun onPause() {
        super.onPause()
        activity?.let {
            if (!it.isChangingConfigurations) {
                viewModel.clearActiveQuickStartTask()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding?.recyclerView?.layoutManager?.let {
            outState.putParcelable(KEY_LIST_STATE, it.onSaveInstanceState())
        }
        (binding?.recyclerView?.adapter as? MySiteAdapter)?.let {
            outState.putBundle(KEY_NESTED_LISTS_STATES, it.onSaveInstanceState())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun NewMySiteFragmentBinding.loadGravatar(avatarUrl: String) =
            root.findViewById<ImageView>(R.id.avatar)?.let {
                meGravatarLoader.load(
                        false,
                        meGravatarLoader.constructGravatarUrl(avatarUrl),
                        null,
                        it,
                        USER,
                        null
                )
            }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data == null) {
            return
        }
        when (requestCode) {
            RequestCodes.DO_LOGIN -> if (resultCode == Activity.RESULT_OK) {
                viewModel.handleSuccessfulLoginResult()
            }
            RequestCodes.SITE_ICON_PICKER -> {
                if (resultCode != Activity.RESULT_OK) {
                    return
                }
                when {
                    data.hasExtra(MediaPickerConstants.EXTRA_MEDIA_ID) -> {
                        val mediaId = data.getLongExtra(MediaPickerConstants.EXTRA_MEDIA_ID, 0)
                        viewModel.handleSelectedSiteIcon(mediaId)
                    }
                    data.hasExtra(MediaPickerConstants.EXTRA_MEDIA_URIS) -> {
                        val mediaUriStringsArray = data.getStringArrayExtra(
                                MediaPickerConstants.EXTRA_MEDIA_URIS
                        ) ?: return

                        val source = PhotoPickerMediaSource.fromString(
                                data.getStringExtra(MediaPickerConstants.EXTRA_MEDIA_SOURCE)
                        )
                        val iconUrl = mediaUriStringsArray.getOrNull(0) ?: return
                        viewModel.handleTakenSiteIcon(iconUrl, source)
                    }
                    else -> {
                        AppLog.e(
                                UTILS,
                                "Can't resolve picked or captured image"
                        )
                    }
                }
            }
            RequestCodes.STORIES_PHOTO_PICKER,
            RequestCodes.PHOTO_PICKER -> if (resultCode == Activity.RESULT_OK) {
                viewModel.handleStoriesPhotoPickerResult(data)
            }
            UCrop.REQUEST_CROP -> {
                if (resultCode == UCrop.RESULT_ERROR) {
                    AppLog.e(
                            MAIN,
                            "Image cropping failed!",
                            UCrop.getError(data)
                    )
                }
                viewModel.handleCropResult(UCrop.getOutput(data), resultCode == Activity.RESULT_OK)
            }
            RequestCodes.DOMAIN_REGISTRATION -> if (resultCode == Activity.RESULT_OK) {
                viewModel.handleSuccessfulDomainRegistrationResult(data.getStringExtra(RESULT_REGISTERED_DOMAIN_EMAIL))
            }
            RequestCodes.CREATE_SITE -> {
                viewModel.startQuickStart(data.getIntExtra(SitePickerActivity.KEY_LOCAL_ID, -1))
            }
            RequestCodes.SITE_PICKER -> {
                if (data.getIntExtra(WPMainActivity.ARG_CREATE_SITE, 0) == RequestCodes.CREATE_SITE) {
                    viewModel.startQuickStart(data.getIntExtra(SitePickerActivity.KEY_LOCAL_ID, -1))
                }
            }
        }
    }

    private fun NewMySiteFragmentBinding.loadData(items: List<MySiteItem>) {
        recyclerView.setVisible(true)
        actionableEmptyView.setVisible(false)
        (recyclerView.adapter as? MySiteAdapter)?.loadData(items)
    }

    private fun NewMySiteFragmentBinding.loadEmptyView(shouldShowEmptyViewImage: Boolean) {
        recyclerView.setVisible(false)
        actionableEmptyView.setVisible(true)
        actionableEmptyView.image.setVisible(shouldShowEmptyViewImage)
    }

    private fun showSnackbar(holder: SnackbarMessageHolder) {
        activity?.let { parent ->
            snackbarSequencer.enqueue(
                    SnackbarItem(
                            Info(
                                    view = parent.findViewById(R.id.coordinator),
                                    textRes = holder.message,
                                    duration = Snackbar.LENGTH_LONG
                            ),
                            holder.buttonTitle?.let {
                                Action(
                                        textRes = holder.buttonTitle,
                                        clickListener = { holder.buttonAction() }
                                )
                            },
                            dismissCallback = { _, _ -> holder.onDismissAction() }
                    )
            )
        }
    }

    companion object {
        private const val KEY_LIST_STATE = "key_list_state"
        private const val KEY_NESTED_LISTS_STATES = "key_nested_lists_states"
        fun newInstance(): ImprovedMySiteFragment {
            return ImprovedMySiteFragment()
        }
    }

    override fun onSuccessfulInput(input: String, callbackId: Int) {
        viewModel.onSiteNameChosen(input)
    }

    override fun onTextInputDialogDismissed(callbackId: Int) {
        viewModel.onSiteNameChooserDismissed()
    }
}
