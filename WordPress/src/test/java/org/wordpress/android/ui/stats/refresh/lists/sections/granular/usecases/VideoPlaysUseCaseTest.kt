package org.sitebay.android.ui.stats.refresh.lists.sections.granular.usecases

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.InternalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.sitebay.android.BaseUnitTest
import org.sitebay.android.R
import org.sitebay.android.TEST_DISPATCHER
import org.sitebay.android.fluxc.model.SiteModel
import org.sitebay.android.fluxc.model.stats.LimitMode.Top
import org.sitebay.android.fluxc.model.stats.time.VideoPlaysModel
import org.sitebay.android.fluxc.model.stats.time.VideoPlaysModel.VideoPlays
import org.sitebay.android.fluxc.network.utils.StatsGranularity.DAYS
import org.sitebay.android.fluxc.store.StatsStore.OnStatsFetched
import org.sitebay.android.fluxc.store.StatsStore.StatsError
import org.sitebay.android.fluxc.store.StatsStore.StatsErrorType.GENERIC_ERROR
import org.sitebay.android.fluxc.store.StatsStore.TimeStatsType
import org.sitebay.android.fluxc.store.stats.time.VideoPlaysStore
import org.sitebay.android.test
import org.sitebay.android.ui.stats.refresh.lists.sections.BaseStatsUseCase.UseCaseMode.BLOCK
import org.sitebay.android.ui.stats.refresh.lists.sections.BaseStatsUseCase.UseCaseModel
import org.sitebay.android.ui.stats.refresh.lists.sections.BaseStatsUseCase.UseCaseModel.UseCaseState
import org.sitebay.android.ui.stats.refresh.lists.sections.BlockListItem
import org.sitebay.android.ui.stats.refresh.lists.sections.BlockListItem.Header
import org.sitebay.android.ui.stats.refresh.lists.sections.BlockListItem.Link
import org.sitebay.android.ui.stats.refresh.lists.sections.BlockListItem.ListItemWithIcon
import org.sitebay.android.ui.stats.refresh.lists.sections.BlockListItem.Title
import org.sitebay.android.ui.stats.refresh.lists.sections.BlockListItem.Type.HEADER
import org.sitebay.android.ui.stats.refresh.lists.sections.BlockListItem.Type.LINK
import org.sitebay.android.ui.stats.refresh.lists.sections.BlockListItem.Type.LIST_ITEM_WITH_ICON
import org.sitebay.android.ui.stats.refresh.lists.sections.BlockListItem.Type.TITLE
import org.sitebay.android.ui.stats.refresh.lists.sections.granular.SelectedDateProvider
import org.sitebay.android.ui.stats.refresh.lists.sections.granular.SelectedDateProvider.SelectedDate
import org.sitebay.android.ui.stats.refresh.utils.ContentDescriptionHelper
import org.sitebay.android.ui.stats.refresh.utils.StatsSiteProvider
import org.sitebay.android.ui.stats.refresh.utils.StatsUtils
import org.sitebay.android.util.analytics.AnalyticsTrackerWrapper
import java.util.Date

private const val ITEMS_TO_LOAD = 6
private val statsGranularity = DAYS
private val selectedDate = Date(0)
private val limitMode = Top(ITEMS_TO_LOAD)

class VideoPlaysUseCaseTest : BaseUnitTest() {
    @Mock lateinit var store: VideoPlaysStore
    @Mock lateinit var siteModelProvider: StatsSiteProvider
    @Mock lateinit var site: SiteModel
    @Mock lateinit var selectedDateProvider: SelectedDateProvider
    @Mock lateinit var tracker: AnalyticsTrackerWrapper
    @Mock lateinit var contentDescriptionHelper: ContentDescriptionHelper
    @Mock lateinit var statsUtils: StatsUtils
    private lateinit var useCase: VideoPlaysUseCase
    private val videoPlay = VideoPlays("post1", "Video 1", "group2.jpg", 100)
    private val contentDescription = "title, views"
    @InternalCoroutinesApi
    @Before
    fun setUp() {
        useCase = VideoPlaysUseCase(
                statsGranularity,
                Dispatchers.Unconfined,
                TEST_DISPATCHER,
                store,
                siteModelProvider,
                selectedDateProvider,
                tracker,
                contentDescriptionHelper,
                statsUtils,
                BLOCK
        )
        whenever(siteModelProvider.siteModel).thenReturn(site)
        whenever((selectedDateProvider.getSelectedDate(statsGranularity))).thenReturn(selectedDate)
        whenever((selectedDateProvider.getSelectedDateState(statsGranularity))).thenReturn(
                SelectedDate(
                        selectedDate,
                        listOf(selectedDate)
                )
        )
        whenever(contentDescriptionHelper.buildContentDescription(
                any(),
                any<String>(),
                any()
        )).thenReturn(contentDescription)
        whenever(statsUtils.toFormattedString(any<Int>(), any())).then { (it.arguments[0] as Int).toString() }
    }

    @Test
    fun `maps video plays to UI model`() = test {
        val forced = false
        val model = VideoPlaysModel(10, 15, listOf(videoPlay), false)
        whenever(
                store.getVideoPlays(
                        site,
                        statsGranularity,
                        limitMode,
                        selectedDate
                )
        ).thenReturn(model)
        whenever(store.fetchVideoPlays(site, statsGranularity, limitMode, selectedDate,
                forced)).thenReturn(
                OnStatsFetched(
                        model
                )
        )

        val result = loadData(true, forced)

        assertThat(result.type).isEqualTo(TimeStatsType.VIDEOS)
        assertThat(result.state).isEqualTo(UseCaseState.SUCCESS)
        result.data!!.apply {
            assertTitle(this[0])
            assertHeader(this[1])
            assertItem(this[2], videoPlay.title, videoPlay.plays)
        }
    }

    @Test
    fun `adds view more button when hasMore`() = test {
        val forced = false
        val model = VideoPlaysModel(10, 15, listOf(videoPlay), true)
        whenever(
                store.getVideoPlays(
                        site,
                        statsGranularity,
                        limitMode,
                        selectedDate
                )
        ).thenReturn(model)
        whenever(
                store.fetchVideoPlays(site, statsGranularity, limitMode, selectedDate, forced)
        ).thenReturn(
                OnStatsFetched(
                        model
                )
        )
        val result = loadData(true, forced)

        assertThat(result.type).isEqualTo(TimeStatsType.VIDEOS)
        assertThat(result.state).isEqualTo(UseCaseState.SUCCESS)
        result.data!!.apply {
            assertThat(this).hasSize(4)
            assertTitle(this[0])
            assertHeader(this[1])
            assertItem(this[2], videoPlay.title, videoPlay.plays)
            assertLink(this[3])
        }
    }

    @Test
    fun `maps empty video plays to UI model`() = test {
        val forced = false
        whenever(
                store.fetchVideoPlays(site, statsGranularity, limitMode, selectedDate, forced)
        ).thenReturn(
                OnStatsFetched(VideoPlaysModel(0, 0, listOf(), false))
        )

        val result = loadData(true, forced)

        assertThat(result.type).isEqualTo(TimeStatsType.VIDEOS)
        assertThat(result.state).isEqualTo(UseCaseState.EMPTY)
        result.stateData!!.apply {
            assertThat(this).hasSize(2)
            assertTitle(this[0])
            assertThat(this[1]).isEqualTo(BlockListItem.Empty(R.string.stats_no_data_for_period))
        }
    }

    @Test
    fun `maps error item to UI model`() = test {
        val forced = false
        val message = "Generic error"
        whenever(
                store.fetchVideoPlays(site, statsGranularity, limitMode, selectedDate, forced)
        ).thenReturn(
                OnStatsFetched(
                        StatsError(GENERIC_ERROR, message)
                )
        )

        val result = loadData(true, forced)

        assertThat(result.type).isEqualTo(TimeStatsType.VIDEOS)
    }

    private fun assertTitle(item: BlockListItem) {
        assertThat(item.type).isEqualTo(TITLE)
        assertThat((item as Title).textResource).isEqualTo(R.string.stats_videos)
    }

    private fun assertHeader(item: BlockListItem) {
        assertThat(item.type).isEqualTo(HEADER)
        assertThat((item as Header).startLabel).isEqualTo(R.string.stats_videos_title_label)
        assertThat(item.endLabel).isEqualTo(R.string.stats_videos_views_label)
    }

    private fun assertItem(
        item: BlockListItem,
        key: String,
        views: Int?
    ) {
        assertThat(item.type).isEqualTo(LIST_ITEM_WITH_ICON)
        assertThat((item as ListItemWithIcon).text).isEqualTo(key)
        if (views != null) {
            assertThat(item.value).isEqualTo(views.toString())
        } else {
            assertThat(item.value).isNull()
        }
        assertThat(item.contentDescription).isEqualTo(contentDescription)
    }

    private fun assertLink(item: BlockListItem) {
        assertThat(item.type).isEqualTo(LINK)
        assertThat((item as Link).text).isEqualTo(R.string.stats_insights_view_more)
    }

    private suspend fun loadData(refresh: Boolean, forced: Boolean): UseCaseModel {
        var result: UseCaseModel? = null
        useCase.liveData.observeForever { result = it }
        useCase.fetch(refresh, forced)
        return checkNotNull(result)
    }
}
