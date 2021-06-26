package org.sitebay.android.ui.stats.refresh

import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.sitebay.android.R
import org.sitebay.android.ui.pages.SnackbarMessageHolder
import org.sitebay.android.ui.stats.refresh.StatsViewModel.DateSelectorUiModel
import org.sitebay.android.ui.stats.refresh.lists.StatsBlock
import org.sitebay.android.ui.stats.refresh.lists.sections.BaseStatsUseCase
import org.sitebay.android.ui.stats.refresh.lists.sections.BaseStatsUseCase.UseCaseModel.UseCaseState.EMPTY
import org.sitebay.android.ui.stats.refresh.lists.sections.BaseStatsUseCase.UseCaseModel.UseCaseState.ERROR
import org.sitebay.android.ui.stats.refresh.lists.sections.BaseStatsUseCase.UseCaseModel.UseCaseState.LOADING
import org.sitebay.android.ui.stats.refresh.lists.sections.BaseStatsUseCase.UseCaseModel.UseCaseState.SUCCESS
import org.sitebay.android.ui.stats.refresh.lists.sections.granular.SelectedDateProvider.SelectedDate
import org.sitebay.android.ui.stats.refresh.utils.StatsDateSelector
import org.sitebay.android.ui.stats.refresh.utils.StatsSiteProvider
import org.sitebay.android.ui.utils.UiString.UiStringRes
import org.sitebay.android.util.map
import org.sitebay.android.util.mapNullable
import org.sitebay.android.util.throttle
import org.sitebay.android.viewmodel.Event
import org.sitebay.android.viewmodel.ScopedViewModel

class StatsViewAllViewModel(
    val mainDispatcher: CoroutineDispatcher,
    val bgDispatcher: CoroutineDispatcher,
    val useCase: BaseStatsUseCase<*, *>,
    private val statsSiteProvider: StatsSiteProvider,
    private val dateSelector: StatsDateSelector,
    @StringRes val title: Int
) : ScopedViewModel(mainDispatcher) {
    private val mutableSnackbarMessage = MutableLiveData<Event<SnackbarMessageHolder>>()

    val selectedDate = dateSelector.selectedDate

    val dateSelectorData: LiveData<DateSelectorUiModel> = dateSelector.dateSelectorData.mapNullable {
        it ?: DateSelectorUiModel(false)
    }

    val navigationTarget: LiveData<Event<NavigationTarget>> = useCase.navigationTarget

    val data: LiveData<StatsBlock> = useCase.liveData.map { useCaseModel ->
        when (useCaseModel.state) {
            SUCCESS -> StatsBlock.Success(useCaseModel.type, useCaseModel.data ?: listOf())
            ERROR -> StatsBlock.Error(useCaseModel.type, useCaseModel.stateData ?: useCaseModel.data ?: listOf())
            LOADING -> StatsBlock.Loading(useCaseModel.type, useCaseModel.data ?: useCaseModel.stateData ?: listOf())
            EMPTY -> StatsBlock.EmptyBlock(useCaseModel.type, useCaseModel.stateData ?: useCaseModel.data ?: listOf())
        }
    }.throttle(viewModelScope, true)

    private val _isRefreshing = MutableLiveData<Boolean>()
    val isRefreshing: LiveData<Boolean> = _isRefreshing

    val showSnackbarMessage: LiveData<Event<SnackbarMessageHolder>> = mutableSnackbarMessage

    val toolbarHasShadow = dateSelectorData.map { !it.isVisible }

    fun start(startDate: SelectedDate?) {
        launch {
            startDate?.let {
                dateSelector.start(startDate)
            }
            loadData(refresh = false, forced = false)
            dateSelector.updateDateSelector()
        }
        dateSelector.updateDateSelector()
    }

    fun onPullToRefresh() {
        mutableSnackbarMessage.value = null
        refreshData()
    }

    private fun loadData(executeLoading: suspend () -> Unit) = launch {
        _isRefreshing.value = true

        executeLoading()

        _isRefreshing.value = false
    }

    private suspend fun loadData(refresh: Boolean, forced: Boolean) {
        withContext(bgDispatcher) {
            if (statsSiteProvider.hasLoadedSite()) {
                useCase.fetch(refresh, forced)
            } else {
                mutableSnackbarMessage.postValue(
                        Event(SnackbarMessageHolder(UiStringRes(R.string.stats_site_not_loaded_yet)))
                )
            }
        }
    }

    override fun onCleared() {
        mutableSnackbarMessage.value = null
        useCase.clear()
        statsSiteProvider.reset()
    }

    fun onRetryClick() {
        refreshData()
    }

    fun onNextDateSelected() {
        launch(mainDispatcher) {
            dateSelector.onNextDateSelected()
        }
    }

    fun onPreviousDateSelected() {
        launch(mainDispatcher) {
            dateSelector.onPreviousDateSelected()
        }
    }

    fun onDateChanged() {
        loadData {
            loadData(refresh = true, forced = false)
        }
    }

    private fun refreshData() {
        loadData {
            loadData(refresh = true, forced = true)
        }
    }

    fun getSelectedDate(): SelectedDate {
        return dateSelector.getSelectedDate()
    }
}
