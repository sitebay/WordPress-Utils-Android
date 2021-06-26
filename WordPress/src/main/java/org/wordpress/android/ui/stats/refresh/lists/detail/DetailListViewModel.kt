package org.sitebay.android.ui.stats.refresh.lists.detail

import kotlinx.coroutines.CoroutineDispatcher
import org.sitebay.android.modules.UI_THREAD
import org.sitebay.android.ui.stats.refresh.BLOCK_DETAIL_USE_CASE
import org.sitebay.android.ui.stats.refresh.lists.BaseListUseCase
import org.sitebay.android.ui.stats.refresh.lists.StatsListViewModel
import org.sitebay.android.ui.stats.refresh.lists.StatsListViewModel.StatsSection.DETAIL
import org.sitebay.android.ui.stats.refresh.utils.StatsDateSelector
import org.sitebay.android.util.analytics.AnalyticsTrackerWrapper
import javax.inject.Inject
import javax.inject.Named

class DetailListViewModel
@Inject constructor(
    @Named(UI_THREAD) mainDispatcher: CoroutineDispatcher,
    @Named(BLOCK_DETAIL_USE_CASE) private val detailUseCase: BaseListUseCase,
    analyticsTracker: AnalyticsTrackerWrapper,
    dateSelectorFactory: StatsDateSelector.Factory
) : StatsListViewModel(mainDispatcher, detailUseCase, analyticsTracker, dateSelectorFactory.build(DETAIL)) {
    override fun onCleared() {
        super.onCleared()
        dateSelector.clear()
    }
}
