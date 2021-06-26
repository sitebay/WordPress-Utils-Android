package org.sitebay.android.ui.stats.refresh.lists.sections.insights.usecases

import kotlinx.coroutines.CoroutineDispatcher
import org.sitebay.android.R
import org.sitebay.android.analytics.AnalyticsTracker.Stat
import org.sitebay.android.fluxc.store.StatsStore.ManagementType
import org.sitebay.android.modules.BG_THREAD
import org.sitebay.android.modules.UI_THREAD
import org.sitebay.android.ui.stats.refresh.lists.sections.BaseStatsUseCase.StatelessUseCase
import org.sitebay.android.ui.stats.refresh.lists.sections.BlockListItem
import org.sitebay.android.ui.stats.refresh.lists.sections.BlockListItem.BigTitle
import org.sitebay.android.ui.stats.refresh.lists.sections.BlockListItem.DialogButtons
import org.sitebay.android.ui.stats.refresh.lists.sections.BlockListItem.ImageItem
import org.sitebay.android.ui.utils.ListItemInteraction
import org.sitebay.android.ui.stats.refresh.lists.sections.BlockListItem.Tag
import org.sitebay.android.ui.stats.refresh.lists.sections.BlockListItem.Text
import org.sitebay.android.ui.stats.refresh.utils.NewsCardHandler
import org.sitebay.android.util.analytics.AnalyticsTrackerWrapper
import org.sitebay.android.viewmodel.ResourceProvider
import javax.inject.Inject
import javax.inject.Named

class ManagementNewsCardUseCase
@Inject constructor(
    @Named(UI_THREAD) private val mainDispatcher: CoroutineDispatcher,
    @Named(BG_THREAD) private val backgroundDispatcher: CoroutineDispatcher,
    private val resourceProvider: ResourceProvider,
    private val newsCardHandler: NewsCardHandler,
    private val analyticsTrackerWrapper: AnalyticsTrackerWrapper
) : StatelessUseCase<Boolean>(ManagementType.NEWS_CARD, mainDispatcher, backgroundDispatcher, listOf()) {
    override suspend fun loadCachedData() = true

    override suspend fun fetchRemoteData(forced: Boolean): State<Boolean> = State.Data(true)

    override fun buildLoadingItem(): List<BlockListItem> = listOf()

    override fun buildUiModel(domainModel: Boolean): List<BlockListItem> {
        val highlightedText = resourceProvider.getString(R.string.stats_management_add_new_stats_card)
        val newsCardMessage = resourceProvider.getString(R.string.stats_management_news_card_message, highlightedText)
        return listOf(
                ImageItem(R.drawable.insights_management_feature_image),
                Tag(R.string.stats_management_new),
                BigTitle(R.string.stats_manage_your_stats),
                Text(text = newsCardMessage, bolds = listOf(highlightedText)),
                DialogButtons(
                        R.string.stats_management_try_it_now,
                        ListItemInteraction.create(this::onEditInsights),
                        R.string.stats_management_dismiss_insights_news_card,
                        ListItemInteraction.create(this::onDismiss)
                )
        )
    }

    private fun onEditInsights() {
        analyticsTrackerWrapper.track(Stat.STATS_INSIGHTS_MANAGEMENT_HINT_CLICKED)
        newsCardHandler.goToEdit()
    }

    private fun onDismiss() {
        analyticsTrackerWrapper.track(Stat.STATS_INSIGHTS_MANAGEMENT_HINT_DISMISSED)
        newsCardHandler.dismiss()
    }
}
