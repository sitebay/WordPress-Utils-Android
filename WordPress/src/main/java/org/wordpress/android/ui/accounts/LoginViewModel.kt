package org.sitebay.android.ui.accounts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import org.sitebay.android.fluxc.store.AccountStore.AuthEmailPayloadScheme
import org.sitebay.android.fluxc.store.SiteStore.ConnectSiteInfoPayload
import org.sitebay.android.ui.accounts.LoginNavigationEvents.ShowNoJetpackSites
import org.sitebay.android.ui.accounts.LoginNavigationEvents.ShowSiteAddressError
import org.sitebay.android.util.BuildConfigWrapper
import org.sitebay.android.viewmodel.Event
import javax.inject.Inject
import kotlin.text.RegexOption.IGNORE_CASE

class LoginViewModel @Inject constructor(private val buildConfigWrapper: BuildConfigWrapper) : ViewModel() {
    private val _navigationEvents = MediatorLiveData<Event<LoginNavigationEvents>>()
    val navigationEvents: LiveData<Event<LoginNavigationEvents>> = _navigationEvents

    fun onHandleSiteAddressError(siteInfo: ConnectSiteInfoPayload) {
        val protocolRegex = Regex("^(http[s]?://)", IGNORE_CASE)
        val siteAddressClean = siteInfo.url.replaceFirst(protocolRegex.toString().toRegex(), "")
        _navigationEvents.postValue(Event(ShowSiteAddressError(siteAddressClean)))
    }

    fun onHandleNoJetpackSites() {
        _navigationEvents.postValue(Event(ShowNoJetpackSites))
    }

    fun getMagicLinkScheme() = if (buildConfigWrapper.isJetpackApp) {
        AuthEmailPayloadScheme.JETPACK
    } else {
        AuthEmailPayloadScheme.WORDPRESS
    }
}
