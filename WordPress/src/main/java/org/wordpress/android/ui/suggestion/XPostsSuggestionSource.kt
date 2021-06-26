package org.sitebay.android.ui.suggestion

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.sitebay.android.fluxc.model.SiteModel
import org.sitebay.android.fluxc.store.XPostsResult
import org.sitebay.android.fluxc.store.XPostsStore
import org.sitebay.android.modules.BG_THREAD
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.CoroutineContext

class XPostsSuggestionSource @Inject constructor(
    private val xPostsStore: XPostsStore,
    override val site: SiteModel,
    @Named(BG_THREAD) private val bgDispatcher: CoroutineDispatcher
) : SuggestionSource, CoroutineScope {
    override val coroutineContext: CoroutineContext = bgDispatcher + Job()

    private val _suggestions = MutableLiveData<SuggestionResult>()
    override val suggestionData: LiveData<SuggestionResult> = _suggestions

    @VisibleForTesting
    var fetchJob: Job? = null

    override fun initialize() {
        launch {
            when (val result = xPostsStore.getXPostsFromDb(site)) {
                is XPostsResult.Result -> {
                    val xPostSuggestions = suggestionsFromResult(result)
                    if (xPostSuggestions.isNotEmpty()) {
                        _suggestions.postValue(SuggestionResult(xPostSuggestions, false))
                    }
                }
            }
        }
        refreshSuggestions()
    }

    private fun suggestionsFromResult(result: XPostsResult.Result): List<Suggestion> =
            result.xPosts
                    .map { Suggestion.fromXpost(it) }
                    .sortedBy { it.value }

    override fun refreshSuggestions() {
        if (fetchJob?.isActive != true) {
            fetchJob = launch {
                val result = when (val fetchResult = xPostsStore.fetchXPosts(site)) {
                    is XPostsResult.Result -> {
                        val xPosts = suggestionsFromResult(fetchResult)
                        SuggestionResult(xPosts, false)
                    }
                    XPostsResult.Unknown -> {
                        val previousXposts = suggestionData.value?.suggestions ?: emptyList()
                        SuggestionResult(previousXposts, true)
                    }
                }
                _suggestions.postValue(result)
            }
        }
    }

    override fun isFetchInProgress(): Boolean = fetchJob?.isActive == true

    override fun onCleared() { /* Do nothing */ }
}
