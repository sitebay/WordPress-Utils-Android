package org.sitebay.android.ui.mysite

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.reset
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.InternalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.sitebay.android.BaseUnitTest
import org.sitebay.android.TEST_DISPATCHER
import org.sitebay.android.fluxc.Dispatcher
import org.sitebay.android.fluxc.model.DynamicCardType.CUSTOMIZE_QUICK_START
import org.sitebay.android.fluxc.model.DynamicCardType.GROW_QUICK_START
import org.sitebay.android.fluxc.model.DynamicCardsModel
import org.sitebay.android.fluxc.model.SiteHomepageSettings.ShowOnFront
import org.sitebay.android.fluxc.model.SiteModel
import org.sitebay.android.fluxc.store.DynamicCardStore
import org.sitebay.android.fluxc.store.QuickStartStore
import org.sitebay.android.fluxc.store.QuickStartStore.QuickStartTask.CREATE_SITE
import org.sitebay.android.fluxc.store.QuickStartStore.QuickStartTask.EDIT_HOMEPAGE
import org.sitebay.android.fluxc.store.QuickStartStore.QuickStartTask.ENABLE_POST_SHARING
import org.sitebay.android.fluxc.store.QuickStartStore.QuickStartTask.PUBLISH_POST
import org.sitebay.android.fluxc.store.QuickStartStore.QuickStartTask.UPDATE_SITE_TITLE
import org.sitebay.android.fluxc.store.QuickStartStore.QuickStartTaskType.CUSTOMIZE
import org.sitebay.android.fluxc.store.QuickStartStore.QuickStartTaskType.GROW
import org.sitebay.android.test
import org.sitebay.android.testScope
import org.sitebay.android.ui.mysite.MySiteUiState.PartialState.QuickStartUpdate
import org.sitebay.android.ui.pages.SnackbarMessageHolder
import org.sitebay.android.ui.quickstart.QuickStartEvent
import org.sitebay.android.ui.quickstart.QuickStartMySitePrompts
import org.sitebay.android.ui.quickstart.QuickStartTaskDetails
import org.sitebay.android.ui.quickstart.QuickStartTaskDetails.CREATE_SITE_TUTORIAL
import org.sitebay.android.ui.quickstart.QuickStartTaskDetails.PUBLISH_POST_TUTORIAL
import org.sitebay.android.ui.quickstart.QuickStartTaskDetails.SHARE_SITE_TUTORIAL
import org.sitebay.android.ui.utils.UiString.UiStringText
import org.sitebay.android.util.EventBusWrapper
import org.sitebay.android.util.HtmlCompatWrapper
import org.sitebay.android.util.QuickStartUtilsWrapper
import org.sitebay.android.util.analytics.AnalyticsTrackerWrapper
import org.sitebay.android.util.config.MySiteImprovementsFeatureConfig
import org.sitebay.android.viewmodel.ResourceProvider

class QuickStartRepositoryTest : BaseUnitTest() {
    @Mock lateinit var quickStartStore: QuickStartStore
    @Mock lateinit var quickStartUtils: QuickStartUtilsWrapper
    @Mock lateinit var selectedSiteRepository: SelectedSiteRepository
    @Mock lateinit var resourceProvider: ResourceProvider
    @Mock lateinit var analyticsTrackerWrapper: AnalyticsTrackerWrapper
    @Mock lateinit var dispatcher: Dispatcher
    @Mock lateinit var eventBus: EventBusWrapper
    @Mock lateinit var dynamicCardStore: DynamicCardStore
    @Mock lateinit var htmlCompat: HtmlCompatWrapper
    @Mock lateinit var mySiteImprovementsFeatureConfig: MySiteImprovementsFeatureConfig
    private lateinit var site: SiteModel
    private lateinit var quickStartRepository: QuickStartRepository
    private lateinit var snackbars: MutableList<SnackbarMessageHolder>
    private lateinit var quickStartPrompts: MutableList<QuickStartMySitePrompts>
    private lateinit var result: MutableList<QuickStartUpdate>
    private val siteId = 1

    @InternalCoroutinesApi
    @Before
    fun setUp() = test {
        quickStartRepository = QuickStartRepository(
                TEST_DISPATCHER,
                quickStartStore,
                quickStartUtils,
                selectedSiteRepository,
                resourceProvider,
                analyticsTrackerWrapper,
                dispatcher,
                eventBus,
                dynamicCardStore,
                htmlCompat,
                mySiteImprovementsFeatureConfig
        )
        snackbars = mutableListOf()
        quickStartPrompts = mutableListOf()
        quickStartRepository.onSnackbar.observeForever { event ->
            event?.getContentIfNotHandled()
                    ?.let { snackbars.add(it) }
        }
        quickStartRepository.onQuickStartMySitePrompts.observeForever { event ->
            event?.getContentIfNotHandled()?.let { quickStartPrompts.add(it) }
        }
        site = SiteModel()
        site.id = siteId
        result = mutableListOf()
        quickStartRepository.buildSource(testScope(), siteId).observeForever { result.add(it) }
    }

    @Test
    fun `refresh loads model`() = test {
        initStore()

        quickStartRepository.refresh()

        assertModel()
    }

    @Test
    fun `refresh shows completion message and removes card if all tasks of a same type have been completed`() = test {
        initStore()

        val completionMessage = "All tasks completed!"

        whenever(selectedSiteRepository.getSelectedSite()).thenReturn(site)
        whenever(quickStartUtils.isEveryQuickStartTaskDoneForType(siteId, GROW)).thenReturn(true)
        whenever(resourceProvider.getString(any())).thenReturn(completionMessage)
        whenever(htmlCompat.fromHtml(completionMessage)).thenReturn(completionMessage)

        val task = PUBLISH_POST
        quickStartRepository.setActiveTask(task)
        quickStartRepository.completeTask(task)
        quickStartRepository.refresh()

        assertThat(snackbars).containsOnly(SnackbarMessageHolder(UiStringText(completionMessage)))

        verify(dynamicCardStore).removeCard(siteId, GROW_QUICK_START)
    }

    @Test
    fun `refresh does not show completion message if not all tasks of a same type have been completed`() = test {
        initStore()

        whenever(selectedSiteRepository.getSelectedSite()).thenReturn(site)
        whenever(quickStartUtils.isEveryQuickStartTaskDoneForType(siteId, GROW)).thenReturn(false)

        val task = PUBLISH_POST
        quickStartRepository.setActiveTask(task)
        quickStartRepository.completeTask(task)
        quickStartRepository.refresh()

        assertThat(snackbars).isEmpty()
    }

    @Test
    fun `start marks CREATE_SITE as done and loads model`() = test {
        initStore()

        quickStartRepository.startQuickStart(siteId)

        verify(quickStartUtils).startQuickStart(siteId)
        assertModel()
    }

    @Test
    fun `sets active task and shows stylized snackbar when not UPDATE_SITE_TITLE`() = test {
        initStore()
        quickStartRepository.refresh()

        quickStartRepository.setActiveTask(PUBLISH_POST)

        assertThat(result.last().activeTask).isEqualTo(PUBLISH_POST)
        assertThat(quickStartPrompts.last()).isEqualTo(QuickStartMySitePrompts.PUBLISH_POST_TUTORIAL)
    }

    @Test
    fun `completeTask marks current active task as done and refreshes model`() = test {
        whenever(selectedSiteRepository.getSelectedSite()).thenReturn(site)
        initStore()
        quickStartRepository.refresh()
        val task = PUBLISH_POST

        quickStartRepository.setActiveTask(task)

        quickStartRepository.completeTask(task)

        verify(quickStartStore).setDoneTask(siteId.toLong(), task, true)
        val update = result.last()
        assertThat(update.activeTask).isNull()
        assertThat(update.categories).isNotEmpty()
    }

    @Test
    fun `completeTask marks current pending task as done and refreshes model`() = test {
        whenever(selectedSiteRepository.getSelectedSite()).thenReturn(site)
        initStore()
        quickStartRepository.refresh()
        val task = PUBLISH_POST

        quickStartRepository.setActiveTask(task)
        quickStartRepository.requestNextStepOfTask(task)
        quickStartRepository.completeTask(task)

        verify(quickStartStore).setDoneTask(siteId.toLong(), task, true)
        val update = result.last()
        assertThat(update.activeTask).isNull()
        assertThat(update.categories).isNotEmpty()
    }

    @Test
    fun `completeTask does not marks active task as done if it is different`() = test {
        whenever(selectedSiteRepository.getSelectedSite()).thenReturn(site)
        initStore()

        quickStartRepository.setActiveTask(PUBLISH_POST)

        reset(quickStartStore)

        quickStartRepository.completeTask(UPDATE_SITE_TITLE)

        verifyZeroInteractions(quickStartStore)
    }

    @Test
    fun `requestNextStepOfTask emits quick start event`() = test {
        initQuickStartInProgress()

        quickStartRepository.setActiveTask(ENABLE_POST_SHARING)
        quickStartRepository.requestNextStepOfTask(ENABLE_POST_SHARING)

        verify(eventBus).postSticky(QuickStartEvent(ENABLE_POST_SHARING))
    }

    @Test
    fun `requestNextStepOfTask clears current active task`() = test {
        initQuickStartInProgress()

        quickStartRepository.setActiveTask(ENABLE_POST_SHARING)
        quickStartRepository.requestNextStepOfTask(ENABLE_POST_SHARING)

        val update = result.last()
        assertThat(update.activeTask).isNull()
    }

    @Test
    fun `requestNextStepOfTask does not proceed if the active task is different`() = test {
        initQuickStartInProgress()

        quickStartRepository.setActiveTask(PUBLISH_POST)
        quickStartRepository.requestNextStepOfTask(ENABLE_POST_SHARING)

        verifyZeroInteractions(eventBus)
        val update = result.last()
        assertThat(update.activeTask).isEqualTo(PUBLISH_POST)
    }

    @Test
    fun `clearActiveTask clears current active task`() = test {
        initQuickStartInProgress()

        quickStartRepository.setActiveTask(ENABLE_POST_SHARING)
        quickStartRepository.clearActiveTask()

        val update = result.last()
        assertThat(update.activeTask).isNull()
    }

    @Test
    fun `marks EDIT_HOMEPAGE task as done when site showing Posts instead of Homepage`() = test {
        initStore()

        val updatedSiteId = 2
        site.id = updatedSiteId
        site.showOnFront = ShowOnFront.POSTS.value
        whenever(selectedSiteRepository.getSelectedSite()).thenReturn(site)
        whenever(quickStartStore.hasDoneTask(updatedSiteId.toLong(), EDIT_HOMEPAGE)).thenReturn(false)

        quickStartRepository.buildSource(testScope(), updatedSiteId)

        verify(quickStartStore).setDoneTask(updatedSiteId.toLong(), EDIT_HOMEPAGE, true)
    }

    @Test
    fun `does not mark EDIT_HOMEPAGE task as done when site showing Homepage`() = test {
        val updatedSiteId = 2
        site.id = updatedSiteId
        site.showOnFront = ShowOnFront.PAGE.value
        whenever(selectedSiteRepository.getSelectedSite()).thenReturn(site)

        quickStartRepository.buildSource(testScope(), updatedSiteId)

        verify(quickStartStore, never()).setDoneTask(updatedSiteId.toLong(), EDIT_HOMEPAGE, true)
    }

    private suspend fun initQuickStartInProgress() {
        initStore()
        quickStartRepository.refresh()
    }

    private suspend fun initStore() {
        whenever(dynamicCardStore.getCards(siteId)).thenReturn(
                DynamicCardsModel(
                        dynamicCardTypes = listOf(
                                CUSTOMIZE_QUICK_START,
                                GROW_QUICK_START
                        )
                )
        )
        whenever(quickStartUtils.isQuickStartInProgress(site.id)).thenReturn(true)
        whenever(quickStartStore.getUncompletedTasksByType(siteId.toLong(), CUSTOMIZE)).thenReturn(listOf(CREATE_SITE))
        whenever(quickStartStore.getCompletedTasksByType(siteId.toLong(), CUSTOMIZE)).thenReturn(
                listOf(
                        UPDATE_SITE_TITLE
                )
        )
        whenever(
                quickStartStore.getUncompletedTasksByType(
                        siteId.toLong(),
                        GROW
                )
        ).thenReturn(listOf(ENABLE_POST_SHARING))
        whenever(quickStartStore.getCompletedTasksByType(siteId.toLong(), GROW)).thenReturn(listOf(PUBLISH_POST))
    }

    private fun assertModel() {
        val quickStartUpdate = result.last()
        quickStartUpdate.categories.let { categories ->
            assertThat(categories).hasSize(2)
            assertThat(categories[0].taskType).isEqualTo(CUSTOMIZE)
            assertThat(categories[0].uncompletedTasks).containsExactly(CREATE_SITE_TUTORIAL)
            assertThat(categories[0].completedTasks).containsExactly(QuickStartTaskDetails.UPDATE_SITE_TITLE)
            assertThat(categories[1].taskType).isEqualTo(GROW)
            assertThat(categories[1].uncompletedTasks).containsExactly(SHARE_SITE_TUTORIAL)
            assertThat(categories[1].completedTasks).containsExactly(PUBLISH_POST_TUTORIAL)
        }
    }
}
