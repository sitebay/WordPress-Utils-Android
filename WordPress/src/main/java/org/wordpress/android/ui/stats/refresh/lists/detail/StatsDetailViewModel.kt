package org.sitebay.android.ui.stats.refresh.lists.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineDispatcher
import org.sitebay.android.R
import org.sitebay.android.modules.UI_THREAD
import org.sitebay.android.ui.pages.SnackbarMessageHolder
import org.sitebay.android.ui.stats.refresh.BLOCK_DETAIL_USE_CASE
import org.sitebay.android.ui.stats.refresh.lists.BaseListUseCase
import org.sitebay.android.ui.stats.refresh.utils.StatsPostProvider
import org.sitebay.android.ui.stats.refresh.utils.StatsSiteProvider
import org.sitebay.android.ui.utils.UiString.UiStringRes
import org.sitebay.android.util.NetworkUtilsWrapper
import org.sitebay.android.util.mergeNotNull
import org.sitebay.android.viewmodel.ScopedViewModel
import javax.inject.Inject
import javax.inject.Named

class StatsDetailViewModel
@Inject constructor(
    @Named(UI_THREAD) mainDispatcher: CoroutineDispatcher,
    @Named(BLOCK_DETAIL_USE_CASE) private val detailUseCase: BaseListUseCase,
    private val statsSiteProvider: StatsSiteProvider,
    private val statsPostProvider: StatsPostProvider,
    private val networkUtilsWrapper: NetworkUtilsWrapper
) : ScopedViewModel(mainDispatcher) {
    private val _isRefreshing = MutableLiveData<Boolean>()
    val isRefreshing: LiveData<Boolean> = _isRefreshing

    private val _showSnackbarMessage = mergeNotNull(
            detailUseCase.snackbarMessage,
            distinct = true,
            singleEvent = true
    )
    val showSnackbarMessage: LiveData<SnackbarMessageHolder> = _showSnackbarMessage

    fun init(
        postId: Long,
        postType: String,
        postTitle: String,
        postUrl: String?
    ) {
        statsPostProvider.init(postId, postType, postTitle, postUrl)
    }

    fun refresh() {
        launch {
            detailUseCase.refreshData(true)
            _isRefreshing.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        detailUseCase.onCleared()
        statsPostProvider.clear()
    }

    fun onPullToRefresh() {
        _showSnackbarMessage.value = null
        statsSiteProvider.clear()
        if (networkUtilsWrapper.isNetworkAvailable()) {
            refresh()
        } else {
            _isRefreshing.value = false
            _showSnackbarMessage.value = SnackbarMessageHolder(UiStringRes(R.string.no_network_title))
        }
    }
}
