package org.sitebay.android.ui.sitecreation.theme

import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.sitebay.android.R
import org.sitebay.android.fluxc.Dispatcher
import org.sitebay.android.modules.BG_THREAD
import org.sitebay.android.modules.UI_THREAD
import org.sitebay.android.ui.sitecreation.misc.SiteCreationErrorType.INTERNET_UNAVAILABLE_ERROR
import org.sitebay.android.ui.sitecreation.misc.SiteCreationErrorType.UNKNOWN
import org.sitebay.android.ui.sitecreation.misc.SiteCreationTracker
import org.sitebay.android.ui.layoutpicker.LayoutPickerUiState.Content
import org.sitebay.android.ui.layoutpicker.LayoutPickerUiState.Loading
import org.sitebay.android.ui.layoutpicker.LayoutPickerUiState.Error
import org.sitebay.android.ui.layoutpicker.LayoutPickerViewModel
import org.sitebay.android.ui.layoutpicker.toLayoutCategories
import org.sitebay.android.ui.layoutpicker.toLayoutModels
import org.sitebay.android.ui.sitecreation.usecases.FetchHomePageLayoutsUseCase
import org.sitebay.android.util.NetworkUtilsWrapper
import org.sitebay.android.viewmodel.SingleLiveEvent
import javax.inject.Inject
import javax.inject.Named

const val defaultTemplateSlug = "default"

private const val ERROR_CONTEXT = "design"

class HomePagePickerViewModel @Inject constructor(
    override val networkUtils: NetworkUtilsWrapper,
    private val dispatcher: Dispatcher,
    private val fetchHomePageLayoutsUseCase: FetchHomePageLayoutsUseCase,
    private val analyticsTracker: SiteCreationTracker,
    @Named(BG_THREAD) override val bgDispatcher: CoroutineDispatcher,
    @Named(UI_THREAD) override val mainDispatcher: CoroutineDispatcher
) : LayoutPickerViewModel(mainDispatcher, bgDispatcher, networkUtils, analyticsTracker) {
    private val _onDesignActionPressed = SingleLiveEvent<DesignSelectionAction>()
    val onDesignActionPressed: LiveData<DesignSelectionAction> = _onDesignActionPressed

    private val _onBackButtonPressed = SingleLiveEvent<Unit>()
    val onBackButtonPressed: LiveData<Unit> = _onBackButtonPressed

    override val useCachedData: Boolean = false

    sealed class DesignSelectionAction(val template: String) {
        object Skip : DesignSelectionAction(defaultTemplateSlug)
        class Choose(template: String) : DesignSelectionAction(template)
    }

    init {
        dispatcher.register(fetchHomePageLayoutsUseCase)
    }

    override fun onCleared() {
        super.onCleared()
        dispatcher.unregister(fetchHomePageLayoutsUseCase)
    }

    fun start(isTablet: Boolean = false) {
        initializePreviewMode(isTablet)
        if (uiState.value !is Content) {
            analyticsTracker.trackSiteDesignViewed(selectedPreviewMode().key)
            fetchLayouts()
        }
    }

    override fun fetchLayouts(preferCache: Boolean) {
        if (!networkUtils.isNetworkAvailable()) {
            analyticsTracker.trackErrorShown(ERROR_CONTEXT, INTERNET_UNAVAILABLE_ERROR, "Retry error")
            updateUiState(Error(toast = R.string.hpp_retry_error))
            return
        }
        if (isLoading) return
        updateUiState(Loading)
        launch {
            val event = fetchHomePageLayoutsUseCase.fetchStarterDesigns()
            withContext(mainDispatcher) {
                if (event.isError) {
                    analyticsTracker.trackErrorShown(ERROR_CONTEXT, UNKNOWN, "Error fetching designs")
                    updateUiState(Error())
                } else {
                    handleResponse(event.designs.toLayoutModels(), event.categories.toLayoutCategories())
                }
            }
        }
    }

    override fun onPreviewChooseTapped() {
        super.onPreviewChooseTapped()
        onChooseTapped()
    }

    fun onChooseTapped() {
        selectedLayout?.let { layout ->
            val template = layout.slug
            analyticsTracker.trackSiteDesignSelected(template)
            _onDesignActionPressed.value = DesignSelectionAction.Choose(template)
            return
        }
        analyticsTracker.trackErrorShown(ERROR_CONTEXT, UNKNOWN, "Error choosing design")
        updateUiState(Error(toast = R.string.hpp_choose_error))
    }

    fun onSkippedTapped() {
        analyticsTracker.trackSiteDesignSkipped()
        _onDesignActionPressed.value = DesignSelectionAction.Skip
    }

    fun onBackPressed() = _onBackButtonPressed.call()
}
