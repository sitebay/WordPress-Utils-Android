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
import org.sitebay.android.fluxc.model.stats.time.ReferrersModel
import org.sitebay.android.fluxc.model.stats.time.ReferrersModel.Group
import org.sitebay.android.fluxc.model.stats.time.ReferrersModel.Referrer
import org.sitebay.android.fluxc.network.utils.StatsGranularity.DAYS
import org.sitebay.android.fluxc.store.StatsStore.OnStatsFetched
import org.sitebay.android.fluxc.store.StatsStore.StatsError
import org.sitebay.android.fluxc.store.StatsStore.StatsErrorType.GENERIC_ERROR
import org.sitebay.android.fluxc.store.StatsStore.TimeStatsType
import org.sitebay.android.fluxc.store.stats.time.ReferrersStore
import org.sitebay.android.test
import org.sitebay.android.ui.stats.refresh.lists.sections.BaseStatsUseCase.UseCaseMode.BLOCK
import org.sitebay.android.ui.stats.refresh.lists.sections.BaseStatsUseCase.UseCaseModel
import org.sitebay.android.ui.stats.refresh.lists.sections.BaseStatsUseCase.UseCaseModel.UseCaseState
import org.sitebay.android.ui.stats.refresh.lists.sections.BlockListItem
import org.sitebay.android.ui.stats.refresh.lists.sections.BlockListItem.Divider
import org.sitebay.android.ui.stats.refresh.lists.sections.BlockListItem.ExpandableItem
import org.sitebay.android.ui.stats.refresh.lists.sections.BlockListItem.Header
import org.sitebay.android.ui.stats.refresh.lists.sections.BlockListItem.Link
import org.sitebay.android.ui.stats.refresh.lists.sections.BlockListItem.ListItemWithIcon
import org.sitebay.android.ui.stats.refresh.lists.sections.BlockListItem.ListItemWithIcon.TextStyle
import org.sitebay.android.ui.stats.refresh.lists.sections.BlockListItem.Title
import org.sitebay.android.ui.stats.refresh.lists.sections.BlockListItem.Type.EXPANDABLE_ITEM
import org.sitebay.android.ui.stats.refresh.lists.sections.BlockListItem.Type.HEADER
import org.sitebay.android.ui.stats.refresh.lists.sections.BlockListItem.Type.LINK
import org.sitebay.android.ui.stats.refresh.lists.sections.BlockListItem.Type.LIST_ITEM_WITH_ICON
import org.sitebay.android.ui.stats.refresh.lists.sections.BlockListItem.Type.TITLE
import org.sitebay.android.ui.stats.refresh.lists.sections.granular.SelectedDateProvider
import org.sitebay.android.ui.stats.refresh.lists.sections.granular.SelectedDateProvider.SelectedDate
import org.sitebay.android.ui.stats.refresh.utils.ContentDescriptionHelper
import org.sitebay.android.ui.stats.refresh.utils.ReferrerPopupMenuHandler
import org.sitebay.android.ui.stats.refresh.utils.StatsSiteProvider
import org.sitebay.android.ui.stats.refresh.utils.StatsUtils
import org.sitebay.android.util.analytics.AnalyticsTrackerWrapper
import java.util.Date

private const val itemsToLoad = 6
private val statsGranularity = DAYS
private val selectedDate = Date(0)
private val limitMode = Top(itemsToLoad)

class ReferrersUseCaseTest : BaseUnitTest() {
    @Mock lateinit var store: ReferrersStore
    @Mock lateinit var statsSiteProvider: StatsSiteProvider
    @Mock lateinit var site: SiteModel
    @Mock lateinit var selectedDateProvider: SelectedDateProvider
    @Mock lateinit var tracker: AnalyticsTrackerWrapper
    @Mock lateinit var contentDescriptionHelper: ContentDescriptionHelper
    @Mock lateinit var statsUtils: StatsUtils
    @Mock lateinit var popupMenuHandler: ReferrerPopupMenuHandler
    private lateinit var useCase: ReferrersUseCase
    private val firstGroupViews = 50
    private val secondGroupViews = 30
    private val singleReferrer = Group(
            "group1",
            "Group 1",
            "group1.jpg",
            "group1.com",
            firstGroupViews,
            listOf(),
            false
    )
    private val referrer1 = Referrer("Referrer 1", 20, "referrer.jpg", "referrer.com", false)
    private val referrer2 = Referrer("Referrer 1", 20, "referrer.jpg", "referrer.com", true)
    private val group = Group(
            "group2",
            "Group 2",
            "group2.jpg",
            "group2.com",
            secondGroupViews,
            listOf(referrer1, referrer2),
            true
    )
    private val contentDescription = "title, views"
    @InternalCoroutinesApi
    @Before
    fun setUp() {
        useCase = ReferrersUseCase(
                statsGranularity,
                Dispatchers.Unconfined,
                TEST_DISPATCHER,
                store,
                statsSiteProvider,
                selectedDateProvider,
                tracker,
                contentDescriptionHelper,
                statsUtils,
                BLOCK,
                popupMenuHandler
        )
        whenever(statsSiteProvider.siteModel).thenReturn(site)
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
    fun `maps referrers to UI model`() = test {
        val forced = false
        val model = ReferrersModel(10, 15, listOf(singleReferrer, group), false)
        whenever(store.getReferrers(site, statsGranularity, limitMode, selectedDate)).thenReturn(model)
        whenever(store.fetchReferrers(site,
                statsGranularity, limitMode, selectedDate, forced)).thenReturn(
                OnStatsFetched(
                        model
                )
        )

        val result = loadData(true, forced)

        assertThat(result.type).isEqualTo(TimeStatsType.REFERRERS)
        val expandableItem = result.data!!.assertNonExpandedList()

        expandableItem.onExpandClicked(true)

        val updatedResult = loadData(true, forced)

        updatedResult.data!!.assertExpandedList()
    }

    private fun List<BlockListItem>.assertNonExpandedList(): ExpandableItem {
        assertThat(this).hasSize(4)
        assertTitle(this[0])
        assertLabel(this[1])
        assertSingleItem(
                this[2],
                singleReferrer.name!!,
                singleReferrer.total,
                singleReferrer.icon,
                singleReferrer.markedAsSpam
        )
        return assertExpandableItem(this[3], group.name!!, group.total!!, group.icon, group.markedAsSpam)
    }

    private fun List<BlockListItem>.assertExpandedList(): ExpandableItem {
        assertThat(this).hasSize(7)
        assertTitle(this[0])
        assertLabel(this[1])
        assertSingleItem(
                this[2],
                singleReferrer.name!!,
                singleReferrer.total,
                singleReferrer.icon,
                singleReferrer.markedAsSpam
        )
        val expandableItem = assertExpandableItem(this[3], group.name!!, group.total!!, group.icon, group.markedAsSpam)
        assertSingleItem(this[4], referrer1.name, referrer1.views, referrer1.icon, referrer1.markedAsSpam)
        assertSingleItem(this[5], referrer2.name, referrer2.views, referrer2.icon, referrer2.markedAsSpam)
        assertThat(this[6]).isEqualTo(Divider)
        return expandableItem
    }

    @Test
    fun `adds view more button when hasMore`() = test {
        val forced = false
        val model = ReferrersModel(10, 15, listOf(singleReferrer), true)
        whenever(store.getReferrers(site, statsGranularity, limitMode, selectedDate)).thenReturn(model)
        whenever(store.fetchReferrers(site,
                statsGranularity, limitMode, selectedDate, forced)).thenReturn(
                OnStatsFetched(
                        model
                )
        )
        val result = loadData(true, forced)

        assertThat(result.type).isEqualTo(TimeStatsType.REFERRERS)
        assertThat(result.state).isEqualTo(UseCaseState.SUCCESS)
        result.data!!.apply {
            assertThat(this).hasSize(4)
            assertTitle(this[0])
            assertLabel(this[1])
            assertSingleItem(
                    this[2],
                    singleReferrer.name!!,
                    singleReferrer.total,
                    singleReferrer.icon,
                    singleReferrer.markedAsSpam
            )
            assertLink(this[3])
        }
    }

    @Test
    fun `maps empty referrers to UI model`() = test {
        val forced = false
        whenever(store.fetchReferrers(site,
                statsGranularity, limitMode, selectedDate, forced)).thenReturn(
                OnStatsFetched(ReferrersModel(0, 0, listOf(), false))
        )

        val result = loadData(true, forced)

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
        whenever(store.fetchReferrers(site,
                statsGranularity, limitMode, selectedDate, forced)).thenReturn(
                OnStatsFetched(
                        StatsError(GENERIC_ERROR, message)
                )
        )

        val result = loadData(true, forced)

        assertThat(result.state).isEqualTo(UseCaseState.ERROR)
    }

    private fun assertTitle(item: BlockListItem) {
        assertThat(item.type).isEqualTo(TITLE)
        assertThat((item as Title).textResource).isEqualTo(R.string.stats_referrers)
    }

    private fun assertLabel(item: BlockListItem) {
        assertThat(item.type).isEqualTo(HEADER)
        assertThat((item as Header).startLabel).isEqualTo(R.string.stats_referrer_label)
        assertThat(item.endLabel).isEqualTo(R.string.stats_referrer_views_label)
    }

    private fun assertSingleItem(
        item: BlockListItem,
        key: String,
        views: Int?,
        icon: String?,
        spam: Boolean?
    ) {
        assertThat(item.type).isEqualTo(LIST_ITEM_WITH_ICON)
        assertThat((item as ListItemWithIcon).text).isEqualTo(key)
        if (views != null) {
            assertThat(item.value).isEqualTo(views.toString())
        } else {
            assertThat(item.value).isNull()
        }
        if (spam != null && spam) {
            assertThat(item.icon).isEqualTo(R.drawable.ic_spam_white_24dp)
            assertThat(item.textStyle).isEqualTo(TextStyle.LIGHT)
            assertThat(item.iconUrl).isNull()
        } else {
            assertThat(item.icon).isNull()
            assertThat(item.textStyle).isEqualTo(TextStyle.NORMAL)
            assertThat(item.iconUrl).isEqualTo(icon)
        }
        assertThat(item.contentDescription).isEqualTo(contentDescription)
    }

    private fun assertExpandableItem(
        item: BlockListItem,
        label: String,
        views: Int,
        icon: String?,
        spam: Boolean?
    ): ExpandableItem {
        assertThat(item.type).isEqualTo(EXPANDABLE_ITEM)
        assertThat((item as ExpandableItem).header.text).isEqualTo(label)

        if (spam != null && spam) {
            assertThat(item.header.icon).isEqualTo(R.drawable.ic_spam_white_24dp)
            assertThat(item.header.textStyle).isEqualTo(TextStyle.LIGHT)
            assertThat(item.header.iconUrl).isNull()
        } else {
            assertThat(item.header.icon).isNull()
            assertThat(item.header.textStyle).isEqualTo(TextStyle.NORMAL)
            assertThat(item.header.iconUrl).isEqualTo(icon)
        }
        assertThat(item.header.contentDescription).isEqualTo(contentDescription)
        return item
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
