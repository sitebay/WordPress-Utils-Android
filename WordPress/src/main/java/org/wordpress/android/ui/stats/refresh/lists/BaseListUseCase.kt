package org.sitebay.android.ui.stats.refresh.lists

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.sitebay.android.R
import org.sitebay.android.fluxc.model.SiteModel
import org.sitebay.android.fluxc.store.StatsStore.StatsType
import org.sitebay.android.ui.pages.SnackbarMessageHolder
import org.sitebay.android.ui.stats.refresh.NavigationTarget
import org.sitebay.android.ui.stats.refresh.lists.StatsListViewModel.StatsSection
import org.sitebay.android.ui.stats.refresh.lists.StatsListViewModel.UiModel
import org.sitebay.android.ui.stats.refresh.lists.sections.BaseStatsUseCase
import org.sitebay.android.ui.stats.refresh.lists.sections.BaseStatsUseCase.UseCaseModel
import org.sitebay.android.ui.stats.refresh.lists.sections.BaseStatsUseCase.UseCaseParam
import org.sitebay.android.ui.stats.refresh.utils.StatsSiteProvider
import org.sitebay.android.ui.utils.UiString.UiStringRes
import org.sitebay.android.util.PackageUtils
import org.sitebay.android.util.combineMap
import org.sitebay.android.util.distinct
import org.sitebay.android.util.map
import org.sitebay.android.util.mapAsync
import org.sitebay.android.util.mergeAsyncNotNull
import org.sitebay.android.util.mergeNotNull
import org.sitebay.android.viewmodel.Event
import org.sitebay.android.viewmodel.SingleLiveEvent
import kotlin.coroutines.CoroutineContext

class BaseListUseCase(
    private val bgDispatcher: CoroutineDispatcher,
    private val mainDispatcher: CoroutineDispatcher,
    private val statsSiteProvider: StatsSiteProvider,
    private val useCases: List<BaseStatsUseCase<*, *>>,
    private val getStatsTypes: suspend (SiteModel) -> List<StatsType>,
    private val mapUiModel: (
        useCaseModels: List<UseCaseModel>,
        showError: (Int) -> Unit
    ) -> UiModel
) : CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = bgDispatcher

    private val blockListData = combineMap(
            useCases.associateBy { it.type }.mapValues { entry -> entry.value.liveData }
    )
    private val statsTypes = MutableLiveData<List<StatsType>>()
    val data: MediatorLiveData<UiModel> = mergeAsyncNotNull(this, statsTypes, blockListData) { types, map ->
        val result = types.mapNotNull {
            if (map.containsKey(it)) {
                map[it]
            } else {
                null
            }
        }
        result
    }.mapAsync(this) { useCaseModels ->
        mapUiModel(useCaseModels) { message ->
            mutableSnackbarMessage.postValue(message)
        }
    }.distinct()

    private val mutableNavigationTarget = MutableLiveData<Event<NavigationTarget>>()
    val navigationTarget: LiveData<Event<NavigationTarget>> = mergeNotNull(
            useCases.map { it.navigationTarget } + mutableNavigationTarget,
            distinct = false
    )

    private val mutableSnackbarMessage = MutableLiveData<Int>()
    val snackbarMessage: LiveData<SnackbarMessageHolder> = mutableSnackbarMessage.map {
        SnackbarMessageHolder(UiStringRes(it))
    }

    private val mutableListSelected = SingleLiveEvent<Unit>()
    val listSelected: LiveData<Unit> = mutableListSelected

    suspend fun loadData() {
        loadData(refresh = false, forced = false)
    }

    suspend fun refreshData(forced: Boolean = false) {
        loadData(true, forced)
    }

    private suspend fun onParamChanged(param: UseCaseParam) {
        statsTypes.value?.forEach { type ->
            useCases.find { it.type == type }
                    ?.let { block ->
                        withContext(bgDispatcher) {
                            block.onParamsChange(param)
                        }
                    }
        }
    }

    suspend fun refreshTypes(): List<StatsType> {
        val items = getStatsTypes(statsSiteProvider.siteModel)
        if (statsTypes.value != items) {
            withContext(mainDispatcher) {
                statsTypes.value = items
            }
        }
        return items
    }

    private suspend fun loadData(refresh: Boolean, forced: Boolean) {
        if (statsSiteProvider.hasLoadedSite()) {
            withContext(bgDispatcher) {
                if (PackageUtils.isDebugBuild() && useCases.distinctBy { it.type }.size < useCases.size) {
                    throw RuntimeException("Duplicate stats type in a use case")
                }
                val visibleTypes = refreshTypes()
                visibleTypes.forEach { type ->
                    useCases.find { it.type == type }
                            ?.let { block ->
                                launch(bgDispatcher) {
                                    block.fetch(refresh, forced)
                                }
                            }
                }
            }
        } else {
            mutableSnackbarMessage.postValue(R.string.stats_site_not_loaded_yet)
        }
    }

    fun onCleared() {
        mutableSnackbarMessage.value = null
        blockListData.value = null
        useCases.forEach { it.clear() }
        data.value = null
    }

    suspend fun onDateChanged(selectedSection: StatsSection) {
        onParamChanged(UseCaseParam.SelectedDateParam(selectedSection))
    }

    fun onListSelected() {
        mutableListSelected.call()
    }
}
