package org.sitebay.android.ui.accounts.login.jetpack

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import kotlinx.coroutines.CoroutineDispatcher
import org.sitebay.android.modules.UI_THREAD
import org.sitebay.android.ui.accounts.LoginNavigationEvents
import org.sitebay.android.ui.accounts.LoginNavigationEvents.ShowInstructions
import org.sitebay.android.ui.accounts.LoginNavigationEvents.ShowSignInForResultJetpackOnly
import org.sitebay.android.viewmodel.Event
import org.sitebay.android.viewmodel.ScopedViewModel
import javax.inject.Inject
import javax.inject.Named

class LoginSiteCheckErrorViewModel @Inject constructor(
    @Named(UI_THREAD) mainDispatcher: CoroutineDispatcher
) : ScopedViewModel(mainDispatcher) {
    private val _navigationEvents = MediatorLiveData<Event<LoginNavigationEvents>>()
    val navigationEvents: LiveData<Event<LoginNavigationEvents>> = _navigationEvents

    private var isStarted = false
    fun start() {
        if (isStarted) return
        isStarted = true
    }

    fun onSeeInstructionsPressed() {
        _navigationEvents.postValue(Event(ShowInstructions()))
    }

    fun onTryAnotherAccountPressed() {
        _navigationEvents.postValue(Event(ShowSignInForResultJetpackOnly))
    }

    fun onBackPressed() {
        _navigationEvents.postValue(Event(ShowSignInForResultJetpackOnly))
    }
}
