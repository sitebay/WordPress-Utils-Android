package org.sitebay.android.ui.stats.refresh.lists.sections.insights.usecases

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
import org.sitebay.android.fluxc.model.stats.FollowersModel
import org.sitebay.android.fluxc.model.stats.LimitMode
import org.sitebay.android.fluxc.model.stats.PagedMode
import org.sitebay.android.fluxc.model.stats.PublicizeModel
import org.sitebay.android.fluxc.model.stats.PublicizeModel.Service
import org.sitebay.android.fluxc.store.StatsStore.OnStatsFetched
import org.sitebay.android.fluxc.store.stats.insights.FollowersStore
import org.sitebay.android.fluxc.store.stats.insights.PublicizeStore
import org.sitebay.android.test
import org.sitebay.android.ui.stats.refresh.lists.sections.BaseStatsUseCase.UseCaseModel
import org.sitebay.android.ui.stats.refresh.lists.sections.BaseStatsUseCase.UseCaseModel.UseCaseState.SUCCESS
import org.sitebay.android.ui.stats.refresh.lists.sections.BlockListItem
import org.sitebay.android.ui.stats.refresh.lists.sections.BlockListItem.ListItemWithIcon
import org.sitebay.android.ui.stats.refresh.lists.sections.BlockListItem.Title
import org.sitebay.android.ui.stats.refresh.lists.sections.BlockListItem.Type.LIST_ITEM_WITH_ICON
import org.sitebay.android.ui.stats.refresh.lists.sections.BlockListItem.Type.TITLE
import org.sitebay.android.ui.stats.refresh.utils.ContentDescriptionHelper
import org.sitebay.android.ui.stats.refresh.utils.ItemPopupMenuHandler
import org.sitebay.android.ui.stats.refresh.utils.StatsSiteProvider
import org.sitebay.android.ui.stats.refresh.utils.StatsUtils

class FollowerTotalsUseCaseTest : BaseUnitTest() {
    @Mock lateinit var followersStore: FollowersStore
    @Mock lateinit var publicizeStore: PublicizeStore
    @Mock lateinit var statsSiteProvider: StatsSiteProvider
    @Mock lateinit var contentDescriptionHelper: ContentDescriptionHelper
    @Mock lateinit var popupMenuHandler: ItemPopupMenuHandler
    @Mock lateinit var statsUtils: StatsUtils
    @Mock lateinit var site: SiteModel
    private lateinit var useCase: FollowerTotalsUseCase

    private val emailModel = FollowersModel(7, emptyList(), false)
    private val wpModel = FollowersModel(3, emptyList(), false)
    private val socialModel = PublicizeModel(listOf(
            Service("Twitter", 10),
            Service("FB", 5)),
            false)
    private val contentDescription = "title, views"

    @InternalCoroutinesApi
    @Before
    fun setUp() {
        useCase = FollowerTotalsUseCase(
                Dispatchers.Unconfined,
                TEST_DISPATCHER,
                followersStore,
                publicizeStore,
                statsSiteProvider,
                contentDescriptionHelper,
                statsUtils,
                popupMenuHandler
        )
        whenever(statsSiteProvider.siteModel).thenReturn(site)

        whenever(followersStore.getEmailFollowers(site, LimitMode.Top(0))).thenReturn(emailModel)
        whenever(followersStore.getWpComFollowers(site, LimitMode.Top(0))).thenReturn(wpModel)
        whenever(publicizeStore.getPublicizeData(site, LimitMode.All)).thenReturn(socialModel)
        whenever(contentDescriptionHelper.buildContentDescription(
                any(),
                any<Int>()
        )).thenReturn(contentDescription)
        whenever(statsUtils.toFormattedString(any<Int>(), any())).then { (it.arguments[0] as Int).toString() }
    }

    @Test
    fun `maps follower totals to UI model`() = test {
        val forced = false
        val refresh = true

        whenever(followersStore.fetchEmailFollowers(site, PagedMode(0))).thenReturn(OnStatsFetched(emailModel))
        whenever(followersStore.fetchWpComFollowers(site, PagedMode(0))).thenReturn(OnStatsFetched(wpModel))
        whenever(publicizeStore.fetchPublicizeData(site, LimitMode.All)).thenReturn(OnStatsFetched(socialModel))

        val result = loadFollowerTotalsData(refresh, forced)

        assertThat(result.state).isEqualTo(SUCCESS)
        result.data!!.apply {
            assertThat(this).hasSize(4)
            assertTitle(this[0])

            for (i in 1..3) {
                assertThat(this[i].type).isEqualTo(LIST_ITEM_WITH_ICON)
                assertItem(this[i] as ListItemWithIcon)
            }
        }
    }

    private fun assertItem(item: ListItemWithIcon) {
        when (item.icon) {
            R.drawable.ic_sitebay_white_24dp -> {
                assertThat(item.value).isEqualTo(3.toString())
            }
            R.drawable.ic_mail_white_24dp -> {
                assertThat(item.value).isEqualTo(7.toString())
            }
            R.drawable.ic_share_white_24dp -> {
                assertThat(item.value).isEqualTo(15.toString())
            }
        }
        assertThat(item.contentDescription).isEqualTo(contentDescription)
    }

    private fun assertTitle(item: BlockListItem) {
        assertThat(item.type).isEqualTo(TITLE)
        assertThat((item as Title).textResource).isEqualTo(R.string.stats_view_follower_totals)
        assertThat(item.menuAction).isNotNull
    }

    private suspend fun loadFollowerTotalsData(refresh: Boolean, forced: Boolean): UseCaseModel {
        var result: UseCaseModel? = null
        useCase.liveData.observeForever { result = it }
        useCase.fetch(refresh, forced)
        return checkNotNull(result)
    }
}
