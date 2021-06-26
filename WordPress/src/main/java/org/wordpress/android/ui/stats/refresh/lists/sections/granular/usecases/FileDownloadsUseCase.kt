package org.sitebay.android.ui.stats.refresh.lists.sections.granular.usecases

import kotlinx.coroutines.CoroutineDispatcher
import org.sitebay.android.R
import org.sitebay.android.analytics.AnalyticsTracker
import org.sitebay.android.fluxc.model.SiteModel
import org.sitebay.android.fluxc.model.stats.LimitMode
import org.sitebay.android.fluxc.model.stats.time.FileDownloadsModel
import org.sitebay.android.fluxc.network.utils.StatsGranularity
import org.sitebay.android.fluxc.store.StatsStore.TimeStatsType.FILE_DOWNLOADS
import org.sitebay.android.fluxc.store.stats.time.FileDownloadsStore
import org.sitebay.android.modules.BG_THREAD
import org.sitebay.android.modules.UI_THREAD
import org.sitebay.android.ui.stats.refresh.NavigationTarget.ViewFileDownloads
import org.sitebay.android.ui.stats.refresh.lists.BLOCK_ITEM_COUNT
import org.sitebay.android.ui.stats.refresh.lists.VIEW_ALL_ITEM_COUNT
import org.sitebay.android.ui.stats.refresh.lists.sections.BaseStatsUseCase.UseCaseMode.BLOCK
import org.sitebay.android.ui.stats.refresh.lists.sections.BaseStatsUseCase.UseCaseMode.VIEW_ALL
import org.sitebay.android.ui.stats.refresh.lists.sections.BlockListItem
import org.sitebay.android.ui.stats.refresh.lists.sections.BlockListItem.Empty
import org.sitebay.android.ui.stats.refresh.lists.sections.BlockListItem.Header
import org.sitebay.android.ui.stats.refresh.lists.sections.BlockListItem.Link
import org.sitebay.android.ui.stats.refresh.lists.sections.BlockListItem.ListItemWithIcon
import org.sitebay.android.ui.utils.ListItemInteraction
import org.sitebay.android.ui.stats.refresh.lists.sections.BlockListItem.Title
import org.sitebay.android.ui.stats.refresh.lists.sections.granular.GranularStatelessUseCase
import org.sitebay.android.ui.stats.refresh.lists.sections.granular.GranularUseCaseFactory
import org.sitebay.android.ui.stats.refresh.lists.sections.granular.SelectedDateProvider
import org.sitebay.android.ui.stats.refresh.utils.ContentDescriptionHelper
import org.sitebay.android.ui.stats.refresh.utils.StatsSiteProvider
import org.sitebay.android.ui.stats.refresh.utils.StatsUtils
import org.sitebay.android.ui.stats.refresh.utils.trackGranular
import org.sitebay.android.util.LocaleManagerWrapper
import org.sitebay.android.util.analytics.AnalyticsTrackerWrapper
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Named

private const val THRESHOLD_YEAR = 2019
private const val THRESHOLD_MONTH = Calendar.JUNE
private const val THRESHOLD_DAY = 29

class FileDownloadsUseCase
constructor(
    statsGranularity: StatsGranularity,
    @Named(UI_THREAD) private val mainDispatcher: CoroutineDispatcher,
    @Named(BG_THREAD) private val backgroundDispatcher: CoroutineDispatcher,
    private val store: FileDownloadsStore,
    statsSiteProvider: StatsSiteProvider,
    selectedDateProvider: SelectedDateProvider,
    private val analyticsTracker: AnalyticsTrackerWrapper,
    private val contentDescriptionHelper: ContentDescriptionHelper,
    private val localeManagerWrapper: LocaleManagerWrapper,
    private val statsUtils: StatsUtils,
    private val useCaseMode: UseCaseMode
) : GranularStatelessUseCase<FileDownloadsModel>(
        FILE_DOWNLOADS,
        mainDispatcher,
        backgroundDispatcher,
        selectedDateProvider,
        statsSiteProvider,
        statsGranularity
) {
    private val itemsToLoad = if (useCaseMode == VIEW_ALL) VIEW_ALL_ITEM_COUNT else BLOCK_ITEM_COUNT

    override fun buildLoadingItem(): List<BlockListItem> = listOf(Title(R.string.stats_file_downloads))

    override suspend fun loadCachedData(selectedDate: Date, site: SiteModel): FileDownloadsModel? {
        return store.getFileDownloads(
                site,
                statsGranularity,
                LimitMode.Top(itemsToLoad),
                selectedDate
        )
    }

    override fun buildEmptyItem(): List<BlockListItem> {
        val selectedDate = selectedDateProvider.getSelectedDate(statsGranularity)
        val startCalendar = localeManagerWrapper.getCurrentCalendar()
        startCalendar.set(THRESHOLD_YEAR, THRESHOLD_MONTH, THRESHOLD_DAY, 0, 0, 0)
        return if (selectedDate != null && selectedDate.before(startCalendar.time)) {
            buildLoadingItem() + listOf(Empty(textResource = R.string.stats_data_not_recorded_for_period))
        } else {
            super.buildEmptyItem()
        }
    }

    override suspend fun fetchRemoteData(
        selectedDate: Date,
        site: SiteModel,
        forced: Boolean
    ): State<FileDownloadsModel> {
        val response = store.fetchFileDownloads(
                site,
                statsGranularity,
                LimitMode.Top(itemsToLoad),
                selectedDate,
                forced
        )
        val model = response.model
        val error = response.error

        return when {
            error != null -> State.Error(error.message ?: error.type.name)
            model != null && model.fileDownloads.isNotEmpty() -> State.Data(model)
            else -> State.Empty()
        }
    }

    override fun buildUiModel(domainModel: FileDownloadsModel): List<BlockListItem> {
        val items = mutableListOf<BlockListItem>()

        if (useCaseMode == BLOCK) {
            items.add(Title(R.string.stats_file_downloads))
        }

        if (domainModel.fileDownloads.isEmpty()) {
            items.add(Empty(R.string.stats_no_data_for_period))
        } else {
            val header = Header(R.string.stats_file_downloads_title_label, R.string.stats_file_downloads_value_label)
            items.add(header)
            items.addAll(domainModel.fileDownloads.mapIndexed { index, fileDownloads ->
                ListItemWithIcon(
                        text = fileDownloads.filename,
                        value = statsUtils.toFormattedString(fileDownloads.downloads),
                        showDivider = index < domainModel.fileDownloads.size - 1,
                        contentDescription = contentDescriptionHelper.buildContentDescription(
                                header,
                                fileDownloads.filename,
                                fileDownloads.downloads
                        )
                )
            })

            if (useCaseMode == BLOCK && domainModel.hasMore) {
                items.add(
                        Link(
                                text = R.string.stats_insights_view_more,
                                navigateAction = ListItemInteraction.create(statsGranularity, this::onViewMoreClick)
                        )
                )
            }
        }
        return items
    }

    private fun onViewMoreClick(statsGranularity: StatsGranularity) {
        analyticsTracker.trackGranular(AnalyticsTracker.Stat.STATS_FILE_DOWNLOADS_VIEW_MORE_TAPPED, statsGranularity)
        navigateTo(
                ViewFileDownloads(
                        statsGranularity,
                        selectedDateProvider.getSelectedDate(statsGranularity) ?: Date()
                )
        )
    }

    class FileDownloadsUseCaseFactory
    @Inject constructor(
        @Named(UI_THREAD) private val mainDispatcher: CoroutineDispatcher,
        @Named(BG_THREAD) private val backgroundDispatcher: CoroutineDispatcher,
        private val store: FileDownloadsStore,
        private val selectedDateProvider: SelectedDateProvider,
        private val statsSiteProvider: StatsSiteProvider,
        private val analyticsTracker: AnalyticsTrackerWrapper,
        private val contentDescriptionHelper: ContentDescriptionHelper,
        private val statsUtils: StatsUtils,
        private val localeManagerWrapper: LocaleManagerWrapper
    ) : GranularUseCaseFactory {
        override fun build(granularity: StatsGranularity, useCaseMode: UseCaseMode) =
                FileDownloadsUseCase(
                        granularity,
                        mainDispatcher,
                        backgroundDispatcher,
                        store,
                        statsSiteProvider,
                        selectedDateProvider,
                        analyticsTracker,
                        contentDescriptionHelper,
                        localeManagerWrapper,
                        statsUtils,
                        useCaseMode
                )
    }
}
