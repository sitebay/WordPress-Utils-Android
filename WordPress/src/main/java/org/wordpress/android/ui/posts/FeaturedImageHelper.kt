package org.sitebay.android.ui.posts

import android.net.Uri
import android.text.TextUtils
import dagger.Reusable
import org.sitebay.android.R
import org.sitebay.android.analytics.AnalyticsTracker.Stat
import org.sitebay.android.fluxc.Dispatcher
import org.sitebay.android.fluxc.generated.MediaActionBuilder
import org.sitebay.android.fluxc.model.MediaModel
import org.sitebay.android.fluxc.model.MediaModel.MediaUploadState
import org.sitebay.android.fluxc.model.PostImmutableModel
import org.sitebay.android.fluxc.model.SiteModel
import org.sitebay.android.fluxc.store.MediaStore
import org.sitebay.android.fluxc.store.MediaStore.CancelMediaPayload
import org.sitebay.android.fluxc.store.UploadStore
import org.sitebay.android.ui.reader.utils.ReaderUtilsWrapper
import org.sitebay.android.ui.uploads.UploadServiceFacade
import org.sitebay.android.util.AppLog
import org.sitebay.android.util.AppLog.T
import org.sitebay.android.util.FluxCUtilsWrapper
import org.sitebay.android.util.SiteUtilsWrapper
import org.sitebay.android.util.StringUtils
import org.sitebay.android.util.analytics.AnalyticsTrackerWrapper
import org.sitebay.android.viewmodel.ResourceProvider
import java.util.ArrayList
import javax.inject.Inject

const val EMPTY_LOCAL_POST_ID = -1

/**
 * Helper class for separating logic related to FeaturedImage upload.
 *
 * This class is not testable at the moment, since it uses Static methods and Android dependencies.
 * However, it at least separates this piece of business logic from the view layer.
 */
@Reusable
class FeaturedImageHelper @Inject constructor(
    private val uploadStore: UploadStore,
    private val mediaStore: MediaStore,
    private val uploadServiceFacade: UploadServiceFacade,
    private val resourceProvider: ResourceProvider,
    private val readerUtilsWrapper: ReaderUtilsWrapper,
    private val fluxCUtilsWrapper: FluxCUtilsWrapper,
    private val siteUtilsWrapper: SiteUtilsWrapper,
    private val dispatcher: Dispatcher,
    private val analyticsTrackerWrapper: AnalyticsTrackerWrapper
) {
    fun getFailedFeaturedImageUpload(post: PostImmutableModel): MediaModel? {
        val failedMediaForPost = uploadStore.getFailedMediaForPost(post)
        for (item in failedMediaForPost) {
            if (item != null && item.markedLocallyAsFeatured) {
                return item
            }
        }
        return null
    }

    fun retryFeaturedImageUpload(
        site: SiteModel,
        post: PostImmutableModel
    ): MediaModel? {
        val mediaModel = getFailedFeaturedImageUpload(post)
        if (mediaModel != null) {
            uploadServiceFacade.cancelFinalNotification(post)
            uploadServiceFacade.cancelFinalNotificationForMedia(site)
            mediaModel.setUploadState(MediaUploadState.QUEUED)
            dispatcher.dispatch(MediaActionBuilder.newUpdateMediaAction(mediaModel))
            startUploadService(mediaModel)

            trackFeaturedImageEvent(TrackableEvent.IMAGE_UPLOAD_RETRY_CLICKED, post.id)
        }
        return mediaModel
    }

    private fun startUploadService(media: MediaModel) {
        val mediaList = ArrayList<MediaModel>()
        mediaList.add(media)
        uploadServiceFacade.uploadMedia(mediaList)
    }

    fun queueFeaturedImageForUpload(
        localPostId: Int,
        site: SiteModel,
        uri: Uri,
        mimeType: String?
    ): EnqueueFeaturedImageResult {
        val media = fluxCUtilsWrapper.mediaModelFromLocalUri(uri, mimeType, site.id)
                ?: return EnqueueFeaturedImageResult.FILE_NOT_FOUND
        if (localPostId != EMPTY_LOCAL_POST_ID) {
            media.localPostId = localPostId
        } else {
            AppLog.e(T.MEDIA, "Upload featured image can't be invoked without a valid local post id.")
            return EnqueueFeaturedImageResult.INVALID_POST_ID
        }
        media.markedLocallyAsFeatured = true

        dispatcher.dispatch(MediaActionBuilder.newUpdateMediaAction(media))
        startUploadService(media)
        return EnqueueFeaturedImageResult.SUCCESS
    }

    fun queueFeaturedImageForUpload(
        localPostId: Int,
        media: MediaModel
    ): EnqueueFeaturedImageResult {
        if (localPostId != EMPTY_LOCAL_POST_ID) {
            media.localPostId = localPostId
        } else {
            AppLog.e(T.MEDIA, "Upload featured image can't be invoked without a valid local post id.")
            return EnqueueFeaturedImageResult.INVALID_POST_ID
        }
        media.markedLocallyAsFeatured = true

        dispatcher.dispatch(MediaActionBuilder.newUpdateMediaAction(media))
        startUploadService(media)
        return EnqueueFeaturedImageResult.SUCCESS
    }

    fun cancelFeaturedImageUpload(
        site: SiteModel,
        post: PostImmutableModel,
        cancelFailedOnly: Boolean
    ) {
        var mediaModel: MediaModel? = getFailedFeaturedImageUpload(post)
        if (!cancelFailedOnly && mediaModel == null) {
            mediaModel = uploadServiceFacade.getPendingOrInProgressFeaturedImageUploadForPost(post)
        }
        if (mediaModel != null) {
            val payload = CancelMediaPayload(site, mediaModel, true)
            dispatcher.dispatch(MediaActionBuilder.newCancelMediaUploadAction(payload))
            uploadServiceFacade.cancelFinalNotification(post)
            uploadServiceFacade.cancelFinalNotificationForMedia(site)

            trackFeaturedImageEvent(TrackableEvent.IMAGE_UPLOAD_CANCELED, post.id)
        }
    }

    fun createCurrentFeaturedImageState(site: SiteModel, post: PostImmutableModel): FeaturedImageData {
        var uploadModel: MediaModel? = uploadServiceFacade.getPendingOrInProgressFeaturedImageUploadForPost(post)
        if (uploadModel != null) {
            return FeaturedImageData(FeaturedImageState.IMAGE_UPLOAD_IN_PROGRESS, uploadModel.filePath)
        }
        uploadModel = getFailedFeaturedImageUpload(post)
        if (uploadModel != null) {
            return FeaturedImageData(FeaturedImageState.IMAGE_UPLOAD_FAILED, uploadModel.filePath)
        }
        if (!post.hasFeaturedImage()) {
            return FeaturedImageData(FeaturedImageState.IMAGE_EMPTY, null)
        }

        val media = mediaStore.getSiteMediaWithId(site, post.featuredImageId) ?: return FeaturedImageData(
                FeaturedImageState.IMAGE_EMPTY,
                null
        )

        // Get max width/height for photon thumbnail - we load a smaller image so it's loaded quickly
        val maxDimen = resourceProvider.getDimension(R.dimen.post_settings_featured_image_height_min).toInt()

        val mediaUri = StringUtils.notNullStr(
                if (TextUtils.isEmpty(media.thumbnailUrl)) {
                    media.url
                } else {
                    media.thumbnailUrl
                }
        )

        val photonUrl = readerUtilsWrapper.getResizedImageUrl(
                mediaUri,
                maxDimen,
                maxDimen,
                siteUtilsWrapper.getAccessibilityInfoFromSite(site)
        )
        return FeaturedImageData(FeaturedImageState.REMOTE_IMAGE_LOADING, photonUrl)
    }

    fun trackFeaturedImageEvent(
        event: TrackableEvent,
        postId: Int
    ) = analyticsTrackerWrapper.track(event.label, mapOf(POST_ID_KEY to postId))

    data class FeaturedImageData(val uiState: FeaturedImageState, val mediaUri: String?)

    enum class FeaturedImageState(
        val buttonVisible: Boolean = false,
        val imageViewVisible: Boolean = false,
        val localImageViewVisible: Boolean = false,
        val progressOverlayVisible: Boolean = false,
        val retryOverlayVisible: Boolean = false
    ) {
        IMAGE_EMPTY(buttonVisible = true),
        REMOTE_IMAGE_LOADING(localImageViewVisible = true, imageViewVisible = true),
        REMOTE_IMAGE_SET(imageViewVisible = true),
        IMAGE_UPLOAD_IN_PROGRESS(localImageViewVisible = true, progressOverlayVisible = true),
        IMAGE_UPLOAD_FAILED(localImageViewVisible = true, retryOverlayVisible = true);
    }

    enum class EnqueueFeaturedImageResult {
        FILE_NOT_FOUND, INVALID_POST_ID, SUCCESS
    }

    enum class TrackableEvent(val label: Stat) {
        IMAGE_SET_CLICKED(Stat.FEATURED_IMAGE_SET_CLICKED_POST_SETTINGS),
        IMAGE_PICKED_POST_SETTINGS(Stat.FEATURED_IMAGE_PICKED_POST_SETTINGS),
        IMAGE_PICKED_GUTENBERG_EDITOR(Stat.FEATURED_IMAGE_PICKED_GUTENBERG_EDITOR),
        IMAGE_REMOVED_GUTENBERG_EDITOR(Stat.FEATURED_IMAGE_REMOVED_GUTENBERG_EDITOR),
        IMAGE_UPLOAD_CANCELED(Stat.FEATURED_IMAGE_UPLOAD_CANCELED_POST_SETTINGS),
        IMAGE_UPLOAD_RETRY_CLICKED(Stat.FEATURED_IMAGE_UPLOAD_RETRY_CLICKED_POST_SETTINGS),
        IMAGE_REMOVE_CLICKED(Stat.FEATURED_IMAGE_REMOVE_CLICKED_POST_SETTINGS)
    }

    companion object {
        private const val POST_ID_KEY = "post_id"
    }
}
