package org.sitebay.android.ui.posts

import android.text.TextUtils
import org.sitebay.android.R
import org.sitebay.android.fluxc.model.PostImmutableModel
import org.sitebay.android.fluxc.model.post.PostStatus
import org.sitebay.android.ui.stats.refresh.utils.DateUtils
import org.sitebay.android.viewmodel.ResourceProvider
import javax.inject.Inject

class PostSettingsUtils
@Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val dateUtils: DateUtils,
    private val postUtilsWrapper: PostUtilsWrapper
) {
    fun getPublishDateLabel(
        postModel: PostImmutableModel
    ): String {
        val labelToUse: String
        val dateCreated = postModel.dateCreated
        val status = PostStatus.fromPost(postModel)
        if (!TextUtils.isEmpty(dateCreated)) {
            val formattedDate = dateUtils.formatDateTime(dateCreated)

            if (postModel.isLocalDraft) {
                if (postUtilsWrapper.isPublishDateInThePast(postModel.dateCreated)) {
                    labelToUse = resourceProvider.getString(R.string.backdated_for, formattedDate)
                } else if (postUtilsWrapper.isPublishDateInTheFuture(postModel.dateCreated)) {
                    labelToUse = resourceProvider.getString(R.string.schedule_for, formattedDate)
                } else if (postUtilsWrapper.shouldPublishImmediately(status, postModel.dateCreated)) {
                    labelToUse = resourceProvider.getString(R.string.immediately)
                } else {
                    labelToUse = resourceProvider.getString(R.string.publish_on, formattedDate)
                }
            } else if (status == PostStatus.SCHEDULED) {
                labelToUse = resourceProvider.getString(R.string.scheduled_for, formattedDate)
            } else if (status == PostStatus.PUBLISHED || status == PostStatus.PRIVATE) {
                labelToUse = resourceProvider.getString(R.string.published_on, formattedDate)
            } else if (postUtilsWrapper.isPublishDateInTheFuture(postModel.dateCreated)) {
                labelToUse = resourceProvider.getString(R.string.schedule_for, formattedDate)
            } else {
                labelToUse = resourceProvider.getString(R.string.publish_on, formattedDate)
            }
        } else if (postUtilsWrapper.shouldPublishImmediatelyOptionBeAvailable(status)) {
            labelToUse = resourceProvider.getString(R.string.immediately)
        } else {
            labelToUse = resourceProvider.getString(R.string.immediately)
        }
        return labelToUse
    }
}
