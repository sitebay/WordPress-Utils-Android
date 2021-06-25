package org.wordpress.android.ui.sitecreation.domains

import android.util.Log
import androidx.annotation.StringRes
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.wordpress.android.R
import org.wordpress.android.fluxc.Dispatcher
import org.wordpress.android.fluxc.store.SiteStore.OnSuggestedDomains
import org.wordpress.android.fluxc.store.SiteStore.SuggestDomainErrorType
import org.wordpress.android.models.networkresource.ListState
import org.wordpress.android.models.networkresource.ListState.Error
import org.wordpress.android.models.networkresource.ListState.Loading
import org.wordpress.android.models.networkresource.ListState.Ready
import org.wordpress.android.models.networkresource.ListState.Success
import org.wordpress.android.modules.BG_THREAD
import org.wordpress.android.modules.UI_THREAD
import org.wordpress.android.ui.sitecreation.misc.SiteCreationHeaderUiState
import org.wordpress.android.ui.sitecreation.misc.SiteCreationSearchInputUiState
import org.wordpress.android.ui.sitecreation.misc.SiteCreationTracker
import org.wordpress.android.ui.sitecreation.usecases.FetchDomainsUseCase
import org.wordpress.android.ui.utils.UiString
import org.wordpress.android.ui.utils.UiString.UiStringRes
import org.wordpress.android.util.NetworkUtilsWrapper
import org.wordpress.android.viewmodel.SingleLiveEvent
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.CoroutineContext
import kotlin.properties.Delegates

class SiteCreationLoginDetailsViewModel @Inject constructor(
    @Named(BG_THREAD) private val bgDispatcher: CoroutineDispatcher,
    @Named(UI_THREAD) private val mainDispatcher: CoroutineDispatcher
) : ViewModel(), CoroutineScope {
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = bgDispatcher + job
    private var onCreateSiteSelected: (() -> Unit)? = null

    private val _createSiteBtnClicked = SingleLiveEvent<Map<String, String>>()
    val createSiteBtnClicked: LiveData<Map<String, String>> = _createSiteBtnClicked

    private val _onHelpClicked = SingleLiveEvent<Unit>()
    val onHelpClicked: LiveData<Unit> = _onHelpClicked



    fun createSiteBtnClicked(wpBlogName: String, wpFirstName: String, wpLastName: String, wpEmail: String, wpUsername: String, wpPassword: String) {
        val wpValues: Map<String, String> = mapOf(
                "wpBlogName" to wpBlogName,
                "wpFirstName" to wpFirstName,
                "wpLastName" to wpLastName,
                "wpEmail" to wpEmail,
                "wpUsername" to wpUsername,
                "wpPassword" to wpPassword
        )
        Log.i("TEST LOGIN DETAILS", "BUTTON CLICKED")
        _createSiteBtnClicked.value = wpValues
        onCreateSiteSelected?.invoke()
    }



    fun onHelpClicked() {
        _onHelpClicked.call()
    }

}

