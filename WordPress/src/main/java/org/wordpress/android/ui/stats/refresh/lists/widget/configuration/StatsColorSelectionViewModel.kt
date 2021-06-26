package org.sitebay.android.ui.stats.refresh.lists.widget.configuration

import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineDispatcher
import org.sitebay.android.R
import org.sitebay.android.fluxc.store.AccountStore
import org.sitebay.android.modules.UI_THREAD
import org.sitebay.android.ui.prefs.AppPrefsWrapper
import org.sitebay.android.viewmodel.Event
import org.sitebay.android.viewmodel.ScopedViewModel
import javax.inject.Inject
import javax.inject.Named

class StatsColorSelectionViewModel
@Inject constructor(
    @Named(UI_THREAD) private val mainDispatcher: CoroutineDispatcher,
    private val accountStore: AccountStore,
    private val appPrefsWrapper: AppPrefsWrapper
) : ScopedViewModel(mainDispatcher) {
    private val mutableViewMode = MutableLiveData<Color>()
    val viewMode: LiveData<Color> = mutableViewMode

    private val mutableNotification = MutableLiveData<Event<Int>>()
    val notification: LiveData<Event<Int>> = mutableNotification

    private val mutableDialogOpened = MutableLiveData<Event<Unit>>()
    val dialogOpened: LiveData<Event<Unit>> = mutableDialogOpened

    private var appWidgetId: Int = -1

    fun start(
        appWidgetId: Int
    ) {
        this.appWidgetId = appWidgetId
        val colorMode = appPrefsWrapper.getAppWidgetColor(appWidgetId)
        if (colorMode != null) {
            mutableViewMode.postValue(colorMode)
        }
    }

    fun selectColor(color: Color) {
        mutableViewMode.postValue(color)
    }

    fun openColorDialog() {
        if (accountStore.hasAccessToken()) {
            mutableDialogOpened.postValue(Event(Unit))
        } else {
            mutableNotification.postValue(Event(R.string.stats_widget_log_in_message))
        }
    }

    enum class Color(@StringRes val title: Int) {
        LIGHT(R.string.stats_widget_color_light), DARK(R.string.stats_widget_color_dark)
    }
}
