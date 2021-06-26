package org.sitebay.android.ui.sitecreation.domains

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
import org.sitebay.android.R
import org.sitebay.android.fluxc.Dispatcher
import org.sitebay.android.fluxc.store.SiteStore.OnSuggestedDomains
import org.sitebay.android.fluxc.store.SiteStore.SuggestDomainErrorType
import org.sitebay.android.models.networkresource.ListState
import org.sitebay.android.models.networkresource.ListState.Error
import org.sitebay.android.models.networkresource.ListState.Loading
import org.sitebay.android.models.networkresource.ListState.Ready
import org.sitebay.android.models.networkresource.ListState.Success
import org.sitebay.android.modules.BG_THREAD
import org.sitebay.android.modules.UI_THREAD
import org.sitebay.android.ui.sitecreation.misc.SiteCreationHeaderUiState
import org.sitebay.android.ui.sitecreation.misc.SiteCreationSearchInputUiState
import org.sitebay.android.ui.sitecreation.misc.SiteCreationTracker
import org.sitebay.android.ui.sitecreation.usecases.FetchDomainsUseCase
import org.sitebay.android.ui.utils.UiString
import org.sitebay.android.ui.utils.UiString.UiStringRes
import org.sitebay.android.util.NetworkUtilsWrapper
import org.sitebay.android.viewmodel.SingleLiveEvent
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

