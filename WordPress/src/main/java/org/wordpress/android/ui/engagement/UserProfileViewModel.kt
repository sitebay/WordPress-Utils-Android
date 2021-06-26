package org.sitebay.android.ui.engagement

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.sitebay.android.R
import org.sitebay.android.ui.engagement.BottomSheetAction.HideBottomSheet
import org.sitebay.android.ui.engagement.BottomSheetAction.ShowBottomSheet
import org.sitebay.android.ui.engagement.BottomSheetUiState.UserProfileUiState
import org.sitebay.android.ui.engagement.EngagedListNavigationEvent.OpenUserProfileBottomSheet.UserProfile
import org.sitebay.android.ui.engagement.EngagementNavigationSource.LIKE_NOTIFICATION_LIST
import org.sitebay.android.ui.engagement.EngagementNavigationSource.LIKE_READER_LIST
import org.sitebay.android.ui.reader.tracker.ReaderTracker
import org.sitebay.android.util.analytics.AnalyticsUtilsWrapper
import org.sitebay.android.viewmodel.Event
import org.sitebay.android.viewmodel.ResourceProvider
import javax.inject.Inject

class UserProfileViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val analyticsUtilsWrapper: AnalyticsUtilsWrapper
) : ViewModel() {
    private val _onBottomSheetAction = MutableLiveData<Event<BottomSheetAction>>()
    val onBottomSheetAction: LiveData<Event<BottomSheetAction>> = _onBottomSheetAction

    private val _bottomSheetUiState = MutableLiveData<BottomSheetUiState>()
    val bottomSheetUiState: LiveData<BottomSheetUiState> = _bottomSheetUiState

    fun onBottomSheetOpen(
        userProfile: UserProfile,
        onClick: ((siteId: Long, siteUrl: String, source: String) -> Unit)?,
        source: EngagementNavigationSource?
    ) {
        _bottomSheetUiState.value = with(userProfile) {
            UserProfileUiState(
                    userAvatarUrl = userAvatarUrl,
                    blavatarUrl = blavatarUrl,
                    userName = userName,
                    userLogin = userLogin,
                    userBio = userBio,
                    siteTitle = if (siteTitle.isBlank()) {
                        resourceProvider.getString(R.string.user_profile_untitled_site)
                    } else {
                        siteTitle
                    },
                    siteUrl = siteUrl,
                    siteId = siteId,
                    onSiteClickListener = onClick,
                    blogPreviewSource = source?.let {
                        when (it) {
                            LIKE_NOTIFICATION_LIST -> ReaderTracker.SOURCE_NOTIF_LIKE_LIST_USER_PROFILE
                            LIKE_READER_LIST -> ReaderTracker.SOURCE_READER_LIKE_LIST_USER_PROFILE
                        }
                    } ?: ReaderTracker.SOURCE_USER_PROFILE_UNKNOWN
            )
        }
        analyticsUtilsWrapper.trackUserProfileShown(EngagementNavigationSource.getSourceDescription(source))
        _onBottomSheetAction.value = Event(ShowBottomSheet)
    }

    fun onBottomSheetCancelled() {
        _onBottomSheetAction.value = Event(HideBottomSheet)
    }

    companion object {
        const val USER_PROFILE_VM_KEY = "USER_PROFILE_VIEW_MODEL_KEY"
    }
}
