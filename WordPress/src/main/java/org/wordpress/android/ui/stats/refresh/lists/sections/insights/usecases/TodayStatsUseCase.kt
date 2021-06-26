package org.sitebay.android.ui.stats.refresh.lists.sections.insights.usecases

import android.view.View
import kotlinx.coroutines.CoroutineDispatcher
import org.sitebay.android.R
import org.sitebay.android.R.string
import org.sitebay.android.fluxc.model.stats.VisitsModel
import org.sitebay.android.fluxc.store.StatsStore.InsightType.TODAY_STATS
import org.sitebay.android.fluxc.store.stats.insights.TodayInsightsStore
import org.sitebay.android.modules.BG_THREAD
import org.sitebay.android.modules.UI_THREAD
import org.sitebay.android.ui.stats.refresh.lists.sections.BaseStatsUseCase.StatelessUseCase
import org.sitebay.android.ui.stats.refresh.lists.sections.BlockListItem
import org.sitebay.android.ui.stats.refresh.lists.sections.BlockListItem.Empty
import org.sitebay.android.ui.stats.refresh.lists.sections.BlockListItem.QuickScanItem
import org.sitebay.android.ui.stats.refresh.lists.sections.BlockListItem.QuickScanItem.Column
import org.sitebay.android.ui.stats.refresh.lists.sections.BlockListItem.Title
import org.sitebay.android.ui.stats.refresh.lists.widget.WidgetUpdater.StatsWidgetUpdaters
import org.sitebay.android.ui.stats.refresh.utils.ItemPopupMenuHandler
import org.sitebay.android.ui.stats.refresh.utils.StatsSiteProvider
import org.sitebay.android.ui.stats.refresh.utils.StatsUtils
import javax.inject.Inject
import javax.inject.Named

class TodayStatsUseCase
@Inject constructor(
    @Named(UI_THREAD) private val mainDispatcher: CoroutineDispatcher,
    @Named(BG_THREAD) private val backgroundDispatcher: CoroutineDispatcher,
    private val todayStore: TodayInsightsStore,
    private val statsSiteProvider: StatsSiteProvider,
    private val statsWidgetUpdaters: StatsWidgetUpdaters,
    private val statsUtils: StatsUtils,
    private val popupMenuHandler: ItemPopupMenuHandler
) : StatelessUseCase<VisitsModel>(TODAY_STATS, mainDispatcher, backgroundDispatcher) {
    override suspend fun loadCachedData(): VisitsModel? {
        statsWidgetUpdaters.updateTodayWidget(statsSiteProvider.siteModel.siteId)
        return todayStore.getTodayInsights(statsSiteProvider.siteModel)
    }

    override suspend fun fetchRemoteData(forced: Boolean): State<VisitsModel> {
        val response = todayStore.fetchTodayInsights(statsSiteProvider.siteModel, forced)
        val model = response.model
        val error = response.error

        return when {
            error != null -> State.Error(error.message ?: error.type.name)
            model != null && model.hasData() -> State.Data(model)
            else -> State.Empty()
        }
    }

    private fun VisitsModel.hasData() =
            this.comments > 0 || this.views > 0 || this.likes > 0 || this.visitors > 0

    override fun buildLoadingItem(): List<BlockListItem> = listOf(Title(R.string.stats_insights_today_stats))

    override fun buildEmptyItem(): List<BlockListItem> {
        return listOf(buildTitle(), Empty())
    }

    override fun buildUiModel(domainModel: VisitsModel): List<BlockListItem> {
        val items = mutableListOf<BlockListItem>()
        items.add(buildTitle())

        val hasViews = domainModel.views > 0
        val hasVisitors = domainModel.visitors > 0
        val hasLikes = domainModel.likes > 0
        val hasComments = domainModel.comments > 0
        if (!hasViews && !hasVisitors && !hasLikes && !hasComments) {
            items.add(Empty())
        } else {
            items.add(
                    QuickScanItem(
                            Column(R.string.stats_views, statsUtils.toFormattedString(domainModel.views)),
                            Column(R.string.stats_visitors, statsUtils.toFormattedString(domainModel.visitors))
                    )
            )
            items.add(
                    QuickScanItem(
                            Column(R.string.stats_likes, statsUtils.toFormattedString(domainModel.likes)),
                            Column(R.string.stats_comments, statsUtils.toFormattedString(domainModel.comments))
                    )
            )
        }
        return items
    }

    private fun onMenuClick(view: View) {
        popupMenuHandler.onMenuClick(view, type)
    }

    private fun buildTitle() = Title(string.stats_insights_today_stats, menuAction = this::onMenuClick)
}
