package org.sitebay.android.ui.stats.refresh.lists.sections.granular

import kotlinx.coroutines.CoroutineDispatcher
import org.sitebay.android.R
import org.sitebay.android.fluxc.model.SiteModel
import org.sitebay.android.fluxc.network.utils.StatsGranularity
import org.sitebay.android.fluxc.store.StatsStore.StatsType
import org.sitebay.android.ui.stats.refresh.lists.sections.BaseStatsUseCase
import org.sitebay.android.ui.stats.refresh.lists.sections.BlockListItem
import org.sitebay.android.ui.stats.refresh.utils.StatsSiteProvider
import org.sitebay.android.ui.stats.refresh.utils.toStatsSection
import java.util.Date

abstract class GranularStatefulUseCase<DOMAIN_MODEL, UI_STATE>(
    type: StatsType,
    mainDispatcher: CoroutineDispatcher,
    backgroundDispatcher: CoroutineDispatcher,
    val statsSiteProvider: StatsSiteProvider,
    val selectedDateProvider: SelectedDateProvider,
    val statsGranularity: StatsGranularity,
    defaultUiState: UI_STATE
) : BaseStatsUseCase<DOMAIN_MODEL, UI_STATE>(
        type,
        mainDispatcher,
        backgroundDispatcher,
        defaultUiState,
        listOf(UseCaseParam.SelectedDateParam(statsGranularity.toStatsSection()))
) {
    abstract suspend fun loadCachedData(selectedDate: Date, site: SiteModel): DOMAIN_MODEL?

    final override suspend fun loadCachedData(): DOMAIN_MODEL? {
        val selectedDate = selectedDateProvider.getSelectedDate(statsGranularity)
        return selectedDate?.let { loadCachedData(it, statsSiteProvider.siteModel) }
    }

    abstract suspend fun fetchRemoteData(selectedDate: Date, site: SiteModel, forced: Boolean): State<DOMAIN_MODEL>

    final override suspend fun fetchRemoteData(forced: Boolean): State<DOMAIN_MODEL> {
        return selectedDateProvider.getSelectedDateState(statsGranularity).let { date ->
            when {
                date.error -> State.Error("Missing date")
                date.hasData() -> fetchRemoteData(date.getDate(), statsSiteProvider.siteModel, forced)
                date.loading -> State.Loading()
                else -> State.Loading()
            }
        }
    }

    override fun buildEmptyItem(): List<BlockListItem> {
        return buildLoadingItem() + listOf(BlockListItem.Empty(textResource = R.string.stats_no_data_for_period))
    }
}
