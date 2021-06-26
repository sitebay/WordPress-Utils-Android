package org.sitebay.android.ui.posts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import org.sitebay.android.R
import org.sitebay.android.analytics.AnalyticsTracker.Stat
import org.sitebay.android.fluxc.model.SiteModel
import org.sitebay.android.fluxc.model.post.PostStatus
import org.sitebay.android.modules.BG_THREAD
import org.sitebay.android.ui.posts.PrepublishingHomeItemUiState.ActionType
import org.sitebay.android.ui.posts.PrepublishingHomeItemUiState.ActionType.CATEGORIES
import org.sitebay.android.ui.posts.PrepublishingHomeItemUiState.ActionType.PUBLISH
import org.sitebay.android.ui.posts.PrepublishingHomeItemUiState.ActionType.TAGS
import org.sitebay.android.ui.posts.PrepublishingHomeItemUiState.HeaderUiState
import org.sitebay.android.ui.posts.PrepublishingHomeItemUiState.HomeUiState
import org.sitebay.android.ui.posts.PrepublishingHomeItemUiState.StoryTitleUiState
import org.sitebay.android.ui.posts.prepublishing.home.usecases.GetButtonUiStateUseCase
import org.sitebay.android.ui.stories.StoryRepositoryWrapper
import org.sitebay.android.ui.stories.usecase.UpdateStoryPostTitleUseCase
import org.sitebay.android.ui.utils.UiString.UiStringRes
import org.sitebay.android.ui.utils.UiString.UiStringText
import org.sitebay.android.util.StringUtils
import org.sitebay.android.util.analytics.AnalyticsTrackerWrapper
import org.sitebay.android.viewmodel.Event
import org.sitebay.android.viewmodel.ScopedViewModel
import javax.inject.Inject
import javax.inject.Named

private const val THROTTLE_DELAY = 500L

class PrepublishingHomeViewModel @Inject constructor(
    private val getPostTagsUseCase: GetPostTagsUseCase,
    private val postSettingsUtils: PostSettingsUtils,
    private val getButtonUiStateUseCase: GetButtonUiStateUseCase,
    private val analyticsTrackerWrapper: AnalyticsTrackerWrapper,
    private val storyRepositoryWrapper: StoryRepositoryWrapper,
    private val updateStoryPostTitleUseCase: UpdateStoryPostTitleUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    @Named(BG_THREAD) private val bgDispatcher: CoroutineDispatcher
) : ScopedViewModel(bgDispatcher) {
    private var isStarted = false
    private var updateStoryTitleJob: Job? = null
    private lateinit var editPostRepository: EditPostRepository

    private val _uiState = MutableLiveData<List<PrepublishingHomeItemUiState>>()
    val uiState: LiveData<List<PrepublishingHomeItemUiState>> = _uiState

    private val _storyTitleUiState = MutableLiveData<StoryTitleUiState>()
    val storyTitleUiState: LiveData<StoryTitleUiState> = _storyTitleUiState

    private val _onActionClicked = MutableLiveData<Event<ActionType>>()
    val onActionClicked: LiveData<Event<ActionType>> = _onActionClicked

    private val _onSubmitButtonClicked = MutableLiveData<Event<PublishPost>>()
    val onSubmitButtonClicked: LiveData<Event<PublishPost>> = _onSubmitButtonClicked

    fun start(editPostRepository: EditPostRepository, site: SiteModel, isStoryPost: Boolean) {
        this.editPostRepository = editPostRepository
        if (isStarted) return
        isStarted = true

        setupHomeUiState(editPostRepository, site, isStoryPost)
    }

    private fun setupHomeUiState(
        editPostRepository: EditPostRepository,
        site: SiteModel,
        isStoryPost: Boolean
    ) {
        val prepublishingHomeUiStateList = mutableListOf<PrepublishingHomeItemUiState>().apply {
            if (isStoryPost) {
                _storyTitleUiState.postValue(StoryTitleUiState(
                        storyTitle = UiStringText(StringUtils.notNullStr(editPostRepository.title)),
                        storyThumbnailUrl = storyRepositoryWrapper.getCurrentStoryThumbnailUrl()
                ) { storyTitle ->
                    onStoryTitleChanged(storyTitle)
                })
            } else {
                add(HeaderUiState(UiStringText(site.name), StringUtils.notNullStr(site.iconUrl)))
            }

            if (editPostRepository.status != PostStatus.PRIVATE) {
                add(
                        HomeUiState(
                                actionType = PUBLISH,
                                actionResult = editPostRepository.getEditablePost()
                                        ?.let {
                                            UiStringText(
                                                    postSettingsUtils.getPublishDateLabel(
                                                            it
                                                    )
                                            )
                                        },
                                actionClickable = true,
                                onActionClicked = ::onActionClicked
                        )
                )
            } else {
                add(
                        HomeUiState(
                                actionType = PUBLISH,
                                actionResult = editPostRepository.getEditablePost()
                                        ?.let {
                                            UiStringText(
                                                    postSettingsUtils.getPublishDateLabel(
                                                            it
                                                    )
                                            )
                                        },
                                actionTypeColor = R.color.prepublishing_action_type_disabled_color,
                                actionResultColor = R.color.prepublishing_action_result_disabled_color,
                                actionClickable = false,
                                onActionClicked = null
                        )
                )
            }

            if (!editPostRepository.isPage) {
                add(HomeUiState(
                        actionType = TAGS,
                        actionResult = getPostTagsUseCase.getTags(editPostRepository)
                                ?.let { UiStringText(it) }
                                ?: run { UiStringRes(R.string.prepublishing_nudges_home_tags_not_set) },
                        actionClickable = true,
                        onActionClicked = ::onActionClicked
                )
                )

                val categoryString: String = getCategoriesUseCase.getPostCategoriesString(
                        editPostRepository,
                        site
                )
                if (categoryString.isNotEmpty()) {
                    UiStringText(categoryString)
                }
            } else {
                UiStringRes(R.string.prepublishing_nudges_home_categories_not_set)
            }

            val categoriesString = getCategoriesUseCase.getPostCategoriesString(
                    editPostRepository,
                    site
            )

            add(HomeUiState(
                    actionType = CATEGORIES,
                    actionResult = if (categoriesString.isNotEmpty()) {
                        UiStringText(categoriesString)
                    } else {
                        run { UiStringRes(R.string.prepublishing_nudges_home_categories_not_set) }
                    },
                    actionClickable = true,
                    onActionClicked = ::onActionClicked
            ))

            add(getButtonUiStateUseCase.getUiState(editPostRepository, site) { publishPost ->
                launch(bgDispatcher) {
                    waitForStoryTitleJobAndSubmit(publishPost)
                }
            })
        }.toList()

        _uiState.postValue(prepublishingHomeUiStateList)
    }

    private fun onStoryTitleChanged(storyTitle: String) {
        updateStoryTitleJob?.cancel()
        updateStoryTitleJob = launch(bgDispatcher) {
            // there's a delay here since every single character change event triggers onStoryTitleChanged
            // and without a delay we would have multiple save operations being triggered unnecessarily.
            delay(THROTTLE_DELAY)
            storyRepositoryWrapper.setCurrentStoryTitle(storyTitle)
            updateStoryPostTitleUseCase.updateStoryTitle(storyTitle, editPostRepository)
        }
    }

    private suspend fun waitForStoryTitleJobAndSubmit(publishPost: PublishPost) {
        updateStoryTitleJob?.join()
        analyticsTrackerWrapper.trackPrepublishingNudges(Stat.EDITOR_POST_PUBLISH_NOW_TAPPED)
        _onSubmitButtonClicked.postValue(Event(publishPost))
    }

    override fun onCleared() {
        super.onCleared()
        updateStoryTitleJob?.cancel()
    }

    private fun onActionClicked(actionType: ActionType) {
        _onActionClicked.postValue(Event(actionType))
    }
}
