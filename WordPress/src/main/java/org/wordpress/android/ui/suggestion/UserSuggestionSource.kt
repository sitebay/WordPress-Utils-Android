package org.sitebay.android.ui.suggestion

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe
import org.sitebay.android.datasets.UserSuggestionTable
import org.sitebay.android.fluxc.model.SiteModel
import org.sitebay.android.modules.BG_THREAD
import org.sitebay.android.ui.suggestion.service.SuggestionEvents.SuggestionNameListUpdated
import org.sitebay.android.ui.suggestion.util.SuggestionServiceConnectionManager
import org.sitebay.android.util.EventBusWrapper
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.CoroutineContext

class UserSuggestionSource @Inject constructor(
    context: Context,
    override val site: SiteModel,
    private val eventBusWrapper: EventBusWrapper,
    @Named(BG_THREAD) private val bgDispatcher: CoroutineDispatcher
) : SuggestionSource, CoroutineScope {
    override val coroutineContext: CoroutineContext = bgDispatcher + Job()
    private val connectionManager = SuggestionServiceConnectionManager(context, site.siteId)

    private val _suggestions = MutableLiveData<SuggestionResult>()
    override val suggestionData: LiveData<SuggestionResult> = _suggestions

    private var isFetching: Boolean = false

    override fun initialize() {
        postSavedSuggestions(false)
        isFetching = true
        connectionManager.bindToService()
        eventBusWrapper.register(this)
    }

    private fun postSavedSuggestions(suggestionsWereJustUpdated: Boolean) {
        launch {
            val suggestions = Suggestion.fromUserSuggestions(
                    UserSuggestionTable.getSuggestionsForSite(site.siteId)
            )

            // Only send empty suggestions if they are recent
            if (suggestions.isNotEmpty() || suggestionsWereJustUpdated) {
                _suggestions.postValue(SuggestionResult(suggestions, false))
            }
        }
    }

    override fun refreshSuggestions() {
        isFetching = true
        connectionManager.apply {
            unbindFromService()
            bindToService()
        }
    }

    @Subscribe
    fun onEventMainThread(event: SuggestionNameListUpdated) {
        if (event.mRemoteBlogId == site.siteId) {
            isFetching = false
            postSavedSuggestions(true)
        }
    }

    override fun isFetchInProgress(): Boolean = isFetching

    override fun onCleared() {
        eventBusWrapper.unregister(this)
        connectionManager.unbindFromService()
    }
}
