package org.sitebay.android.ui.stats.refresh.lists.widget.configuration

import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineDispatcher
import org.sitebay.android.R
import org.sitebay.android.R.string
import org.sitebay.android.fluxc.store.AccountStore
import org.sitebay.android.modules.UI_THREAD
import org.sitebay.android.ui.prefs.AppPrefsWrapper
import org.sitebay.android.viewmodel.Event
import org.sitebay.android.viewmodel.ScopedViewModel
import javax.inject.Inject
import javax.inject.Named

class StatsDataTypeSelectionViewModel
@Inject constructor(
    @Named(UI_THREAD) private val mainDispatcher: CoroutineDispatcher,
    private val accountStore: AccountStore,
    private val appPrefsWrapper: AppPrefsWrapper
) : ScopedViewModel(mainDispatcher) {
    private val mutableDataType = MutableLiveData<DataType>()
    val dataType: LiveData<DataType> = mutableDataType

    private val mutableNotification = MutableLiveData<Event<Int>>()
    val notification: LiveData<Event<Int>> = mutableNotification

    private val mutableDialogOpened = MutableLiveData<Event<Unit>>()
    val dialogOpened: LiveData<Event<Unit>> = mutableDialogOpened

    private var appWidgetId: Int = -1

    fun start(
        appWidgetId: Int
    ) {
        this.appWidgetId = appWidgetId
        val dataType = appPrefsWrapper.getAppWidgetDataType(appWidgetId)
        if (dataType != null) {
            mutableDataType.postValue(dataType)
        }
    }

    fun selectDataType(dataType: DataType) {
        mutableDataType.postValue(dataType)
    }

    fun openDataTypeDialog() {
        if (accountStore.hasAccessToken()) {
            mutableDialogOpened.postValue(Event(Unit))
        } else {
            mutableNotification.postValue(Event(string.stats_widget_log_in_message))
        }
    }

    enum class DataType(@StringRes val title: Int) {
        VIEWS(R.string.stats_views),
        VISITORS(R.string.stats_visitors),
        COMMENTS(R.string.stats_comments),
        LIKES(R.string.stats_likes)
    }
}
