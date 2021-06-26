package org.sitebay.android.ui.accounts.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineDispatcher
import org.sitebay.android.R
import org.sitebay.android.analytics.AnalyticsTracker.Stat
import org.sitebay.android.modules.UI_THREAD
import org.sitebay.android.ui.accounts.LoginNavigationEvents
import org.sitebay.android.ui.accounts.LoginNavigationEvents.ShowEmailLoginScreen
import org.sitebay.android.ui.accounts.LoginNavigationEvents.ShowLoginViaSiteAddressScreen
import org.sitebay.android.ui.accounts.UnifiedLoginTracker
import org.sitebay.android.ui.accounts.UnifiedLoginTracker.Click.CONTINUE_WITH_WORDPRESS_COM
import org.sitebay.android.ui.accounts.UnifiedLoginTracker.Click.LOGIN_WITH_SITE_ADDRESS
import org.sitebay.android.ui.accounts.UnifiedLoginTracker.Flow
import org.sitebay.android.ui.accounts.UnifiedLoginTracker.Step.PROLOGUE
import org.sitebay.android.ui.accounts.login.LoginPrologueViewModel.ButtonUiState.ContinueWithWpcomButtonState
import org.sitebay.android.ui.accounts.login.LoginPrologueViewModel.ButtonUiState.EnterYourSiteAddressButtonState
import org.sitebay.android.util.analytics.AnalyticsTrackerWrapper
import org.sitebay.android.viewmodel.Event
import org.sitebay.android.viewmodel.ScopedViewModel
import javax.inject.Inject
import javax.inject.Named

class LoginPrologueViewModel @Inject constructor(
    private val unifiedLoginTracker: UnifiedLoginTracker,
    private val analyticsTrackerWrapper: AnalyticsTrackerWrapper,
    @Named(UI_THREAD) mainDispatcher: CoroutineDispatcher
) : ScopedViewModel(mainDispatcher) {
    private val _navigationEvents = MediatorLiveData<Event<LoginNavigationEvents>>()
    val navigationEvents: LiveData<Event<LoginNavigationEvents>> = _navigationEvents

    private val _uiState: MutableLiveData<UiState> = MutableLiveData()
    val uiState: LiveData<UiState> = _uiState

    private var isStarted = false
    fun start() {
        if (isStarted) return
        isStarted = true

        analyticsTrackerWrapper.track(stat = Stat.LOGIN_PROLOGUE_VIEWED)
        unifiedLoginTracker.track(flow = Flow.PROLOGUE, step = PROLOGUE)

        _uiState.value = UiState(
                enterYourSiteAddressButtonState = EnterYourSiteAddressButtonState(::onEnterYourSiteAddressButtonClick),
                continueWithWpcomButtonState = ContinueWithWpcomButtonState(::onContinueWithWpcomButtonClick)
        )
    }

    fun onFragmentResume() {
        unifiedLoginTracker.setFlowAndStep(Flow.PROLOGUE, PROLOGUE)
    }

    private fun onContinueWithWpcomButtonClick() {
        unifiedLoginTracker.trackClick(CONTINUE_WITH_WORDPRESS_COM)
        _navigationEvents.postValue(Event(ShowEmailLoginScreen))
    }

    private fun onEnterYourSiteAddressButtonClick() {
        unifiedLoginTracker.trackClick(LOGIN_WITH_SITE_ADDRESS)
        _navigationEvents.postValue(Event(ShowLoginViaSiteAddressScreen))
    }

    data class UiState(
        val enterYourSiteAddressButtonState: EnterYourSiteAddressButtonState,
        val continueWithWpcomButtonState: ContinueWithWpcomButtonState
    )

    sealed class ButtonUiState {
        abstract val title: Int
        abstract val onClick: (() -> Unit)

        data class ContinueWithWpcomButtonState(override val onClick: () -> Unit) : ButtonUiState() {
            override val title = R.string.continue_with_wpcom_no_signup
        }

        data class EnterYourSiteAddressButtonState(override val onClick: () -> Unit) : ButtonUiState() {
            override val title = R.string.enter_your_site_address
        }
    }
}
