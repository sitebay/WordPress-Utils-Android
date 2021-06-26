package org.sitebay.android.ui.mediapicker.insert

import kotlinx.coroutines.flow.flow
import org.sitebay.android.R
import org.sitebay.android.analytics.AnalyticsTracker
import org.sitebay.android.analytics.AnalyticsTracker.Stat.STOCK_MEDIA_UPLOADED
import org.sitebay.android.fluxc.model.MediaModel
import org.sitebay.android.fluxc.model.SiteModel
import org.sitebay.android.fluxc.store.StockMediaStore
import org.sitebay.android.fluxc.store.StockMediaUploadItem
import org.sitebay.android.ui.mediapicker.MediaItem.Identifier
import org.sitebay.android.ui.mediapicker.MediaItem.Identifier.StockMediaIdentifier
import org.sitebay.android.ui.mediapicker.insert.MediaInsertHandler.InsertModel
import org.sitebay.android.util.AppLog
import org.sitebay.android.util.AppLog.T.MEDIA
import java.util.HashMap
import javax.inject.Inject

class StockMediaInsertUseCase(
    private val site: SiteModel,
    private val stockMediaStore: StockMediaStore
) : MediaInsertUseCase {
    override val actionTitle: Int
        get() = R.string.media_uploading_stock_library_photo

    override suspend fun insert(identifiers: List<Identifier>) = flow {
        emit(InsertModel.Progress(actionTitle))
        val result = stockMediaStore.performUploadStockMedia(site, identifiers.mapNotNull { identifier ->
            (identifier as? StockMediaIdentifier)?.let {
                StockMediaUploadItem(it.name, it.title, it.url)
            }
        })
        emit(when {
            result.error != null -> InsertModel.Error(result.error.message)
            else -> {
                trackUploadedStockMediaEvent(result.mediaList)
                InsertModel.Success(result.mediaList.mapNotNull { Identifier.RemoteId(it.mediaId) })
            }
        })
    }

    private fun trackUploadedStockMediaEvent(mediaList: List<MediaModel>) {
        if (mediaList.isEmpty()) {
            AppLog.e(MEDIA, "Cannot track uploaded stock media event if mediaList is empty")
            return
        }
        val isMultiselect = mediaList.size > 1
        val properties: MutableMap<String, Any?> = HashMap()
        properties["is_part_of_multiselection"] = isMultiselect
        properties["number_of_media_selected"] = mediaList.size
        AnalyticsTracker.track(STOCK_MEDIA_UPLOADED, properties)
    }

    class StockMediaInsertUseCaseFactory
    @Inject constructor(
        private val stockMediaStore: StockMediaStore
    ) {
        fun build(site: SiteModel): StockMediaInsertUseCase {
            return StockMediaInsertUseCase(site, stockMediaStore)
        }
    }
}
