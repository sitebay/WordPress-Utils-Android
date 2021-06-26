package org.sitebay.android.ui.activitylog.detail

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import org.sitebay.android.R
import org.sitebay.android.WordPress
import org.sitebay.android.databinding.ActivityLogItemDetailBinding
import org.sitebay.android.fluxc.model.SiteModel
import org.sitebay.android.ui.ActivityLauncher
import org.sitebay.android.ui.ActivityLauncher.SOURCE_TRACK_EVENT_PROPERTY_KEY
import org.sitebay.android.ui.RequestCodes
import org.sitebay.android.ui.activitylog.detail.ActivityLogDetailNavigationEvents.ShowBackupDownload
import org.sitebay.android.ui.activitylog.detail.ActivityLogDetailNavigationEvents.ShowRestore
import org.sitebay.android.ui.notifications.blocks.NoteBlockClickableSpan
import org.sitebay.android.ui.notifications.utils.FormattableContentClickHandler
import org.sitebay.android.ui.notifications.utils.NotificationsUtilsWrapper
import org.sitebay.android.ui.reader.tracker.ReaderTracker
import org.sitebay.android.ui.utils.UiHelpers
import org.sitebay.android.util.image.ImageManager
import org.sitebay.android.util.image.ImageType.AVATAR_WITH_BACKGROUND
import org.sitebay.android.viewmodel.activitylog.ACTIVITY_LOG_ARE_BUTTONS_VISIBLE_KEY
import org.sitebay.android.viewmodel.activitylog.ACTIVITY_LOG_ID_KEY
import org.sitebay.android.viewmodel.activitylog.ActivityLogDetailViewModel
import org.sitebay.android.viewmodel.observeEvent
import javax.inject.Inject

private const val DETAIL_TRACKING_SOURCE = "detail"
private const val FORWARD_SLASH = "/"

class ActivityLogDetailFragment : Fragment(R.layout.activity_log_item_detail) {
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var imageManager: ImageManager
    @Inject lateinit var notificationsUtilsWrapper: NotificationsUtilsWrapper
    @Inject lateinit var formattableContentClickHandler: FormattableContentClickHandler
    @Inject lateinit var uiHelpers: UiHelpers

    private lateinit var viewModel: ActivityLogDetailViewModel

    companion object {
        fun newInstance(): ActivityLogDetailFragment {
            return ActivityLogDetailFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity?.application as WordPress).component()?.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let { activity ->
            viewModel = ViewModelProvider(activity, viewModelFactory)
                    .get(ActivityLogDetailViewModel::class.java)
            with(ActivityLogItemDetailBinding.bind(view)) {
                val (site, activityLogId) = sideAndActivityId(savedInstanceState, activity.intent)
                val areButtonsVisible = areButtonsVisible(savedInstanceState, activity.intent)

                viewModel.activityLogItem.observe(viewLifecycleOwner, { activityLogModel ->
                    loadLogItem(activityLogModel, activity)
                })

                viewModel.restoreVisible.observe(viewLifecycleOwner, { available ->
                    activityRestoreButton.visibility = if (available == true) View.VISIBLE else View.GONE
                })
                viewModel.downloadBackupVisible.observe(viewLifecycleOwner, { available ->
                    activityDownloadBackupButton.visibility = if (available == true) View.VISIBLE else View.GONE
                })

            viewModel.navigationEvents.observeEvent(viewLifecycleOwner, {
                when (it) {
                    is ShowBackupDownload -> ActivityLauncher.showBackupDownloadForResult(
                            requireActivity(),
                            viewModel.site,
                            it.model.activityID,
                            RequestCodes.BACKUP_DOWNLOAD,
                            buildTrackingSource()
                    )
                    is ShowRestore -> ActivityLauncher.showRestoreForResult(
                            requireActivity(),
                            viewModel.site,
                            it.model.activityID,
                            RequestCodes.RESTORE,
                            buildTrackingSource()
                    )
                }
            })

                viewModel.handleFormattableRangeClick.observe(viewLifecycleOwner, { range ->
                    if (range != null) {
                        formattableContentClickHandler.onClick(
                                activity,
                                range,
                                ReaderTracker.SOURCE_ACTIVITY_LOG_DETAIL
                        )
                    }
                })

                viewModel.start(site, activityLogId, areButtonsVisible)
            }
        }
    }

    private fun ActivityLogItemDetailBinding.loadLogItem(
        activityLogModel: ActivityLogDetailModel?,
        activity: FragmentActivity
    ) {
        setActorIcon(activityLogModel?.actorIconUrl, activityLogModel?.showJetpackIcon)
        uiHelpers.setTextOrHide(activityActorName, activityLogModel?.actorName)
        uiHelpers.setTextOrHide(activityActorRole, activityLogModel?.actorRole)

        val spannable = activityLogModel?.content?.let {
            notificationsUtilsWrapper.getSpannableContentForRanges(
                    it,
                    activityMessage,
                    { range ->
                        viewModel.onRangeClicked(range)
                    },
                    false
            )
        }

        val noteBlockSpans = spannable?.getSpans(
                0,
                spannable.length,
                NoteBlockClickableSpan::class.java
        )

        noteBlockSpans?.forEach {
            it.enableColors(activity)
        }

        uiHelpers.setTextOrHide(activityMessage, spannable)
        uiHelpers.setTextOrHide(activityType, activityLogModel?.summary)

        activityCreatedDate.text = activityLogModel?.createdDate
        activityCreatedTime.text = activityLogModel?.createdTime

        if (activityLogModel != null) {
            activityRestoreButton.setOnClickListener {
                viewModel.onRestoreClicked(activityLogModel)
            }
            activityDownloadBackupButton.setOnClickListener {
                viewModel.onDownloadBackupClicked(activityLogModel)
            }
        }
    }

    private fun sideAndActivityId(savedInstanceState: Bundle?, intent: Intent?) = when {
        savedInstanceState != null -> {
            val site = savedInstanceState.getSerializable(WordPress.SITE) as SiteModel
            val activityLogId = requireNotNull(
                    savedInstanceState.getString(
                            ACTIVITY_LOG_ID_KEY
                    )
            )
            site to activityLogId
        }
        intent != null -> {
            val site = intent.getSerializableExtra(WordPress.SITE) as SiteModel
            val activityLogId = intent.getStringExtra(ACTIVITY_LOG_ID_KEY) as String
            site to activityLogId
        }
        else -> throw Throwable("Couldn't initialize Activity Log view model")
    }

    private fun areButtonsVisible(savedInstanceState: Bundle?, intent: Intent?) = when {
        savedInstanceState != null ->
            requireNotNull(savedInstanceState.getBoolean(ACTIVITY_LOG_ARE_BUTTONS_VISIBLE_KEY, true))
        intent != null ->
            intent.getBooleanExtra(ACTIVITY_LOG_ARE_BUTTONS_VISIBLE_KEY, true)
        else -> throw Throwable("Couldn't initialize Activity Log view model")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(WordPress.SITE, viewModel.site)
        outState.putString(ACTIVITY_LOG_ID_KEY, viewModel.activityLogId)
        outState.putBoolean(ACTIVITY_LOG_ARE_BUTTONS_VISIBLE_KEY, viewModel.areButtonsVisible)
    }

    private fun ActivityLogItemDetailBinding.setActorIcon(actorIcon: String?, showJetpackIcon: Boolean?) {
        when {
            actorIcon != null && actorIcon != "" -> {
                imageManager.loadIntoCircle(activityActorIcon, AVATAR_WITH_BACKGROUND, actorIcon)
                activityActorIcon.visibility = View.VISIBLE
                activityJetpackActorIcon.visibility = View.GONE
            }
            showJetpackIcon == true -> {
                activityJetpackActorIcon.visibility = View.VISIBLE
                activityActorIcon.visibility = View.GONE
            }
            else -> {
                imageManager.cancelRequestAndClearImageView(activityActorIcon)
                activityActorIcon.visibility = View.GONE
                activityJetpackActorIcon.visibility = View.GONE
            }
        }
    }

    private fun buildTrackingSource() = requireActivity().intent?.extras?.let {
        val source = it.getString(SOURCE_TRACK_EVENT_PROPERTY_KEY)
        when {
            source != null -> source + FORWARD_SLASH + DETAIL_TRACKING_SOURCE
            else -> DETAIL_TRACKING_SOURCE
        }
    }
}
