package org.sitebay.android.ui.stats.refresh.lists.sections.insights.usecases

import android.view.View
import kotlinx.coroutines.CoroutineDispatcher
import org.sitebay.android.R
import org.sitebay.android.fluxc.model.stats.InsightsMostPopularModel
import org.sitebay.android.fluxc.store.StatsStore.InsightType.MOST_POPULAR_DAY_AND_HOUR
import org.sitebay.android.fluxc.store.stats.insights.MostPopularInsightsStore
import org.sitebay.android.modules.BG_THREAD
import org.sitebay.android.modules.UI_THREAD
import org.sitebay.android.ui.stats.refresh.lists.sections.BaseStatsUseCase.StatelessUseCase
import org.sitebay.android.ui.stats.refresh.lists.sections.BlockListItem
import org.sitebay.android.ui.stats.refresh.lists.sections.BlockListItem.Empty
import org.sitebay.android.ui.stats.refresh.lists.sections.BlockListItem.QuickScanItem
import org.sitebay.android.ui.stats.refresh.lists.sections.BlockListItem.QuickScanItem.Column
import org.sitebay.android.ui.stats.refresh.lists.sections.BlockListItem.Title
import org.sitebay.android.ui.stats.refresh.utils.DateUtils
import org.sitebay.android.ui.stats.refresh.utils.ItemPopupMenuHandler
import org.sitebay.android.ui.stats.refresh.utils.StatsSiteProvider
import org.sitebay.android.viewmodel.ResourceProvider
import javax.inject.Inject
import javax.inject.Named
import kotlin.math.roundToInt

class MostPopularInsightsUseCase
@Inject constructor(
    @Named(UI_THREAD) private val mainDispatcher: CoroutineDispatcher,
    @Named(BG_THREAD) private val backgroundDispatcher: CoroutineDispatcher,
    private val mostPopularStore: MostPopularInsightsStore,
    private val statsSiteProvider: StatsSiteProvider,
    private val dateUtils: DateUtils,
    private val resourceProvider: ResourceProvider,
    private val popupMenuHandler: ItemPopupMenuHandler
) : StatelessUseCase<InsightsMostPopularModel>(MOST_POPULAR_DAY_AND_HOUR, mainDispatcher, backgroundDispatcher) {
    override suspend fun loadCachedData(): InsightsMostPopularModel? {
        return mostPopularStore.getMostPopularInsights(statsSiteProvider.siteModel)
    }

    override suspend fun fetchRemoteData(forced: Boolean): State<InsightsMostPopularModel> {
        val response = mostPopularStore.fetchMostPopularInsights(statsSiteProvider.siteModel, forced)
        val model = response.model
        val error = response.error

        return when {
            error != null -> State.Error(error.message ?: error.type.name)
            model != null -> State.Data(model)
            else -> State.Empty()
        }
    }

    override fun buildLoadingItem(): List<BlockListItem> = listOf(Title(R.string.stats_insights_popular))

    override fun buildEmptyItem(): List<BlockListItem> {
        return listOf(buildTitle(), Empty())
    }

    @ExperimentalStdlibApi
    override fun buildUiModel(domainModel: InsightsMostPopularModel): List<BlockListItem> {
        val items = mutableListOf<BlockListItem>()
        items.add(buildTitle())
        items.add(
                QuickScanItem(
                        Column(
                                R.string.stats_insights_best_day,
                                dateUtils.getWeekDay(domainModel.highestDayOfWeek),
                                resourceProvider.getString(
                                        R.string.stats_most_popular_percent_views,
                                        domainModel.highestDayPercent.roundToInt()
                                )
                        ),
                        Column(
                                R.string.stats_insights_best_hour,
                                dateUtils.getHour(domainModel.highestHour),
                                resourceProvider.getString(
                                        R.string.stats_most_popular_percent_views,
                                        domainModel.highestHourPercent.roundToInt()
                                )
                        )
                )
        )
        return items
    }

    private fun buildTitle() = Title(R.string.stats_insights_popular, menuAction = this::onMenuClick)

    private fun onMenuClick(view: View) {
        popupMenuHandler.onMenuClick(view, type)
    }
}
