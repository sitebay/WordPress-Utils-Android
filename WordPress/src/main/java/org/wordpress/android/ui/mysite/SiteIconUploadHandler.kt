package org.sitebay.android.ui.mysite

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.sitebay.android.analytics.AnalyticsTracker.Stat.MY_SITE_ICON_UPLOADED
import org.sitebay.android.analytics.AnalyticsTracker.Stat.MY_SITE_ICON_UPLOAD_UNSUCCESSFUL
import org.sitebay.android.fluxc.model.MediaModel
import org.sitebay.android.fluxc.model.PostModel
import org.sitebay.android.fluxc.model.SiteModel
import org.sitebay.android.ui.mysite.SiteIconUploadHandler.ItemUploadedModel.MediaUploaded
import org.sitebay.android.ui.mysite.SiteIconUploadHandler.ItemUploadedModel.PostUploaded
import org.sitebay.android.ui.uploads.UploadService.UploadErrorEvent
import org.sitebay.android.ui.uploads.UploadService.UploadMediaSuccessEvent
import org.sitebay.android.util.AppLog
import org.sitebay.android.util.AppLog.T.MAIN
import org.sitebay.android.util.EventBusWrapper
import org.sitebay.android.util.analytics.AnalyticsTrackerWrapper
import org.sitebay.android.viewmodel.Event
import javax.inject.Inject

class SiteIconUploadHandler
@Inject constructor(
    private val selectedSiteRepository: SelectedSiteRepository,
    private val analyticsTrackerWrapper: AnalyticsTrackerWrapper,
    private val eventBusWrapper: EventBusWrapper
) {
    private val _onUploadedItem = MutableLiveData<Event<ItemUploadedModel>>()
    val onUploadedItem = _onUploadedItem as LiveData<Event<ItemUploadedModel>>

    init {
        eventBusWrapper.register(this)
    }

    fun clear() {
        eventBusWrapper.unregister(this)
    }

    sealed class ItemUploadedModel(open val site: SiteModel?, open val errorMessage: String? = null) {
        data class PostUploaded(
            val post: PostModel,
            override val site: SiteModel?,
            override val errorMessage: String? = null
        ) : ItemUploadedModel(site, errorMessage)

        data class MediaUploaded(
            val media: List<MediaModel>,
            override val site: SiteModel?,
            override val errorMessage: String? = null
        ) : ItemUploadedModel(site, errorMessage)
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onEventMainThread(event: UploadErrorEvent) {
        analyticsTrackerWrapper.track(MY_SITE_ICON_UPLOAD_UNSUCCESSFUL)
        eventBusWrapper.removeStickyEvent(event)
        selectedSiteRepository.showSiteIconProgressBar(false)
        val site = selectedSiteRepository.getSelectedSite()
        if (site != null && event.post != null && event.post.localSiteId == site.id) {
            _onUploadedItem.postValue(Event(PostUploaded(event.post, site, event.errorMessage)))
        } else if (event.mediaModelList != null && event.mediaModelList.isNotEmpty()) {
            _onUploadedItem.postValue(Event(MediaUploaded(event.mediaModelList, site, event.errorMessage)))
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onEventMainThread(event: UploadMediaSuccessEvent) {
        analyticsTrackerWrapper.track(MY_SITE_ICON_UPLOADED)
        eventBusWrapper.removeStickyEvent(event)
        val site = selectedSiteRepository.getSelectedSite()
        if (site != null) {
            if (selectedSiteRepository.isSiteIconUploadInProgress()) {
                if (event.mediaModelList.size > 0) {
                    val media = event.mediaModelList[0]
                    selectedSiteRepository.updateSiteIconMediaId(media.mediaId.toInt(), true)
                } else {
                    AppLog.w(
                            MAIN,
                            "Site icon upload completed, but mediaList is empty."
                    )
                }
            } else if (event.mediaModelList != null && event.mediaModelList.isNotEmpty()) {
                _onUploadedItem.postValue(Event(MediaUploaded(event.mediaModelList, site, event.successMessage)))
            }
        }
    }
}
