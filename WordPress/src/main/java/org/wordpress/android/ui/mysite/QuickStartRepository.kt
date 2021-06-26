package org.sitebay.android.ui.mysite

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import org.sitebay.android.R
import org.sitebay.android.analytics.AnalyticsTracker.Stat
import org.sitebay.android.fluxc.Dispatcher
import org.sitebay.android.fluxc.generated.SiteActionBuilder
import org.sitebay.android.fluxc.model.DynamicCardType
import org.sitebay.android.fluxc.model.DynamicCardType.CUSTOMIZE_QUICK_START
import org.sitebay.android.fluxc.model.DynamicCardType.GROW_QUICK_START
import org.sitebay.android.fluxc.model.SiteHomepageSettings.ShowOnFront
import org.sitebay.android.fluxc.store.DynamicCardStore
import org.sitebay.android.fluxc.store.QuickStartStore
import org.sitebay.android.fluxc.store.QuickStartStore.QuickStartTask
import org.sitebay.android.fluxc.store.QuickStartStore.QuickStartTask.EDIT_HOMEPAGE
import org.sitebay.android.fluxc.store.QuickStartStore.QuickStartTask.UPDATE_SITE_TITLE
import org.sitebay.android.fluxc.store.QuickStartStore.QuickStartTaskType
import org.sitebay.android.fluxc.store.QuickStartStore.QuickStartTaskType.CUSTOMIZE
import org.sitebay.android.fluxc.store.QuickStartStore.QuickStartTaskType.GROW
import org.sitebay.android.fluxc.store.QuickStartStore.QuickStartTaskType.UNKNOWN
import org.sitebay.android.fluxc.store.SiteStore.CompleteQuickStartPayload
import org.sitebay.android.fluxc.store.SiteStore.CompleteQuickStartVariant.NEXT_STEPS
import org.sitebay.android.modules.BG_THREAD
import org.sitebay.android.ui.mysite.MySiteUiState.PartialState.QuickStartUpdate
import org.sitebay.android.ui.pages.SnackbarMessageHolder
import org.sitebay.android.ui.quickstart.QuickStartEvent
import org.sitebay.android.ui.quickstart.QuickStartMySitePrompts
import org.sitebay.android.ui.quickstart.QuickStartTaskDetails
import org.sitebay.android.ui.utils.UiString.UiStringText
import org.sitebay.android.util.EventBusWrapper
import org.sitebay.android.util.HtmlCompatWrapper
import org.sitebay.android.util.QuickStartUtilsWrapper
import org.sitebay.android.util.SiteUtils
import org.sitebay.android.util.analytics.AnalyticsTrackerWrapper
import org.sitebay.android.util.config.MySiteImprovementsFeatureConfig
import org.sitebay.android.util.mapAsync
import org.sitebay.android.util.merge
import org.sitebay.android.viewmodel.Event
import org.sitebay.android.viewmodel.ResourceProvider
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Singleton
class QuickStartRepository
@Inject constructor(
    @Named(BG_THREAD) private val bgDispatcher: CoroutineDispatcher,
    private val quickStartStore: QuickStartStore,
    private val quickStartUtils: QuickStartUtilsWrapper,
    private val selectedSiteRepository: SelectedSiteRepository,
    private val resourceProvider: ResourceProvider,
    private val analyticsTrackerWrapper: AnalyticsTrackerWrapper,
    private val dispatcher: Dispatcher,
    private val eventBus: EventBusWrapper,
    private val dynamicCardStore: DynamicCardStore,
    private val htmlCompat: HtmlCompatWrapper,
    private val mySiteImprovementsFeatureConfig: MySiteImprovementsFeatureConfig
) : CoroutineScope, MySiteSource<QuickStartUpdate> {
    private val job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = bgDispatcher + job

    private val detailsMap: Map<QuickStartTask, QuickStartTaskDetails> = QuickStartTaskDetails.values()
            .associateBy { it.task }
    private val refresh = MutableLiveData<Boolean>()
    private val _activeTask = MutableLiveData<QuickStartTask?>()
    private val _onSnackbar = MutableLiveData<Event<SnackbarMessageHolder>>()
    private val _onQuickStartMySitePrompts = MutableLiveData<Event<QuickStartMySitePrompts>>()
    val onSnackbar = _onSnackbar as LiveData<Event<SnackbarMessageHolder>>
    val onQuickStartMySitePrompts = _onQuickStartMySitePrompts as LiveData<Event<QuickStartMySitePrompts>>
    val activeTask = _activeTask as LiveData<QuickStartTask?>

    private var pendingTask: QuickStartTask? = null

    private fun buildQuickStartCategory(siteId: Int, quickStartTaskType: QuickStartTaskType) = QuickStartCategory(
            quickStartTaskType,
            uncompletedTasks = quickStartStore.getUncompletedTasksByType(siteId.toLong(), quickStartTaskType)
                    .mapNotNull { detailsMap[it] },
            completedTasks = quickStartStore.getCompletedTasksByType(siteId.toLong(), quickStartTaskType)
                    .mapNotNull { detailsMap[it] })

    override fun buildSource(coroutineScope: CoroutineScope, siteId: Int): LiveData<QuickStartUpdate> {
        _activeTask.value = null
        pendingTask = null
        if (selectedSiteRepository.getSelectedSite()?.showOnFront == ShowOnFront.POSTS.value &&
                !quickStartStore.hasDoneTask(siteId.toLong(), EDIT_HOMEPAGE)) {
            setTaskDoneAndTrack(EDIT_HOMEPAGE, siteId)
            refresh()
        }
        val quickStartTaskTypes = refresh.mapAsync(coroutineScope) {
            dynamicCardStore.getCards(siteId).dynamicCardTypes.map { it.toQuickStartTaskType() }.onEach { taskType ->
                if (quickStartUtils.isEveryQuickStartTaskDoneForType(siteId, taskType)) {
                    onCategoryCompleted(siteId, taskType)
                }
            }
        }
        return merge(quickStartTaskTypes, activeTask) { types, activeTask ->
            val categories = if (quickStartUtils.isQuickStartInProgress(siteId)) {
                types?.map { buildQuickStartCategory(siteId, it) } ?: listOf()
            } else {
                listOf()
            }
            QuickStartUpdate(activeTask, categories)
        }
    }

    fun startQuickStart(newSiteLocalID: Int) {
        if (newSiteLocalID != -1) {
            quickStartUtils.startQuickStart(newSiteLocalID)
            refresh()
        }
    }

    fun refresh() {
        refresh.postValue(true)
    }

    fun setActiveTask(task: QuickStartTask) {
        _activeTask.postValue(task)
        pendingTask = null
        if (task == UPDATE_SITE_TITLE) {
            val shortQuickStartMessage = resourceProvider.getString(
                    R.string.quick_start_dialog_update_site_title_message_short,
                    SiteUtils.getSiteNameOrHomeURL(selectedSiteRepository.getSelectedSite())
            )
            _onSnackbar.postValue(Event(SnackbarMessageHolder(UiStringText(shortQuickStartMessage.asHtml()))))
        } else {
            QuickStartMySitePrompts.getPromptDetailsForTask(task)?.let { activeTutorialPrompt ->
                _onQuickStartMySitePrompts.postValue(Event(activeTutorialPrompt))
            }
        }
    }

    fun clearActiveTask() {
        _activeTask.value = null
    }

    @JvmOverloads fun completeTask(task: QuickStartTask, refreshImmediately: Boolean = false) {
        selectedSiteRepository.getSelectedSite()?.let { site ->
            if (task != activeTask.value && task != pendingTask) return
            _activeTask.value = null
            pendingTask = null
            if (quickStartStore.hasDoneTask(site.id.toLong(), task)) return
            // If we want notice and reminders, we should call QuickStartUtils.completeTaskAndRemindNextOne here
            setTaskDoneAndTrack(task, site.id)
            // We need to refresh immediately. This is useful for tasks that are completed on the My Site screen.
            if (refreshImmediately) {
                refresh()
            }
            if (quickStartUtils.isEveryQuickStartTaskDone(site.id)) {
                quickStartStore.setQuickStartCompleted(site.id.toLong(), true)
                analyticsTrackerWrapper.track(Stat.QUICK_START_ALL_TASKS_COMPLETED, mySiteImprovementsFeatureConfig)
                val payload = CompleteQuickStartPayload(site, NEXT_STEPS.toString())
                dispatcher.dispatch(SiteActionBuilder.newCompleteQuickStartAction(payload))
            }
        }
    }

    private fun setTaskDoneAndTrack(
        task: QuickStartTask,
        siteId: Int
    ) {
        quickStartStore.setDoneTask(siteId.toLong(), task, true)
        analyticsTrackerWrapper.track(quickStartUtils.getTaskCompletedTracker(task), mySiteImprovementsFeatureConfig)
    }

    fun requestNextStepOfTask(task: QuickStartTask) {
        if (task != activeTask.value) return
        _activeTask.value = null
        pendingTask = task
        eventBus.postSticky(QuickStartEvent(task))
    }

    fun clear() {
        job.cancel()
    }

    private suspend fun onCategoryCompleted(siteId: Int, categoryType: QuickStartTaskType) {
        val completionMessage = getCategoryCompletionMessage(categoryType)
        _onSnackbar.postValue(Event(SnackbarMessageHolder(UiStringText(completionMessage.asHtml()))))
        dynamicCardStore.removeCard(siteId, categoryType.toDynamicCardType())
    }

    private fun getCategoryCompletionMessage(taskType: QuickStartTaskType) = when (taskType) {
        CUSTOMIZE -> R.string.quick_start_completed_type_customize_message
        GROW -> R.string.quick_start_completed_type_grow_message
        UNKNOWN -> throw IllegalArgumentException("Unexpected quick start type")
    }.let { resourceProvider.getString(it) }

    private fun String.asHtml() = htmlCompat.fromHtml(this)

    private fun DynamicCardType.toQuickStartTaskType(): QuickStartTaskType {
        return when (this) {
            CUSTOMIZE_QUICK_START -> CUSTOMIZE
            GROW_QUICK_START -> GROW
        }
    }

    private fun QuickStartTaskType.toDynamicCardType(): DynamicCardType {
        return when (this) {
            CUSTOMIZE -> CUSTOMIZE_QUICK_START
            GROW -> GROW_QUICK_START
            UNKNOWN -> throw IllegalArgumentException("Unexpected quick start type")
        }
    }

    data class QuickStartCategory(
        val taskType: QuickStartTaskType,
        val uncompletedTasks: List<QuickStartTaskDetails>,
        val completedTasks: List<QuickStartTaskDetails>
    )
}
